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

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.entity.job.JobState;
import com.epam.grid.engine.entity.job.ParallelEnvOptions;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.thymeleaf.context.IContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(properties = {"grid.engine.type=SLURM"})
public class SlurmJobProviderTest {

    private static final String SLURM_USER = "root";
    private static final String SOME_USER_NAME = "user";

    private static final String SQUEUE_COMMAND = "squeue";
    private static final String ALL_FORMAT = "-o %all";
    private static final String OWNER_FILTRATION_KEY = "-u";
    private static final String STATE_FILTRATION_KEY = "-t";
    private static final String JOB_LIST_KEY = "-j";
    private static final String JOB_NAME_FILTRATION_KEY = "-n";
    private static final String PENDING_STATUS_CODE = "PD";
    private static final String RUNNING_STATUS_CODE = "R";
    private static final String SUSPENDED_STATUS_CODE = "S";
    private static final String SQUEUE_JOB_LOADING_ERROR_STRING = "slurm_load_jobs error: Invalid job id specified";

    private static final String RUNNING_STRING = "RUNNING";
    private static final String PENDING_STRING = "PENDING";
    private static final String SUSPENDED_STRING = "SUSPENDED";
    private static final String TEST_QUEUE = "normal";

    private static final String JOB_NAME1 = "test1.sh";
    private static final String JOB_NAME2 = "test2.sh";
    private static final String JOB_NAME3 = "test3.sh";
    private static final String JOB_PRIORITY1 = "0.99998474121093";
    private static final String JOB_PRIORITY2 = "0.99998474074527";
    private static final String JOB_PRIORITY3 = "0.00000000000000";
    private static final long SOME_WRONG_SENT_PRIORITY = -10L;
    private static final long SECOND_WRONG_SENT_PRIORITY = 4_294_967_295L;

    private static final long SOME_CORRECT_JOB_ID = 5L;
    private static final long SECOND_CORRECT_JOB_ID = 10L;
    private static final long THIRD_CORRECT_JOB_ID = 15L;
    private static final String SOME_CORRECT_JOB_ID_STRING = Long.toString(SOME_CORRECT_JOB_ID);
    private static final long SOME_WRONG_JOB_ID = 0L;

    private static final String BINARY_COMMAND = "binaryCommand";
    private static final String SOME_BINARY_COMMAND = "echo";
    private static final String SOME_ARGUMENT = "someArgument";
    private static final String SOME_ARGUMENT_WITH_SPACES = "some argument with spaces";
    private static final String SOME_JOB_IS_SUBMITTED = "Submitted batch job " + SOME_CORRECT_JOB_ID_STRING;

    private static final List<String> EMPTY_LIST = Collections.EMPTY_LIST;
    private static final String SQUEUE_COMMAND_EXECUTION_HEADER = "ACCOUNT|TRES_PER_NODE|MIN_CPUS|MIN_TMP_DISK|"
            + "END_TIME|FEATURES|GROUP|OVER_SUBSCRIBE|JOBID|NAME|COMMENT|TIME_LIMIT|MIN_MEMORY|REQ_NODES|"
            + "COMMAND|PRIORITY|QOS|REASON||ST|USER|RESERVATION|WCKEY|EXC_NODES|NICE|S:C:T|JOBID|EXEC_HOST|"
            + "CPUS|NODES|DEPENDENCY|ARRAY_JOB_ID|GROUP|SOCKETS_PER_NODE|CORES_PER_SOCKET|THREADS_PER_CORE|"
            + "ARRAY_TASK_ID|TIME_LEFT|TIME|NODELIST|CONTIGUOUS|PARTITION|PRIORITY|NODELIST(REASON)|START_TIME|"
            + "STATE|UID|SUBMIT_TIME|LICENSES|CORE_SPEC|SCHEDNODES|WORK_DIR";

    private static final String JOB_STDOUT_STRING_FORMAT_TEMPLATE = "(null)|N/A|1|0|2022-05-18T10:29:10"
            + "|(null)|root|OK|%d|%s|(null)|5-00:00:00|500M||/data/test.sh|%s|normal|None||%s|%s|(null)"
            + "|(null)||0|*:*:*|2|worker1|1|1||2|0|*|*|*|N/A|4-21:35:49|2:24:11|worker1|0|%s|4294901759"
            + "|worker1|2022-05-13T10:29:10|%s|0|%s|(null)|N/A|(null)|/";

