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
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.ParallelEnvOptions;
import com.epam.grid.engine.entity.job.sge.SgeJob;
import com.epam.grid.engine.entity.job.sge.SgeQueueListing;
import com.epam.grid.engine.mapper.job.sge.SgeJobMapper;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.job.JobProvider;
import com.epam.grid.engine.provider.utils.JaxbUtils;
import com.epam.grid.engine.provider.utils.sge.job.QstatCommandParser;
import com.epam.grid.engine.provider.utils.CommandsUtils;
import com.epam.grid.engine.utils.TextConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.CommandsUtils.mergeOutputLines;

/**
 * This class performs various actions with jobs for the SGE engine.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SGE")
public class SgeJobProvider implements JobProvider {

    private static final int PE_MIN_VALUE = 1;
    private static final int PE_MAX_VALUE = 9_999_999;
    private static final String JOB_FILTER = "filter";
    private static final String JOB_STATE = "state";
    private static final String QDEL_COMMAND = "qdel";
    private static final String QSTAT_COMMAND = "qstat";
    private static final String QSUB_COMMAND = "qsub";
    private static final String ARGUMENTS = "arguments";
    private static final String OPTIONS = "options";
    private static final String LOG_DIR = "logDir";
    private static final String ENV_VARIABLES = "envVariables";
    private static final String JOBS_DELETING_EXECUTION_RESULT = "Jobs deleting result: ";
    private static final Pattern SUBMITTED_JOB_ID_PATTERN = Pattern.compile("Your job (\\d+).* has been submitted");
    private static final Pattern DELETED_JOB_ID_PATTERN = Pattern.compile(".*has deleted job (\\d+).*");

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

    public SgeJobProvider(final SgeJobMapper jobMapper,
                          final SimpleCmdExecutor simpleCmdExecutor,
                          final GridEngineCommandCompiler commandCompiler) {
        this.jobMapper = jobMapper;
        this.simpleCmdExecutor = simpleCmdExecutor;
        this.commandCompiler = commandCompiler;
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
        }
        if (!result.getStdErr().isEmpty()) {
            log.warn(result.getStdErr().toString());
        }
        return mapJobs(JaxbUtils.unmarshall(String.join(TextConstants.NEW_LINE_DELIMITER, result.getStdOut()),
                SgeQueueListing.class), jobFilter);
    }

    /**
     * Gets the type of the executed engine.
     *
     * @return The type of engine being executed.
     */
    @Override
    public CommandType getProviderType() {
        return CommandType.SGE;
    }

    /**
     * Launches the job with the specified parameters.
     *
     * @param options Parameters for launching the job.
     * @param logDir  the path to the directory where all log files will be stored
     *                occurred when processing the job.
     * @return Launched job.
     */
    @Override
    public Job runJob(final JobOptions options, final String logDir) {
        if (options.getParallelExecutionOptions() != null) {
            throw new UnsupportedOperationException("ParallelExecutionOptions can be used only for SLURM grid engine. "
                    + "For SGE engine please use ParallelEnvOptions");
        }
        if (!isValidParallelEnvOptions(options.getParallelEnvOptions())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Invalid PE specification!");
        }
        final CommandResult result = simpleCmdExecutor.execute(makeQsubCommand(options, logDir));
        if (result.getExitCode() != 0 || result.getStdOut().isEmpty()) {
            CommandsUtils.throwExecutionDetails(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        final Matcher matcher = SUBMITTED_JOB_ID_PATTERN.matcher(result.getStdOut().get(0));
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
     * Deletes jobs being performed according to the specified parameters.
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
        final CommandResult result = simpleCmdExecutor.execute(makeQdelCommand(deleteJobFilter));
        final List<Long> deletedJobIds = parseDeletedJobId(result.getStdOut());

        if (result.getExitCode() != 0) {
            if (deletedJobIds.isEmpty()) {
                CommandsUtils.throwExecutionDetails(result, HttpStatus.NOT_FOUND);
            } else {
                log.warn(JOBS_DELETING_EXECUTION_RESULT + result);
            }
        }
        return new Listing<>(deletedJobIds.stream()
                .map(id -> new DeletedJobInfo(id, jobOwners.get(id)))
                .collect(Collectors.toList()));
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
     * @param logDir  the path to the directory where all log files will be stored
     *                occurred when processing the job.
     * @return The structure of an executable command.
     */
    private String[] makeQsubCommand(final JobOptions options, final String logDir) {
        final Context context = new Context();
        context.setVariable(OPTIONS, options);
        context.setVariable(LOG_DIR, logDir);
        context.setVariable(ARGUMENTS, CommandArgUtils.toEscapeQuotes(options.getArguments()));
        context.setVariable(ENV_VARIABLES, CommandArgUtils.envVariablesMapToString(options.getEnvVariables()));
        return commandCompiler.compileCommand(getProviderType(), QSUB_COMMAND, context);
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

    private boolean isValidParallelEnvOptions(final ParallelEnvOptions options) {
        return options == null
                || StringUtils.hasText(options.getName())
                && !(options.getMin() > options.getMax()
                || options.getMin() < PE_MIN_VALUE || options.getMax() < PE_MIN_VALUE
                || options.getMin() > PE_MAX_VALUE || options.getMax() > PE_MAX_VALUE);
    }

    private List<Long> parseDeletedJobId(final List<String> stdOut) {
        return DELETED_JOB_ID_PATTERN.matcher(mergeOutputLines(stdOut))
                .results()
                .map(matchResult -> matchResult.group(1).trim())
                .map(Long::valueOf)
                .collect(Collectors.toList());
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
}
