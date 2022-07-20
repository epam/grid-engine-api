/*
 *
 *  * Copyright 2022 EPAM Systems, Inc. (https://www.epam.com/)
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.epam.grid.engine.provider.job.sge;

import com.epam.grid.engine.cmd.CmdExecutor;
import com.epam.grid.engine.cmd.CommandArgUtils;
import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobLogInfo;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.entity.job.JobState;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.ParallelEnvOptions;
import com.epam.grid.engine.entity.job.sge.SgeJob;
import com.epam.grid.engine.entity.job.sge.SgeQueueListing;
import com.epam.grid.engine.mapper.job.sge.SgeJobMapper;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.job.JobProvider;
import com.epam.grid.engine.provider.utils.DirectoryPathUtils;
import com.epam.grid.engine.provider.utils.JaxbUtils;
import com.epam.grid.engine.provider.utils.sge.job.QstatCommandParser;
import com.epam.grid.engine.provider.utils.CommandsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.CommandsUtils.mergeOutputLines;
import static com.epam.grid.engine.utils.TextConstants.DOT;
import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;
import static com.epam.grid.engine.utils.TextConstants.NEW_LINE_DELIMITER;
import static com.epam.grid.engine.utils.TextConstants.SPACE;

/**
 * This class performs various actions with jobs for the SGE engine.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SGE")
public class SgeJobProvider implements JobProvider {

    private static final String GET_LOG_LINES_COMMAND = "get_log_lines";
    private static final String GET_LOGFILE_INFO_COMMAND = "get_logfile_info";
    private static final String HAS_DELETED_JOB = "has deleted job";
    private static final String JOB_FILTER = "filter";
    private static final String JOB_STATE = "state";
    private static final String QDEL_COMMAND = "qdel";
    private static final String QSTAT_COMMAND = "qstat";
    private static final String QSUB_COMMAND = "qsub";
    private static final String ARGUMENTS = "arguments";
    private static final String OPTIONS = "options";
    private static final String LOG_DIR = "logDir";
    private static final String ENV_VARIABLES = "envVariables";
    private static final String CANT_FIND_LOG_FILE = "Can't find the job with id = %d or the job log file.";
    private static final String CANT_PARSE_WC_CMD_RESPONSE = "Can't parse wc-command response on the server.";
    private static final String EXECUTION_RESULT = "Execution result - ";
    private static final String WC_COMMAND_REGEX_PATTERN = "^\\d+ \\d+ .+";
    private static final Pattern FIND_ID_PATTERN = Pattern.compile("\\s\\d+\\s");
    private static final Pattern FIND_DELETE_ID_PATTERN = Pattern.compile("\\d+");

    /**
     * The MapStruct mapping mechanism used.
     */
    private final SgeJobMapper jobMapper;

    /**
     * The command execution mechanism used.
     */
    private final SimpleCmdExecutor simpleCmdExecutor;

    /**
     * An object that forms the structure of an executable command according to a template.
     */
    private final GridEngineCommandCompiler commandCompiler;

    /**
     * The path to the directory where all log files will be stored
     * occurred when processing the job.
     */
    private final String logDir;

    public SgeJobProvider(final SgeJobMapper jobMapper,
                          final SimpleCmdExecutor simpleCmdExecutor,
                          final GridEngineCommandCompiler commandCompiler,
                          @Value("${job.log.dir}") final String logDir,
                          @Value("${grid.engine.shared.folder}") final String gridSharedFolder) {
        this.jobMapper = jobMapper;
        this.simpleCmdExecutor = simpleCmdExecutor;
        this.commandCompiler = commandCompiler;
        this.logDir = DirectoryPathUtils.buildProperDir(gridSharedFolder, logDir).toString();
    }

    /**
     * Gets a list of jobs for the specified filters.
     *
     * @param jobFilter The specified filter.
     * @return List of jobs.
     */
    @Override
    public Listing<Job> filterJobs(final JobFilter jobFilter) {
        final CommandResult result = simpleCmdExecutor.execute(makeQstatCommand(jobFilter));
        if (result.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(result);
        } else if (!result.getStdErr().isEmpty()) {
            log.warn(result.getStdErr().toString());
        }
        return mapJobs(JaxbUtils.unmarshall(String.join(NEW_LINE_DELIMITER,
                                result.getStdOut()),
                        SgeQueueListing.class),
                jobFilter);
    }

    /**
     * Gets the type of the executed engine.
     *
     * @return The type of engine being executed.
     */
    @Override
    public EngineType getProviderType() {
        return EngineType.SGE;
    }

    /**
     * Launches the job with the specified parameters.
     *
     * @param options Parameters for launching the job.
     * @return Launched job.
     */
    @Override
    public Job runJob(final JobOptions options) {
        validateJobOptions(options);
        return buildNewJob(getResultOfExecutedCommand(simpleCmdExecutor, makeQsubCommand(options)));
    }

    /**
     * Deletes the job being performed according to the specified parameters.
     *
     * @param deleteJobFilter Search parameters for the job being deleted.
     * @return Information about the deleted job.
     */
    @Override
    public DeletedJobInfo deleteJob(final DeleteJobFilter deleteJobFilter) {
        validateDeleteRequest(deleteJobFilter);
        return parseDeleteCommandResult(makeQdelCommand(deleteJobFilter));
    }

    /**
     * This method provides information about the log file and obtains the specified number of lines from it.
     *
     * @param jobId    The job identifier.
     * @param logType  The log file type to obtain information from.
     * @param lines    The number of lines.
     * @param fromHead if it's true, lines are taken from the head of the log file, otherwise from the tail.
     * @return The object of {@link JobLogInfo}
     */
    @Override
    public JobLogInfo getJobLogInfo(final int jobId, final JobLogInfo.Type logType,
                                    final int lines, final boolean fromHead) {
        if (lines < 0) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST,
                    String.format("The 'lines' parameter can't be < 0, received value = %d", lines));
        }
        final Context context = new Context();
        context.setVariable("path", getLogFilePath(jobId, logType));
        context.setVariable("lines", lines);
        context.setVariable("fromHead", fromHead);

        final CommandResult resultLogFileInfoCommand = simpleCmdExecutor.execute(
                commandCompiler.compileCommand(getProviderType(), GET_LOGFILE_INFO_COMMAND, context));
        final CommandResult resultLogLinesCommand = simpleCmdExecutor.execute(
                commandCompiler.compileCommand(getProviderType(), GET_LOG_LINES_COMMAND, context));

        if (resultLogFileInfoCommand.getExitCode() != 0 || resultLogLinesCommand.getExitCode() != 0) {
            throw new GridEngineException(HttpStatus.NOT_FOUND, String.format(CANT_FIND_LOG_FILE, jobId));
        }
        final String wcCommandResult = resultLogFileInfoCommand.getStdOut().stream()
                .findFirst()
                .orElseThrow(() -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                        CANT_PARSE_WC_CMD_RESPONSE))
                .trim();
        if (!wcCommandResult.matches(WC_COMMAND_REGEX_PATTERN)) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, CANT_PARSE_WC_CMD_RESPONSE);
        }
        final String[] splitWcCommandResult = wcCommandResult.split(SPACE);

        return JobLogInfo.builder()
                .jobId(jobId)
                .type(logType)
                .lines(resultLogLinesCommand.getStdOut())
                .totalCount(Integer.parseInt(splitWcCommandResult[0]))
                .bytes(Integer.parseInt(splitWcCommandResult[1]))
                .build();
    }

    /**
     * Gets a job log file.
     *
     * @param jobId   The job identifier.
     * @param logType The type of required log file.
     * @return The job log file like a byte array.
     */
    @Override
    public InputStream getJobLogFile(final int jobId, final JobLogInfo.Type logType) {
        try {
            return new BufferedInputStream(new FileInputStream(getLogFilePath(jobId, logType)));
        } catch (final IOException e) {
            throw new GridEngineException(HttpStatus.NOT_FOUND, String.format(CANT_FIND_LOG_FILE, jobId), e);
        }
    }

    /**
     * Creates the structure of an executable command based on the passed filter.
     *
     * @param filter User-defined filter for selecting job data.
     * @return The structure of an executable command.
     */
    private String[] makeQstatCommand(final JobFilter filter) {
        final Context context = new Context();
        context.setVariable(JOB_FILTER, filter);
        if (filter != null) {
            final String jobState = Optional.ofNullable(filter.getState())
                    .filter(StringUtils::hasText)
                    .map(QstatCommandParser::getStateFromStateMap)
                    .orElse(null);
            context.setVariable(JOB_STATE, jobState);
        }
        return commandCompiler.compileCommand(getProviderType(), QSTAT_COMMAND, context);
    }

    /**
     * Creates the structure of an executable command based on the passed options.
     *
     * @param options User-defined options.
     * @return The structure of an executable command.
     */
    private String[] makeQsubCommand(final JobOptions options) {
        final Context context = new Context();
        context.setVariable(OPTIONS, options);
        context.setVariable(LOG_DIR, logDir);
        context.setVariable(ARGUMENTS, CommandArgUtils.toEscapeQuotes(options.getArguments()));
        context.setVariable(ENV_VARIABLES, CommandArgUtils.envVariablesMapToString(options.getEnvVariables()));
        return commandCompiler.compileCommand(getProviderType(), QSUB_COMMAND, context);
    }

    private String getResultOfExecutedCommand(final CmdExecutor cmdExecutor, final String[] command) {
        final CommandResult result = cmdExecutor.execute(command);
        if (result.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return parseJobId(result.getStdOut().get(0));
    }

    /**
     * This method creates the structure of the executed job deletion command based on the passed options.
     *
     * @param filter User-defined conditions for searching for deleted jobs.
     * @return The structure of an executable command.
     */
    private String[] makeQdelCommand(final DeleteJobFilter filter) {
        final Context context = new Context();
        context.setVariable(JOB_FILTER, filter);
        return commandCompiler.compileCommand(getProviderType(), QDEL_COMMAND, context);
    }

    /**
     * This method checks the correctness of the user-defined conditions for searching for deleted jobs.
     *
     * @param deleteJobFilter User-defined conditions.
     */
    private void validateDeleteRequest(final DeleteJobFilter deleteJobFilter) {
        if (!StringUtils.hasText(deleteJobFilter.getUser()) && deleteJobFilter.getId() == null) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST,
                    String.format("Incorrect filling in %s. Either `id` or `user` should be specified for job removal!",
                            deleteJobFilter));
        }
        if (deleteJobFilter.getId() != null && deleteJobFilter.getId() == 0) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST,
                    String.format("Id specified in %s for job removal is invalid!", deleteJobFilter));
        }
    }

    /**
     * Processes the received result of executing the delete command.
     *
     * @param command The structure of an executable delete command.
     * @return Information about the deleted job.
     */
    private DeletedJobInfo parseDeleteCommandResult(final String[] command) {
        final CommandResult result = simpleCmdExecutor.execute(command);
        if (result.getExitCode() == 0) {
            return new DeletedJobInfo(
                    parseDeletedJobId(result.getStdOut()),
                    parseUser(result.getStdOut()));
        }
        if (result.getStdOut().get(0).contains(HAS_DELETED_JOB) && result.getStdErr().isEmpty()) {
            log.warn(EXECUTION_RESULT + result);
            return new DeletedJobInfo(
                    parseDeletedJobId(result.getStdOut()),
                    parseUser(result.getStdOut()));
        }
        throw new GridEngineException(HttpStatus.NOT_FOUND, mergeOutputLines(result.getStdOut())
                + NEW_LINE_DELIMITER + mergeOutputLines(result.getStdErr()));
    }

    /**
     * Gets the job ID from the string.
     *
     * @param jobString A string that can contain the job ID.
     * @return JobID or an empty string.
     */
    private String parseJobId(final String jobString) {
        final Matcher matcher = FIND_ID_PATTERN.matcher(jobString);
        return matcher.find()
                ? matcher.group().trim()
                : EMPTY_STRING;
    }

    private void validateJobOptions(final JobOptions options) {
        if (!StringUtils.hasText(options.getCommand())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Command should be specified!");
        }
        if (!checkParallelEnvOptions(options.getParallelEnvOptions())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Invalid PE specification!");
        }
    }

    private boolean checkParallelEnvOptions(final ParallelEnvOptions options) {
        return options == null
                || StringUtils.hasText(options.getName())
                && (options.getMin() > 0 || options.getMax() > 0)
                && (options.getMax() == 0 || options.getMin() < options.getMax());
    }

    private String getLogFilePath(final int jobId, final JobLogInfo.Type logType) {
        return logDir + jobId + DOT + logType.getSuffix();
    }

    private List<Long> parseDeletedJobId(final List<String> stdOut) {
        return FIND_DELETE_ID_PATTERN.matcher(mergeOutputLines(stdOut))
                .results()
                .map(MatchResult::group)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    private String parseUser(final List<String> stdOut) {
        final String stringStdOut = mergeOutputLines(stdOut);
        return stringStdOut.split(SPACE)[0];
    }

    private Job buildNewJob(final String id) {
        return Job.builder()
                .id(Integer.parseInt(id))
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .build())
                .build();
    }

    private Listing<Job> mapJobs(final SgeQueueListing sgeQueueListing, final JobFilter jobFilter) {
        final List<SgeJob> unfilteredJob = mergeJobs(sgeQueueListing);

        final List<SgeJob> jobs = isValidJobFilter(jobFilter)
                ? filteredJob(unfilteredJob, jobFilter)
                : unfilteredJob;

        return mapToJobListing(jobs);
    }

    private boolean isValidJobFilter(final JobFilter jobFilter) {
        final boolean presentId = isFieldPresent(jobFilter, JobFilter::getIds);
        final boolean presentName = isFieldPresent(jobFilter, JobFilter::getNames);

        return presentId || presentName;
    }

    private boolean isFieldPresent(final JobFilter jobFilter,
                                   final Function<JobFilter, List<?>> fieldExtractor) {
        return Optional.ofNullable(jobFilter)
                .map(fieldExtractor)
                .filter(CollectionUtils::isNotEmpty)
                .isPresent();
    }

    private List<SgeJob> filteredJob(final List<SgeJob> unfilteredJob, final JobFilter jobFilter) {
        return unfilteredJob.stream()
                .filter(sgeJob -> jobFilter(jobFilter).test(sgeJob))
                .collect(Collectors.toList());
    }

    private Listing<Job> mapToJobListing(final List<SgeJob> jobs) {
        return new Listing<>(jobs.stream()
                .map(jobMapper::sgeJobToJob)
                .collect(Collectors.toList()));
    }

    private Predicate<SgeJob> jobFilter(final JobFilter request) {
        return job -> CollectionUtils.emptyIfNull(request.getIds())
                .contains(job.getId())
                || CollectionUtils.emptyIfNull(request.getNames())
                .contains(job.getName());
    }

    private List<SgeJob> mergeJobs(final SgeQueueListing sgeQueueListing) {
        return Stream.of(sgeQueueListing.getSgeJobs(), sgeQueueListing.getSgeQueues())
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
