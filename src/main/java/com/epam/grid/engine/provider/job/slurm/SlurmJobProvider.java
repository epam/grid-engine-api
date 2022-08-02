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

import com.epam.grid.engine.cmd.CommandArgUtils;
import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.entity.job.JobState;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.ParallelExecutionOptions;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.mapper.job.slurm.SlurmJobMapper;
import com.epam.grid.engine.provider.job.JobProvider;

import com.epam.grid.engine.provider.utils.CommandsUtils;
import com.epam.grid.engine.provider.utils.slurm.job.SacctCommandParser;
import com.epam.grid.engine.utils.TextConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class performs various actions with jobs for the SLURM engine.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SLURM")
public class SlurmJobProvider implements JobProvider {
    private static final int JOB_OUTPUT_HEADER_LINES_COUNT = 1;
    private static final long MAX_SENT_PRIORITY = 4_294_967_294L;
    private static final String JOB_FILTER = "filter";
    private static final String SCANCEL_COMMAND = "scancel";
    private static final String SQUEUE_COMMAND = "squeue";
    private static final String SBATCH_COMMAND = "sbatch";
    private static final String ARGUMENTS = "arguments";
    private static final String BINARY_COMMAND = "binaryCommand";
    private static final String OPTIONS = "options";
    private static final String LOG_DIR = "logDir";
    private static final String ENV_VARIABLES = "envVariables";
    private static final String START_TERMINATING_JOB_PREFIX = "scancel: Terminating job";
    private static final String KILL_JOB_ERROR_PREFIX = "scancel: error: Kill job error on job id";
    private static final int JOB_ID_START_POSITION = START_TERMINATING_JOB_PREFIX.length() + 1;
    private static final int ERROR_JOB_ID_POSITION = KILL_JOB_ERROR_PREFIX.length() + 1;
    private static final String JOBS_DELETING_EXECUTION_RESULT = "Jobs deleting result: ";
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
     * An object that forms the structure of an executable command according to a template.
     */
    private final GridEngineCommandCompiler commandCompiler;

    public SlurmJobProvider(final SlurmJobMapper jobMapper,
                            final SimpleCmdExecutor simpleCmdExecutor,
                            final GridEngineCommandCompiler commandCompiler) {
        this.jobMapper = jobMapper;
        this.simpleCmdExecutor = simpleCmdExecutor;
        this.commandCompiler = commandCompiler;
    }

    @Override
    public CommandType getProviderType() {
        return CommandType.SLURM;
    }

    @Override
    public Listing<Job> filterJobs(final JobFilter jobFilter) {
        SacctCommandParser.filterCorrectJobIds(jobFilter);
        final CommandResult result = simpleCmdExecutor.execute(makeSqueueCommand(jobFilter));
        if (result.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(result);
        } else if (!result.getStdErr().isEmpty()) {
            log.warn(CommandsUtils.mergeOutputLines(result.getStdErr()));
        }
        return mapToJobListing(result.getStdOut());
    }