    private static final String RUNNING_JOB_SUBMIT_TIME = "2022-05-13T10:29:09";
    private static final String RUNNING_JOB_STDOUT = String.format(JOB_STDOUT_STRING_FORMAT_TEMPLATE,
            SOME_CORRECT_JOB_ID, JOB_NAME1, JOB_PRIORITY1, RUNNING_STATUS_CODE, SLURM_USER,
            TEST_QUEUE, RUNNING_STRING, RUNNING_JOB_SUBMIT_TIME);

    private static final String PENDING_JOB_SUBMIT_TIME = "2022-05-15T08:03:42";
    private static final String PENDING_JOB_STDOUT = String.format(JOB_STDOUT_STRING_FORMAT_TEMPLATE,
            SECOND_CORRECT_JOB_ID, JOB_NAME2, JOB_PRIORITY2, PENDING_STATUS_CODE, SOME_USER_NAME,
            TEST_QUEUE, PENDING_STRING, PENDING_JOB_SUBMIT_TIME);

    private static final String SUSPENDED_JOB_SUBMIT_TIME = "2022-05-15T08:03:38";
    private static final String SUSPENDED_JOB_STDOUT = String.format(JOB_STDOUT_STRING_FORMAT_TEMPLATE,
            THIRD_CORRECT_JOB_ID, JOB_NAME3, JOB_PRIORITY3, SUSPENDED_STATUS_CODE, SLURM_USER,
            TEST_QUEUE, SUSPENDED_STRING, SUSPENDED_JOB_SUBMIT_TIME);

    private static final List<String> VALID_STDOUT = List.of(SQUEUE_COMMAND_EXECUTION_HEADER, RUNNING_JOB_STDOUT);
    private static final List<String> TWO_VALID_JOBS_STDOUT = List.of(SQUEUE_COMMAND_EXECUTION_HEADER,
            PENDING_JOB_STDOUT,
            RUNNING_JOB_STDOUT);

    private static final List<String> INVALID_OUTPUT = List.of(SQUEUE_COMMAND_EXECUTION_HEADER,
            "(null)|N/A|1|0|2022-05-18T10:29:10|(null)|root|OK|2|test.sh|(null)|5-00:00:00|500M||/data/test.sh");

    private static final String TEXT_JOB_SUBMITTED = "Submitted batch job " + SOME_CORRECT_JOB_ID_STRING;
    private static final String SBATCH_COMMAND = "sbatch";
    private static final String ENV_VAR_KEY = "parameter1";
    private static final String ENV_VAR_VALUE = "some value with spaces";
    private static final String ENV_VAR_COMMAND_ARG = String.format("%s=%s", ENV_VAR_KEY, ENV_VAR_VALUE);
    private static final String JOB_PRIORITY4 = "9999";
    private static final String JOB_PARTITION = "normal";
    private static final String JOB_WORK_DIR = "/data/";

    private static final String SCANCEL_COMMAND = "scancel";
    private static final String VERBOSE_KEY = "-v";
    private static final String TERMINATING_JOB_PREFIX = "scancel: Terminating job ";
    private static final String ERROR_DELETING_STRING_TEMPLATE =
            "scancel: error: Kill job error on job id %s: some reason";
    private static final List<String> DELETE_ALL_USER_JOBS_STDOUT =
            List.of("scancel: Consumable Resources (CR) Node Selection plugin loaded with argument 17",
                    "scancel: Cray/Aries node selection plugin loaded",
                    "scancel: Linear node selection plugin loaded with argument 17",
                    "scancel: select/cons_tres loaded with argument 17",
                    TERMINATING_JOB_PREFIX + SOME_CORRECT_JOB_ID_STRING,
                    String.format(ERROR_DELETING_STRING_TEMPLATE, SOME_CORRECT_JOB_ID_STRING),
                    TERMINATING_JOB_PREFIX + SECOND_CORRECT_JOB_ID);

    @Autowired
    private SlurmJobProvider slurmJobProvider;

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @MockBean
    private GridEngineCommandCompiler mockCommandCompiler;

