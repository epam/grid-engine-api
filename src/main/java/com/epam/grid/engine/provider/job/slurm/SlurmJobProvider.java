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

package com.epam.grid.engine.provider.job.slurm;

import com.epam.grid.engine.cmd.CmdExecutor;
import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.entity.job.JobState;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.JobLogInfo;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.mapper.job.slurm.SlurmJobMapper;
import com.epam.grid.engine.provider.job.JobProvider;

import com.epam.grid.engine.provider.utils.CommandsUtils;
import com.epam.grid.engine.provider.utils.slurm.job.SacctCommandParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.apache.commons.collections4.CollectionUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.epam.grid.engine.utils.TextConstants.COLON;
import static com.epam.grid.engine.utils.TextConstants.COMMA;
import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;
import static com.epam.grid.engine.utils.TextConstants.EQUAL_SIGN;

/**
 * This class performs various actions with jobs for the SLURM engine.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SLURM")
public class SlurmJobProvider implements JobProvider {
    private static final int JOB_OUTPUT_HEADER_LINES_COUNT = 1;
    private static final String JOB_FILTER = "filter";
    private static final String SCANCEL_COMMAND = "scancel";
    private static final String SQUEUE_COMMAND = "squeue";
    private static final String SBATCH_COMMAND = "sbatch";
    private static final String OPTIONS = "options";
    private static final String LOG_DIR = "logDir";
    private static final String ENV_VARIABLES = "envVariables";
    private static final String START_TERMINATING_JOB_PREFIX = "scancel: Terminating job";
    private static final String KILL_JOB_ERROR_PREFIX = "scancel: error: Kill job error on job id";
    private static final int JOB_ID_START_POSITION = START_TERMINATING_JOB_PREFIX.length() + 1;
    private static final int ERROR_JOB_ID_POSITION = KILL_JOB_ERROR_PREFIX.length() + 1;
    private static final Pattern SUBMITTED_JOB_PATTERN = Pattern.compile("Submitted batch job (\\d+).*");

    /**
     * The MapStruct mapping mechanism used.
     */
    private final SlurmJobMapper jobMapper;

    /**
     * The command execution mechanism used.
     */
    private final SimpleCmdExecutor simpleCmdExecutor;

    /**
     * Amount of fields with job data description.
     */
    private final int fieldsCount;

    /**
     * Message, which returns when job was not found by id.
     */
    private final String jobIdNotFoundMessage;

    /**
     * An object that forms the structure of an executable command according to a template.
     */
    private final GridEngineCommandCompiler commandCompiler;

    /**
     * The path to the directory where all log files will be stored
     * occurred when processing the job.
     */
    private final String logDir;

    public SlurmJobProvider(final SlurmJobMapper jobMapper,
                            final SimpleCmdExecutor simpleCmdExecutor,
                            final GridEngineCommandCompiler commandCompiler,
                            @Value("${slurm.job.output-fields-count:52}")
                            final int fieldsCount,
                            @Value("${SLURM_JOB_NOT_FOUND_MESSAGE:slurm_load_jobs error: Invalid job id specified}")
                            final String jobIdNotFoundMessage,
                            @Value("${job.log.dir}")
                            final String logDir) {
        this.jobMapper = jobMapper;
        this.simpleCmdExecutor = simpleCmdExecutor;
        this.commandCompiler = commandCompiler;
        this.fieldsCount = fieldsCount;
        this.jobIdNotFoundMessage = jobIdNotFoundMessage;
        this.logDir = logDir;
    }

    @Override
    public EngineType getProviderType() {
        return EngineType.SLURM;
    }

    @Override
    public Listing<Job> filterJobs(final JobFilter jobFilter) {
        SacctCommandParser.filterCorrectJobIds(jobFilter);
        final CommandResult result = simpleCmdExecutor.execute(makeSqueueCommand(jobFilter));
        if (result.getExitCode() != 0 && !jobNotFoundByIdError(result)) {
            CommandsUtils.throwExecutionDetails(result);
        } else if (!result.getStdErr().isEmpty()) {
            log.warn(CommandsUtils.mergeOutputLines(result.getStdErr()));
        }
        return mapToJobListing(result.getStdOut());
    }

    @Override
    public Job runJob(final JobOptions options) {
        validateJobOptions(options);
        return buildNewJob(getResultOfExecutedCommand(simpleCmdExecutor, makeSbatchCommand(options)));
    }

    /**
     * Deletes the job being performed according to the specified parameters.
     *
     * @param deleteJobFilter Search parameters for the job being deleted.
     * @return Information about the deleted job.
     */
    @Override
    public DeletedJobInfo deleteJob(final DeleteJobFilter deleteJobFilter) {
        if (!StringUtils.hasText(deleteJobFilter.getUser()) && deleteJobFilter.getId() == null) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST,
                    String.format("Incorrect filling in %s. Either `id` or `user` should be specified for job removal!",
                            deleteJobFilter));
        }
        if (deleteJobFilter.getId() != null && deleteJobFilter.getId() <= 0) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST,
                    String.format("Id specified in %s for job removal is invalid!", deleteJobFilter));
        }

        final String jobOwner;
        if (StringUtils.hasText(deleteJobFilter.getUser())) {
            jobOwner = deleteJobFilter.getUser();
        } else {
            jobOwner = getJobById(deleteJobFilter.getId()).getOwner();
        }

        final CommandResult result = simpleCmdExecutor.execute(makeScancelCommand(deleteJobFilter));
        if (result.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(result, HttpStatus.NOT_FOUND);
        }

        final Set<String> errorDeletingJobs = result.getStdErr().stream()
                .filter((s) -> s.startsWith(KILL_JOB_ERROR_PREFIX))
                .map((s) -> s.substring(ERROR_JOB_ID_POSITION, s.indexOf(COLON, ERROR_JOB_ID_POSITION)))
                .collect(Collectors.toSet());

        final List<Long> deletedJobIds = result.getStdErr().stream()
                .filter((s) -> s.startsWith(START_TERMINATING_JOB_PREFIX))
                .map((s) -> s.substring(JOB_ID_START_POSITION))
                .filter((id) -> !errorDeletingJobs.contains(id))
                .map(Long::valueOf)
                .collect(Collectors.toList());

        if (deletedJobIds.isEmpty()) {
            CommandsUtils.throwExecutionDetails(result, HttpStatus.NOT_FOUND);
        }
        return new DeletedJobInfo(deletedJobIds, jobOwner);
    }

    @Override
    public JobLogInfo getJobLogInfo(final int jobId, final JobLogInfo.Type logType, final int lines,
                                    final boolean fromHead) {
        throw new UnsupportedOperationException("Job log info retrieving operation haven't implemented yet");
    }

    @Override
    public InputStream getJobLogFile(final int jobId, final JobLogInfo.Type logType) {
        throw new UnsupportedOperationException("Job log info file retrieving operation haven't implemented yet");
    }

    /**
     * Creates the structure of an executable command based on the passed options.
     *
     * @param options User-defined options.
     * @return The structure of an executable command.
     */
    private String[] makeSbatchCommand(final JobOptions options) {
        final Context context = new Context();
        context.setVariable(OPTIONS, options);
        context.setVariable(LOG_DIR, logDir);
        context.setVariable(ENV_VARIABLES, extractEnvVariablesFromOptions(options));
        return commandCompiler.compileCommand(getProviderType(), SBATCH_COMMAND, context);
    }

    private void validateJobOptions(final JobOptions options) {
        if (!StringUtils.hasText(options.getCommand())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Command should be specified!");
        }
        if (options.getPriority() != null && options.getPriority() < 0) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Priority should be between 0 and 4_294_967_294");
        }
        if (options.getParallelEnvOptions() != null) {
            throw new UnsupportedOperationException("Parallel environment variables are not supported yet!");
        }
        if (options.isCanBeBinary()) {
            throw new UnsupportedOperationException("Scripts from command line are not supported yet!");
        }
    }

    private String getResultOfExecutedCommand(final CmdExecutor cmdExecutor, final String[] command) {
        final CommandResult result = cmdExecutor.execute(command);
        if (result.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return parseJobId(result.getStdOut().get(0));
    }

    /**
     * Gets the job ID from the string.
     *
     * @param jobString A string that can contain the job ID.
     * @return JobID or an empty string.
     */
    private String parseJobId(final String jobString) {
        final Matcher matcher = SUBMITTED_JOB_PATTERN.matcher(jobString);
        return matcher.find()
                ? matcher.group(1)
                : EMPTY_STRING;
    }

    private String extractEnvVariablesFromOptions(final JobOptions options) {
        return getVariablesFromMap(MapUtils.emptyIfNull(options.getEnvVariables()));
    }

    private String getVariablesFromMap(final Map<String, String> varMap) {
        return varMap.entrySet().stream()
                .map(this::convertEnvVarToString)
                .collect(Collectors.joining(COMMA));
    }

    private String convertEnvVarToString(final Map.Entry<String, String> entry) {
        final String value = entry.getValue();
        if (StringUtils.hasText(value)) {
            return entry.getKey() + EQUAL_SIGN + value;
        }
        return entry.getKey();
    }

    private Job buildNewJob(final String id) {
        return Job.builder()
                .id(Integer.parseInt(id))
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .build())
                .build();
    }

    /**
     * Creates the structure of an executable command based on the passed filter.
     *
     * @param filter User-defined filter for selecting job data.
     * @return The structure of an executable command.
     */
    private String[] makeSqueueCommand(final JobFilter filter) {
        final Context context = new Context();
        context.setVariable(JOB_FILTER, filter);
        return commandCompiler.compileCommand(getProviderType(), SQUEUE_COMMAND, context);
    }

    private Listing<Job> mapToJobListing(final List<String> stdOut) {
        if (stdOut.size() > JOB_OUTPUT_HEADER_LINES_COUNT) {
            return new Listing<>(stdOut.stream()
                    .skip(JOB_OUTPUT_HEADER_LINES_COUNT)
                    .map(jobDataList -> SacctCommandParser.parseSlurmJob(jobDataList, fieldsCount))
                    .filter(CollectionUtils::isNotEmpty)
                    .map(SacctCommandParser::mapJobDataToSlurmJob)
                    .map(jobMapper::slurmJobToJob)
                    .collect(Collectors.toList()));
        }
        return new Listing<>();
    }

    private boolean jobNotFoundByIdError(final CommandResult result) {
        return ListUtils.emptyIfNull(result.getStdErr()).stream()
                .anyMatch(s -> !s.equals(jobIdNotFoundMessage));
    }

    /**
     * This method creates the structure of the executed job deletion command based on the passed options.
     *
     * @param filter User-defined conditions for searching for deleted jobs.
     * @return The structure of an executable command.
     */
    private String[] makeScancelCommand(final DeleteJobFilter filter) {
        final Context context = new Context();
        context.setVariable(JOB_FILTER, filter);
        return commandCompiler.compileCommand(getProviderType(), SCANCEL_COMMAND, context);
    }

    private Job getJobById(final Long id) {
        final JobFilter jobFilter = JobFilter.builder()
                .ids(Collections.singletonList(id))
                .build();

        return ListUtils.emptyIfNull(filterJobs(jobFilter).getElements()).stream()
                .findFirst()
                .orElseThrow(() -> new GridEngineException(HttpStatus.NOT_FOUND,
                        String.format("Id specified in %d for job removal not found!", id)));
    }
}