    @Override
    public Job runJob(final JobOptions options, final String logDir) {
        if (options.getParallelEnvOptions() != null) {
            throw new UnsupportedOperationException("Unsupported option was specified, for SLURM engine please "
                    + "use ParallelExecutionOptions");
        }
        if (checkParallelExecutionOptions(options.getParallelExecutionOptions())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "All Parallel execution options except Exclusive "
                    + "should be greater than 0!");
        }
        if (options.getPriority() != null && (options.getPriority() < 0 || options.getPriority() > MAX_SENT_PRIORITY)) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Priority should be between 0 and 4_294_967_294");
        }
        final CommandResult result = simpleCmdExecutor.execute(makeSbatchCommand(options, logDir));
        if (result.getExitCode() != 0 || result.getStdOut().isEmpty()) {
            CommandsUtils.throwExecutionDetails(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        final Matcher matcher = SUBMITTED_JOB_PATTERN.matcher(result.getStdOut().get(0));
        if (!matcher.find()) {
            CommandsUtils.throwExecutionDetails(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return Job.builder()
                .id(Long.parseLong(matcher.group(1)))
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .build())
                .build();
    }

    /**
     * Deletes the job being performed according to the specified parameters.
     * <p>note: the {@link DeleteJobFilter} argument must contain only an owner's name or list of job ids.</p>
     *
     * @param deleteJobFilter Search parameters for the job being deleted.
     * @return Information about deleted jobs.
     */
    @Override
    public Listing<DeletedJobInfo> deleteJob(final DeleteJobFilter deleteJobFilter) {
        final Map<Long, String> jobOwners = getJobOwners(deleteJobFilter);
        if (jobOwners.isEmpty()) {
            throw new GridEngineException(HttpStatus.NOT_FOUND,
                    String.format("No jobs found from the specified %s to remove!", deleteJobFilter));
        }

        final CommandResult result = simpleCmdExecutor.execute(makeScancelCommand(deleteJobFilter));
        if (result.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(result, HttpStatus.NOT_FOUND);
        }

        final Set<String> errorDeletingJobs = result.getStdErr().stream()
                .filter((s) -> s.startsWith(KILL_JOB_ERROR_PREFIX))
                .map((s) -> s.substring(ERROR_JOB_ID_POSITION, s.indexOf(TextConstants.COLON, ERROR_JOB_ID_POSITION)))
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
        if (deletedJobIds.size() < jobOwners.size()) {
            log.warn(JOBS_DELETING_EXECUTION_RESULT + result);
        }
        return new Listing<>(deletedJobIds.stream()
                .map(id -> new DeletedJobInfo(id, jobOwners.get(id)))
                .collect(Collectors.toList()));
    }

    /**
     * Creates the structure of an executable command based on the passed options.
     *
     * @param options User-defined options.
     * @param logDir  the path to the directory where all log files will be stored
     *                occurred when processing the job.
     * @return The structure of an executable command.
     */
    private String[] makeSbatchCommand(final JobOptions options, final String logDir) {
        final Context context = new Context();
        context.setVariable(OPTIONS, options);
        context.setVariable(LOG_DIR, logDir);
        context.setVariable(ENV_VARIABLES, CommandArgUtils.envVariablesMapToString(options.getEnvVariables()));

        if (options.isCanBeBinary()) {
            final String binaryCommandArguments = options.getArguments().stream()
                    .map(CommandArgUtils::toEncloseInQuotes)
                    .map(CommandArgUtils::toEscapeQuotes)
                    .collect(Collectors.joining(TextConstants.SPACE));
            context.setVariable(BINARY_COMMAND, options.getCommand() + TextConstants.SPACE + binaryCommandArguments);
        } else {
            context.setVariable(ARGUMENTS, CommandArgUtils.toEscapeQuotes(options.getArguments()));
        }
        return commandCompiler.compileCommand(getProviderType(), SBATCH_COMMAND, context);
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
                    .map(SacctCommandParser::parseSlurmJob)
                    .filter(CollectionUtils::isNotEmpty)
                    .map(SacctCommandParser::mapJobDataToSlurmJob)
                    .map(jobMapper::slurmJobToJob)
                    .collect(Collectors.toList()));
        }
        return new Listing<>();
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

    private Map<Long, String> getJobOwners(final DeleteJobFilter deleteJobFilter) {
        final JobFilter jobFilter = new JobFilter();
        if (StringUtils.hasText(deleteJobFilter.getUser())) {
            jobFilter.setOwners(List.of(deleteJobFilter.getUser()));
        } else {
            jobFilter.setIds(deleteJobFilter.getIds());
        }
        return ListUtils.emptyIfNull(filterJobs(jobFilter).getElements()).stream()
                .collect(Collectors.toMap(Job::getId, Job::getOwner));
    }

    private boolean checkParallelExecutionOptions(final ParallelExecutionOptions parallelExecutionOptions) {
        return parallelExecutionOptions != null
                && (isNotGreaterThanZero(parallelExecutionOptions.getNumTasks())
                || isNotGreaterThanZero(parallelExecutionOptions.getNodes())
                || isNotGreaterThanZero(parallelExecutionOptions.getCpusPerTask())
                || isNotGreaterThanZero(parallelExecutionOptions.getNumTasksPerNode()));
    }

    private boolean isNotGreaterThanZero(final int intToCheck) {
        return intToCheck < 1;
    }
}