    @Test
    public void failsWithInvalidOutput() {
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(INVALID_OUTPUT)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT);
        Assertions.assertEquals(EngineType.SLURM, slurmJobProvider.getProviderType());
        final JobFilter jobFilter = new JobFilter();
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                slurmJobProvider.filterJobs(jobFilter));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    public void shouldNotFailWithEmptyJobList() {
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(List.of(SQUEUE_COMMAND_EXECUTION_HEADER))
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT);
        final List<Job> result = slurmJobProvider.filterJobs(new JobFilter()).getElements();

        Assertions.assertNull(result);
        Assertions.assertEquals(EngineType.SLURM, slurmJobProvider.getProviderType());
    }

    @Test
    public void correctPendingAndRunningJobs() {
        final Job pendingJob = pendingJobTemplate();
        final Job runningJob = runningJobTemplate();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(TWO_VALID_JOBS_STDOUT)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT);
        final Listing<Job> result = slurmJobProvider.filterJobs(new JobFilter());

        Assertions.assertEquals(2, result.getElements().size());
        Assertions.assertEquals(pendingJob, result.getElements().get(0));
        Assertions.assertEquals(runningJob, result.getElements().get(1));
    }

    @Test
    public void shouldReturnCorrectOwnerFiltration() {
        final Job runningJob = runningJobTemplate();
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setOwners(Collections.singletonList(SLURM_USER));
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(VALID_STDOUT)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT, OWNER_FILTRATION_KEY, SLURM_USER);
        final Listing<Job> result = slurmJobProvider.filterJobs(jobFilter);

        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(runningJob, result.getElements().get(0));
    }

    @Test
    public void shouldReturnCorrectStateFiltration() {
        final Job runningJob = runningJobTemplate();
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setState(RUNNING_STRING);
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(VALID_STDOUT)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT, STATE_FILTRATION_KEY, SLURM_USER);
        final Listing<Job> result = slurmJobProvider.filterJobs(jobFilter);

        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(runningJob, result.getElements().get(0));
    }

    @Test
    public void shouldReturnCorrectIdFiltration() {
        final Job runningJob = runningJobTemplate();
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setIds(List.of(SOME_CORRECT_JOB_ID));
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(VALID_STDOUT)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT, JOB_LIST_KEY, SOME_CORRECT_JOB_ID_STRING);
        final Listing<Job> result = slurmJobProvider.filterJobs(jobFilter);

        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(runningJob, result.getElements().get(0));
    }

    @Test
    public void shouldReturnCorrectNameFiltration() {
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setNames(Collections.singletonList(JOB_NAME1));
        final Job runningJob = runningJobTemplate();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(VALID_STDOUT)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT, JOB_NAME_FILTRATION_KEY, JOB_NAME1);
        final Listing<Job> result = slurmJobProvider.filterJobs(jobFilter);

        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(runningJob, result.getElements().get(0));
    }

    @Test
    public void shouldFailWithException() {
        final JobFilter jobFilter = new JobFilter();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(EMPTY_LIST)
                .exitCode(1)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT);
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class,
                () -> slurmJobProvider.filterJobs(jobFilter));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideCasesWithFiltrationByState")
    public void shouldReturnCorrectJobWhenFiltrationByState(final String jobState, final String stdOutJobString,
                                                            final Job expectedJob) {
        final CommandResult squeueCommandResult = CommandResult.builder()
                .stdOut(List.of(SQUEUE_COMMAND_EXECUTION_HEADER, stdOutJobString))
                .stdErr(EMPTY_LIST)
                .build();
        mockCommandCompilation(SQUEUE_COMMAND, squeueCommandResult, ALL_FORMAT, STATE_FILTRATION_KEY, jobState);

        final JobFilter jobFilter = JobFilter.builder().state(jobState).build();
        final Listing<Job> result = slurmJobProvider.filterJobs(jobFilter);

        Assertions.assertEquals(expectedJob, result.getElements().get(0));
    }

    static Stream<Arguments> provideCasesWithFiltrationByState() {
        return Stream.of(
                Arguments.of(RUNNING_STRING, RUNNING_JOB_STDOUT, runningJobTemplate()),
                Arguments.of(PENDING_STRING, PENDING_JOB_STDOUT, pendingJobTemplate()),
                Arguments.of(SUSPENDED_STRING, SUSPENDED_JOB_STDOUT, suspendedJobTemplate())
        );
    }

    private void mockCommandCompilation(final String command, final CommandResult commandResult,
                                        final String... compiledArray) {
        doReturn(compiledArray).when(mockCommandCompiler)
                .compileCommand(Mockito.eq(EngineType.SLURM), Mockito.matches(command), Mockito.any());
        doReturn(commandResult).when(mockCmdExecutor).execute(compiledArray);
    }

    private static Job runningJobTemplate() {
        return Job.builder()
                .id(SOME_CORRECT_JOB_ID)
                .name(JOB_NAME1)
                .priority(Double.parseDouble(JOB_PRIORITY1))
                .owner(SLURM_USER)
                .queueName(TEST_QUEUE)
                .submissionTime(LocalDateTime.parse(RUNNING_JOB_SUBMIT_TIME))
                .state(JobState.builder()
                        .category(JobState.Category.RUNNING)
                        .state(RUNNING_STRING)
                        .stateCode(RUNNING_STATUS_CODE).build())
                .build();
    }

    private static Job pendingJobTemplate() {
        return Job.builder()
                .id(SECOND_CORRECT_JOB_ID)
                .name(JOB_NAME2)
                .priority(Double.parseDouble(JOB_PRIORITY2))
                .owner(SOME_USER_NAME)
                .queueName(TEST_QUEUE)
                .submissionTime(LocalDateTime.parse(PENDING_JOB_SUBMIT_TIME))
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .state(PENDING_STRING)
                        .stateCode(PENDING_STATUS_CODE).build())
                .build();
    }

    private static Job suspendedJobTemplate() {
        return Job.builder()
                .id(THIRD_CORRECT_JOB_ID)
                .name(JOB_NAME3)
                .priority(Double.parseDouble(JOB_PRIORITY3))
                .owner(SLURM_USER)
                .queueName(TEST_QUEUE)
                .submissionTime(LocalDateTime.parse(SUSPENDED_JOB_SUBMIT_TIME))
                .state(JobState.builder()
                        .category(JobState.Category.SUSPENDED)
                        .state(SUSPENDED_STRING)
                        .stateCode(SUSPENDED_STATUS_CODE).build())
                .build();
    }

    @Test
    public void shouldPassValidContextToCommandEngineWhenBinaryCommandSubmitting() {
        final JobOptions testJobOptions = JobOptions.builder()
                .canBeBinary(true)
                .command(SOME_BINARY_COMMAND)
                .arguments(List.of(SOME_ARGUMENT, SOME_ARGUMENT_WITH_SPACES))
                .build();

        final String binaryContextArgument = String.format("%s \\\"%s\\\" \\\"%s\\\"", SOME_BINARY_COMMAND,
                SOME_ARGUMENT, SOME_ARGUMENT_WITH_SPACES);

        final CommandResult commandResult = new CommandResult(List.of(SOME_JOB_IS_SUBMITTED), 0, EMPTY_LIST);
        doReturn(commandResult).when(mockCmdExecutor).execute(Mockito.any());

        Assertions.assertDoesNotThrow(() -> slurmJobProvider.runJob(testJobOptions));

        final ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        final ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockCommandCompiler).compileCommand(Mockito.any(), commandCaptor.capture(),
                contextCaptor.capture());

        Assertions.assertEquals(SBATCH_COMMAND, commandCaptor.getValue());
        Assertions.assertEquals(binaryContextArgument, contextCaptor.getValue().getVariable(BINARY_COMMAND));
    }



    @ParameterizedTest
    @MethodSource("provideInvalidJobOptions")
    public void shouldThrowWhenPassedIllegalJobOptionsToJobSubmitting(final JobOptions jobOptions) {
        Assertions.assertThrows(GridEngineException.class, () -> slurmJobProvider.runJob(jobOptions));
    }

    static Stream<Arguments> provideInvalidJobOptions() {
        return Stream.of(
                Arguments.of(new JobOptions()),
                Arguments.of(JobOptions.builder().command(null).build()),
                Arguments.of(JobOptions.builder().command(EMPTY_STRING).build()),
                Arguments.of(JobOptions.builder().priority(SOME_WRONG_SENT_PRIORITY).command(JOB_NAME1).build()),
                Arguments.of(JobOptions.builder().priority(SECOND_WRONG_SENT_PRIORITY).command(JOB_NAME1).build())
        );
    }

    @Test
    public void shouldThrowUnsupportedExceptionWhenPassedPeOptionsToJobSubmitting() {
        final JobOptions jobOptions = JobOptions.builder().command(JOB_NAME1)
                .parallelEnvOptions(new ParallelEnvOptions(EMPTY_STRING, 1, 10))
                .build();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> slurmJobProvider.runJob(jobOptions));
    }

    @ParameterizedTest
    @MethodSource("provideCorrectSbatchCommands")
    public void shouldReturnScheduledJobIndex(final String[] command) {
        final Job expectedFilteredJob = correctBuild();
        final JobOptions jobOptions = new JobOptions();
        final CommandResult commandResult = new CommandResult();

        jobOptions.setCommand(JOB_NAME1);
        commandResult.setStdOut(Collections.singletonList(TEXT_JOB_SUBMITTED));
        commandResult.setStdErr(EMPTY_LIST);

        mockCommandCompilation(SBATCH_COMMAND, commandResult, command);
        final Job result = slurmJobProvider.runJob(jobOptions);

        Assertions.assertEquals(expectedFilteredJob.getId(), result.getId());
    }

    static Stream<Arguments> provideCorrectSbatchCommands() {
        return Stream.of(
                    new String[]{SBATCH_COMMAND, "--export=", ENV_VAR_COMMAND_ARG, JOB_NAME1},
                    new String[]{SBATCH_COMMAND, "--priority=", JOB_PRIORITY4, JOB_NAME1},
                    new String[]{SBATCH_COMMAND, "-J", JOB_NAME3, JOB_NAME1},
                    new String[]{SBATCH_COMMAND, "--partition=", JOB_PARTITION, JOB_NAME1},
                    new String[]{SBATCH_COMMAND, "-D", JOB_WORK_DIR, JOB_NAME1})
                .map((t) -> (Object) t)
                .map(Arguments::of);
    }

    @Test
    public void shouldReturnCorrectJob() {
        final Job expectedFilteredJob = correctBuild();
        final JobOptions jobOptions = new JobOptions();
        final CommandResult commandResult = new CommandResult();
        jobOptions.setCommand(JOB_NAME1);

        commandResult.setStdOut(Collections.singletonList(TEXT_JOB_SUBMITTED));
        commandResult.setStdErr(EMPTY_LIST);

        mockCommandCompilation(SBATCH_COMMAND, commandResult, SBATCH_COMMAND, JOB_NAME1);
        final Job result = slurmJobProvider.runJob(jobOptions);

        Assertions.assertEquals(expectedFilteredJob, result);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {SOME_WRONG_JOB_ID})
    public void shouldThrowWhenPassWrongRequestToDeleteJob(final Long jobId) {
        final DeleteJobFilter deleteJobFilter = new DeleteJobFilter(false, jobId, null);
        Assertions.assertThrows(GridEngineException.class, () -> slurmJobProvider.deleteJob(deleteJobFilter));
    }

    @ParameterizedTest
    @MethodSource("provideValidNameCasesForRequestsToDeleteJob")
    public void shouldReturnCorrectUserNameAndJobIdWhenDeleteJob(final String expectedUserName,
                                                                 final String passedUserName,
                                                                 final String... commandArgs) {
        final CommandResult squeueCommandResult = CommandResult.builder()
                .stdOut(VALID_STDOUT)
                .stdErr(EMPTY_LIST)
                .build();
        mockCommandCompilation(SQUEUE_COMMAND, squeueCommandResult, ALL_FORMAT,
                JOB_LIST_KEY, SOME_CORRECT_JOB_ID_STRING);

        final CommandResult scancelCommandResult = new CommandResult(EMPTY_LIST, 0,
                Collections.singletonList(TERMINATING_JOB_PREFIX + SOME_CORRECT_JOB_ID_STRING));
        mockCommandCompilation(SCANCEL_COMMAND, scancelCommandResult, commandArgs);

        final DeleteJobFilter testDeleteJobFilter = new DeleteJobFilter(false, SOME_CORRECT_JOB_ID, passedUserName);
        final DeletedJobInfo deletedJobInfoResult = slurmJobProvider.deleteJob(testDeleteJobFilter);
        Assertions.assertEquals(expectedUserName, deletedJobInfoResult.getUser());
        Assertions.assertEquals(Collections.singletonList(SOME_CORRECT_JOB_ID), deletedJobInfoResult.getIds());
    }

    static Stream<Arguments> provideValidNameCasesForRequestsToDeleteJob() {
        return Stream.of(
                Arguments.of(SLURM_USER, null,
                    new String[] {SCANCEL_COMMAND, VERBOSE_KEY, SOME_CORRECT_JOB_ID_STRING}),
                Arguments.of(SOME_USER_NAME, SOME_USER_NAME,
                    new String[] {SCANCEL_COMMAND, VERBOSE_KEY, OWNER_FILTRATION_KEY,
                                  SOME_USER_NAME, SOME_CORRECT_JOB_ID_STRING})
        );
    }

    @Test
    public void shouldReturnCorrectListDeletedJobs() {
        final CommandResult scancelCommandResult = new CommandResult(EMPTY_LIST, 0, DELETE_ALL_USER_JOBS_STDOUT);
        mockCommandCompilation(SCANCEL_COMMAND, scancelCommandResult, SCANCEL_COMMAND, VERBOSE_KEY,
                OWNER_FILTRATION_KEY, SOME_USER_NAME);

        final DeleteJobFilter testDeleteJobFilter = new DeleteJobFilter(false, null, SOME_USER_NAME);
        final DeletedJobInfo deletedJobInfoResult = slurmJobProvider.deleteJob(testDeleteJobFilter);
        Assertions.assertEquals(SOME_USER_NAME, deletedJobInfoResult.getUser());
        Assertions.assertEquals(List.of(SECOND_CORRECT_JOB_ID), deletedJobInfoResult.getIds());
    }

    @Test
    public void shouldThrowWhenUserIsNotPassedAndDeletingJobNotFound() {
        final CommandResult squeueCommandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .exitCode(1)
                .stdErr(Collections.singletonList(SQUEUE_JOB_LOADING_ERROR_STRING))
                .build();
        mockCommandCompilation(SQUEUE_COMMAND, squeueCommandResult, ALL_FORMAT,
                JOB_LIST_KEY, SOME_CORRECT_JOB_ID_STRING);
        final DeleteJobFilter testDeleteJobFilter = new DeleteJobFilter(false, SOME_CORRECT_JOB_ID, null);
        Assertions.assertThrows(GridEngineException.class, () -> slurmJobProvider.deleteJob(testDeleteJobFilter));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void shouldThrowWhenDeletingJobsWentWrong(final int exitCode) {
        final CommandResult scancelCommandResult = new CommandResult(EMPTY_LIST, exitCode, EMPTY_LIST);
        mockCommandCompilation(SCANCEL_COMMAND, scancelCommandResult,
                VERBOSE_KEY, OWNER_FILTRATION_KEY, SOME_USER_NAME);

        final DeleteJobFilter testDeleteJobFilter = new DeleteJobFilter(false, null, SOME_USER_NAME);
        Assertions.assertThrows(GridEngineException.class, () -> slurmJobProvider.deleteJob(testDeleteJobFilter));
    }

    @Test
    public void shouldThrowWhenAllJobsDeletionCompletedWithErrors() {
        final List<String> allJobsErrorStdOut = new ArrayList<>(DELETE_ALL_USER_JOBS_STDOUT);
        allJobsErrorStdOut.add(String.format(ERROR_DELETING_STRING_TEMPLATE, SECOND_CORRECT_JOB_ID));

        final CommandResult scancelCommandResult = new CommandResult(EMPTY_LIST, 0, allJobsErrorStdOut);
        mockCommandCompilation(SCANCEL_COMMAND, scancelCommandResult, SCANCEL_COMMAND, VERBOSE_KEY,
                OWNER_FILTRATION_KEY, SOME_USER_NAME);

        final DeleteJobFilter deleteJobFilter = new DeleteJobFilter(false, null, SOME_USER_NAME);
        Assertions.assertThrows(GridEngineException.class, () -> slurmJobProvider.deleteJob(deleteJobFilter));
    }

    private static Job correctBuild() {
        return Job.builder()
                .id(runningJobTemplate().getId())
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .build())
                .build();
    }
}
