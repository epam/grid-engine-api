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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.stream.Stream;


import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(properties = {"grid.engine.type=SLURM"})
public class SlurmJobProviderTest {

    private static final String SQUEUE_COMMAND = "squeue";
    private static final String ALL_FORMAT = "-o %all";
    private static final String SLURM_USER = "root";
    private static final String OWNER_FILTRATION = "-u root";
    private static final String STATE_FILTRATION = "-t root";
    private static final String ID_FILTRATION = "-j 2";
    private static final String NAME_FILTRATION = "-n test.sh";
    private static final String PENDING_STATUS_CODE = "PD";
    private static final String RUNNING_STATUS_CODE = "R";
    private static final String SUSPENDED_STATUS_CODE = "S";

    private static final String RUNNING_STRING = "RUNNING";
    private static final String PENDING_STRING = "PENDING";
    private static final String SUSPENDED_STRING = "SUSPENDED";
    private static final String TEST_QUEUE = "normal";

    private static final String JOB_NAME1 = "test.sh";
    private static final String JOB_NAME2 = "test3.sh";
    private static final String JOB_NAME3 = "test.sh";
    private static final double JOB_PRIORITY1 = 0.99998474121093;
    private static final double JOB_PRIORITY2 = 0.99998474074527;
    private static final double JOB_PRIORITY3 = 0.00000000000000;
    private static final List<String> NAME = Collections.singletonList(JOB_NAME1);
    private static final List<Integer> ID = Collections.singletonList(7);

    private static final List<String> EMPTY_LIST = Collections.EMPTY_LIST;
    private static final List<String> VALID_STDOUT = List.of("ACCOUNT|TRES_PER_NODE|MIN_CPUS|MIN_TMP_DISK|END_TIME|"
                    + "FEATURES|GROUP|OVER_SUBSCRIBE|JOBID|NAME|COMMENT|TIME_LIMIT|MIN_MEMORY|REQ_NODES|COMMAND|"
                    + "PRIORITY|QOS|REASON||ST|USER|RESERVATION|WCKEY|EXC_NODES|NICE|S:C:T|JOBID|EXEC_HOST|CPUS|NODES|"
                    + "DEPENDENCY|ARRAY_JOB_ID|GROUP|SOCKETS_PER_NODE|CORES_PER_SOCKET|THREADS_PER_CORE|ARRAY_TASK_ID|"
                    + "TIME_LEFT|TIME|NODELIST|CONTIGUOUS|PARTITION|PRIORITY|NODELIST(REASON)|START_TIME|STATE|UID|"
                    + "SUBMIT_TIME|LICENSES|CORE_SPEC|SCHEDNODES|WORK_DIR",
            "(null)|N/A|1|0|2022-05-18T10:29:10|(null)|root|OK|2|test.sh|(null)|5-00:00:00|500M||/data/test.sh|"
                    + "0.99998474121093|normal|None||R|root|(null)|(null)||0|*:*:*|2|worker1|1|1||2|0|*|*|*|N/A|"
                    + "4-21:35:49|2:24:11|worker1|0|normal|4294901759|worker1|2022-05-13T10:29:10|RUNNING|0|"
                    + "2022-05-13T10:29:09|(null)|N/A|(null)|/");

    private static final List<String> TWO_VALID_JOBS_STDOUT = List.of("ACCOUNT|TRES_PER_NODE|MIN_CPUS|MIN_TMP_DISK|"
                    + "END_TIME|FEATURES|GROUP|OVER_SUBSCRIBE|JOBID|NAME|COMMENT|TIME_LIMIT|MIN_MEMORY|REQ_NODES|"
                    + "COMMAND|PRIORITY|QOS|REASON||ST|USER|RESERVATION|WCKEY|EXC_NODES|NICE|S:C:T|JOBID|EXEC_HOST|"
                    + "CPUS|NODES|DEPENDENCY|ARRAY_JOB_ID|GROUP|SOCKETS_PER_NODE|CORES_PER_SOCKET|THREADS_PER_CORE|"
                    + "ARRAY_TASK_ID|TIME_LEFT|TIME|NODELIST|CONTIGUOUS|PARTITION|PRIORITY|NODELIST(REASON)|START_TIME|"
                    + "STATE|UID|SUBMIT_TIME|LICENSES|CORE_SPEC|SCHEDNODES|WORK_DIR",
            "(null)|N/A|1|0|N/A|(null)|root|OK|4|test3.sh|(null)|5-00:00:00|500M||/data/test3.sh|0.99998474074527|"
                    + "normal|Resources||PD|root|(null)|(null)||0|*:*:*|4|n/a|1|1||4|0|*|*|*|N/A|5-00:00:00|0:00||0|"
                    + "normal|4294901757|(Resources)|N/A|PENDING|0|2022-05-15T08:03:42|(null)|N/A|(null)|/",
            "(null)|N/A|1|0|2022-05-18T10:29:10|(null)|root|OK|2|test.sh|(null)|5-00:00:00|500M||/data/test.sh|"
                    + "0.99998474121093|normal|None||R|root|(null)|(null)||0|*:*:*|2|worker1|1|1||2|0|*|*|*|N/A|"
                    + "4-21:35:49|2:24:11|worker1|0|normal|4294901759|worker1|2022-05-13T10:29:10|RUNNING|0|"
                    + "2022-05-13T10:29:09|(null)|N/A|(null)|/");
    private static final List<String> THREE_VALID_JOBS_STDOUT = List.of("ACCOUNT|TRES_PER_NODE|MIN_CPUS|MIN_TMP_DISK|"
                    + "END_TIME|FEATURES|GROUP|OVER_SUBSCRIBE|JOBID|NAME|COMMENT|TIME_LIMIT|MIN_MEMORY|REQ_NODES|"
                    + "COMMAND|PRIORITY|QOS|REASON||ST|USER|RESERVATION|WCKEY|EXC_NODES|NICE|S:C:T|JOBID|EXEC_HOST|"
                    + "CPUS|NODES|DEPENDENCY|ARRAY_JOB_ID|GROUP|SOCKETS_PER_NODE|CORES_PER_SOCKET|THREADS_PER_CORE|"
                    + "ARRAY_TASK_ID|TIME_LEFT|TIME|NODELIST|CONTIGUOUS|PARTITION|PRIORITY|NODELIST(REASON)|START_TIME|"
                    + "STATE|UID|SUBMIT_TIME|LICENSES|CORE_SPEC|SCHEDNODES|WORK_DIR",
            "(null)|N/A|1|0|2022-05-20T08:03:38|(null)|root|OK|5|test.sh|(null)|5-00:00:00|500M||/data/test.sh|"
                    + "0.00000000000000|normal|None||S|root|(null)|(null)||0|*:*:*|5|worker1|1|1||2|0|*|*|*|N/A|"
                    + "4-18:52:51|5:07:09|worker1|0|normal|0|worker1|2022-05-15T08:03:38|SUSPENDED|0|"
                    + "2022-05-15T08:03:37|(null)|N/A|(null)|/",
            "(null)|N/A|1|0|N/A|(null)|root|OK|4|test3.sh|(null)|5-00:00:00|500M||/data/test3.sh|0.99998474074527|"
                    + "normal|Resources||PD|root|(null)|(null)||0|*:*:*|4|n/a|1|1||4|0|*|*|*|N/A|5-00:00:00|0:00||0|"
                    + "normal|4294901757|(Resources)|N/A|PENDING|0|2022-05-15T08:03:42|(null)|N/A|(null)|/",
            "(null)|N/A|1|0|2022-05-18T10:29:10|(null)|root|OK|2|test.sh|(null)|5-00:00:00|500M||/data/test.sh|"
                    + "0.99998474121093|normal|None||R|root|(null)|(null)||0|*:*:*|2|worker1|1|1||2|0|*|*|*|N/A|"
                    + "4-21:35:49|2:24:11|worker1|0|normal|4294901759|worker1|2022-05-13T10:29:10|RUNNING|0|"
                    + "2022-05-13T10:29:09|(null)|N/A|(null)|/");

    private static final List<String> INVALID_OUTPUT = List.of("ACCOUNT|TRES_PER_NODE|MIN_CPUS|MIN_TMP_DISK|END_TIME|"
                    + "FEATURES|GROUP|OVER_SUBSCRIBE|JOBID|NAME|COMMENT|TIME_LIMIT|MIN_MEMORY|REQ_NODES|COMMAND|"
                    + "PRIORITY|QOS|REASON||ST|USER|RESERVATION|WCKEY|EXC_NODES|NICE|S:C:T|JOBID|EXEC_HOST|CPUS|NODES|"
                    + "DEPENDENCY|ARRAY_JOB_ID|GROUP|SOCKETS_PER_NODE|CORES_PER_SOCKET|THREADS_PER_CORE|ARRAY_TASK_ID|"
                    + "TIME_LEFT|TIME|NODELIST|CONTIGUOUS|PARTITION|PRIORITY|NODELIST(REASON)|START_TIME|STATE|UID|"
                    + "SUBMIT_TIME|LICENSES|CORE_SPEC|SCHEDNODES|WORK_DIR",
            "(null)|N/A|1|0|2022-05-18T10:29:10|(null)|root|OK|2|test.sh|(null)|5-00:00:00|500M||/data/test.sh");

    private static final List<String> EMPTY_OUTPUT = List.of("ACCOUNT|TRES_PER_NODE|MIN_CPUS|MIN_TMP_DISK|"
            + "END_TIME|FEATURES|GROUP|OVER_SUBSCRIBE|JOBID|NAME|COMMENT|TIME_LIMIT|MIN_MEMORY|REQ_NODES|COMMAND|"
            + "PRIORITY|QOS|REASON||ST|USER|RESERVATION|WCKEY|EXC_NODES|NICE|S:C:T|JOBID|EXEC_HOST|CPUS|NODES|"
            + "DEPENDENCY|ARRAY_JOB_ID|GROUP|SOCKETS_PER_NODE|CORES_PER_SOCKET|THREADS_PER_CORE|ARRAY_TASK_ID|"
            + "TIME_LEFT|TIME|NODELIST|CONTIGUOUS|PARTITION|PRIORITY|NODELIST(REASON)|START_TIME|STATE|UID|"
            + "SUBMIT_TIME|LICENSES|CORE_SPEC|SCHEDNODES|WORK_DIR");

    private static final String TEXT_JOB_SUBMITTED = "Submitted batch job 2";
    private static final String SBATCH = "sbatch";
    private static final String ENV_VARIABLES = "envVariables";
    private static final String ENV_VAR_KEY = "parameter1";
    private static final String ENV_VAR_VALUE = "parameter1Value";
    private static final String ENV_VAR_MAP_ENTRY = "parameter1=parameter1Value";
    private static final String ENV_VAR_FLAG = "--export=";
    private static final String ENV_VAR_MAP_ONLY_KEY = "parameter1";
    private static final String JOB_PRIORITY4 = "9999";
    private static final String JOB_NAME4 = "newSlurmJob";
    private static final String JOB_PARTITION = "normal";
    private static final String JOB_WORK_DIR = "/data/";

    @Autowired
    private SlurmJobProvider slurmJobProvider;

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @MockBean
    private GridEngineCommandCompiler commandCompiler;

    @MockBean
    private JobFilter mockJobFilter;

    @Captor
    private ArgumentCaptor<EngineType> engineTypeCaptor;

    @Captor
    private ArgumentCaptor<String> commandCaptor;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

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
                .stdOut(EMPTY_OUTPUT)
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

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT, OWNER_FILTRATION);
        final Listing<Job> result = slurmJobProvider.filterJobs(jobFilter);
        Mockito.verify(commandCompiler).compileCommand(engineTypeCaptor.capture(),
                commandCaptor.capture(),
                contextCaptor.capture());

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

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT, STATE_FILTRATION);
        final Listing<Job> result = slurmJobProvider.filterJobs(jobFilter);
        Mockito.verify(commandCompiler).compileCommand(engineTypeCaptor.capture(),
                commandCaptor.capture(),
                contextCaptor.capture());

        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(runningJob, result.getElements().get(0));
    }

    @Test
    public void shouldReturnCorrectIdFiltration() {
        final Job runningJob = runningJobTemplate();
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setIds(ID);
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(VALID_STDOUT)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT, ID_FILTRATION);
        final Listing<Job> result = slurmJobProvider.filterJobs(jobFilter);
        Mockito.verify(commandCompiler).compileCommand(engineTypeCaptor.capture(),
                commandCaptor.capture(),
                contextCaptor.capture());

        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(runningJob, result.getElements().get(0));
    }

    @Test
    public void shouldReturnCorrectNameFiltration() {
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setNames(NAME);
        final Job runningJob = runningJobTemplate();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(VALID_STDOUT)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT, NAME_FILTRATION);
        final Listing<Job> result = slurmJobProvider.filterJobs(jobFilter);
        Mockito.verify(commandCompiler).compileCommand(engineTypeCaptor.capture(),
                commandCaptor.capture(),
                contextCaptor.capture());

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
    @MethodSource("parameters")
    public void shouldReturnCorrectStateCommand(final String jobState) {
        final List<Job> expectedFilteredJob = threeJobsTemplate();

        final CommandResult commandResult = new CommandResult();
        commandResult.setStdOut(THREE_VALID_JOBS_STDOUT);
        commandResult.setStdErr(EMPTY_LIST);

        mockCommandCompilation(SQUEUE_COMMAND, commandResult, ALL_FORMAT);
        doReturn(jobState).when(mockJobFilter).getState();
        final Listing<Job> result = slurmJobProvider.filterJobs(null);
        Mockito.verify(commandCompiler).compileCommand(engineTypeCaptor.capture(),
                commandCaptor.capture(),
                contextCaptor.capture());

        if (jobState.equals(SUSPENDED_STRING)) {
            Assertions.assertEquals(expectedFilteredJob.get(0), result.getElements().get(0));
        }
        if (jobState.equals(PENDING_STRING)) {
            Assertions.assertEquals(expectedFilteredJob.get(1), result.getElements().get(1));
        }
        if (jobState.equals(RUNNING_STRING)) {
            Assertions.assertEquals(expectedFilteredJob.get(2), result.getElements().get(2));
        }
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                        RUNNING_STRING,
                        PENDING_STRING,
                        SUSPENDED_STRING)
                .map(Arguments::of);
    }

    private static List<Job> threeJobsTemplate() {
        final Job running = runningJobTemplate();
        final Job pending = pendingJobTemplate();
        final Job suspended = suspendedJobTemplate();
        return Arrays.asList(suspended, pending, running);
    }

    private void mockCommandCompilation(final String command, final CommandResult commandResult,
                                        final String... compiledArray) {
        doReturn(compiledArray).when(commandCompiler).compileCommand(Mockito.eq(EngineType.SLURM),
                Mockito.matches(command),
                Mockito.any());
        doReturn(commandResult).when(mockCmdExecutor).execute(compiledArray);
    }

    private static Job runningJobTemplate() {
        return Job.builder()
                .id(2)
                .name(JOB_NAME1)
                .priority(JOB_PRIORITY1)
                .owner(SLURM_USER)
                .queueName(TEST_QUEUE)
                .submissionTime(LocalDateTime.parse("2022-05-13T10:29:09"))
                .state(JobState.builder()
                        .category(JobState.Category.RUNNING)
                        .state(RUNNING_STRING)
                        .stateCode(RUNNING_STATUS_CODE).build())
                .build();
    }

    private static Job pendingJobTemplate() {
        return Job.builder()
                .id(4)
                .name(JOB_NAME2)
                .priority(JOB_PRIORITY2)
                .owner(SLURM_USER)
                .queueName(TEST_QUEUE)
                .submissionTime(LocalDateTime.parse("2022-05-15T08:03:42"))
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .state(PENDING_STRING)
                        .stateCode(PENDING_STATUS_CODE).build())
                .build();
    }

    private static Job suspendedJobTemplate() {
        return Job.builder()
                .id(5)
                .name(JOB_NAME3)
                .priority(JOB_PRIORITY3)
                .owner(SLURM_USER)
                .queueName(TEST_QUEUE)
                .submissionTime(LocalDateTime.parse("2022-05-15T08:03:37"))
                .state(JobState.builder()
                        .category(JobState.Category.SUSPENDED)
                        .state(SUSPENDED_STRING)
                        .stateCode(SUSPENDED_STATUS_CODE).build())
                .build();
    }


    @ParameterizedTest
    @MethodSource("provideInvalidJobOptions")
    public void shouldThrowGridEngineExceptionMakingSbatchCommand(final JobOptions jobOptions) {
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class,
                () -> slurmJobProvider.runJob(jobOptions));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideUnsupportedJobOptions")
    public void shouldThrowUnsupportedExceptionMakingSbatchCommand(final JobOptions jobOptions) {
        final Throwable thrown = Assertions.assertThrows(UnsupportedOperationException.class,
                () -> slurmJobProvider.runJob(jobOptions));
        Assertions.assertNotNull(thrown.getMessage());
    }

    static Stream<Arguments> provideInvalidJobOptions() {
        return Stream.of(
                Arguments.of(JobOptions.builder().command(null).build()),
                Arguments.of(JobOptions.builder().command(EMPTY_STRING).build())
        );
    }

    static Stream<Arguments> provideUnsupportedJobOptions() {
        return Stream.of(
                Arguments.of(JobOptions.builder().priority(-100)
                        .command(JOB_NAME1).build()),
                Arguments.of(JobOptions.builder().parallelEnvOptions(new ParallelEnvOptions(EMPTY_STRING, 1, 10))
                        .command(JOB_NAME1).build())
        );
    }

    @Test
    public void shouldThrowsExceptionBecauseOptionsAreEmpty() {
        final JobOptions jobOptions = new JobOptions();
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                slurmJobProvider.runJob(jobOptions));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideValidEnvVariables")
    public void shouldMakeValidEnvVariables(final JobOptions.JobOptionsBuilder jobOptionsBuilder,
                                            final String expectedEnvVariables, final String[] command) {
        final JobOptions jobOptions = jobOptionsBuilder.build();
        final CommandResult commandResult = new CommandResult();

        jobOptions.setCommand(JOB_NAME1);
        commandResult.setStdOut(Collections.singletonList(TEXT_JOB_SUBMITTED));
        commandResult.setStdErr(EMPTY_LIST);

        mockCommandCompilation(SBATCH, commandResult, command);
        slurmJobProvider.runJob(jobOptions);
        Mockito.verify(commandCompiler).compileCommand(engineTypeCaptor.capture(),
                commandCaptor.capture(),
                contextCaptor.capture());

        Assertions.assertEquals(contextCaptor.getValue().getVariable(ENV_VARIABLES), expectedEnvVariables);
    }

    static Stream<Arguments> provideValidEnvVariables() {
        return Stream.of(
                Arguments.of(getSimpleJobCommand().envVariables(Collections.singletonMap(ENV_VAR_KEY, ENV_VAR_VALUE)),
                        ENV_VAR_MAP_ENTRY, new String[]{SBATCH, ENV_VAR_FLAG, ENV_VAR_MAP_ENTRY, JOB_NAME1}),
                Arguments.of(getSimpleJobCommand().envVariables(Collections.singletonMap(ENV_VAR_KEY, EMPTY_STRING)),
                        ENV_VAR_MAP_ONLY_KEY, new String[]{SBATCH, ENV_VAR_FLAG, ENV_VAR_KEY, JOB_NAME1})
        );
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

        mockCommandCompilation(SBATCH, commandResult, command);
        final Job result = slurmJobProvider.runJob(jobOptions);
        Mockito.verify(commandCompiler).compileCommand(engineTypeCaptor.capture(),
                commandCaptor.capture(),
                contextCaptor.capture());

        Assertions.assertEquals(expectedFilteredJob.getId(), result.getId());
    }

    static Stream<Arguments> provideCorrectSbatchCommands() {
        return mapObjectsToArgumentsStream(
                new String[]{SBATCH, "--export=", ENV_VAR_MAP_ENTRY, JOB_NAME1},
                new String[]{SBATCH, "--priority=", JOB_PRIORITY4, JOB_NAME1},
                new String[]{SBATCH, "-J", JOB_NAME4, JOB_NAME1},
                new String[]{SBATCH, "--partition=", JOB_PARTITION, JOB_NAME1},
                new String[]{SBATCH, "-D", JOB_WORK_DIR, JOB_NAME1}
        );
    }

    @Test
    public void shouldReturnCorrectJob() {
        final Job expectedFilteredJob = correctBuild();
        final JobOptions jobOptions = new JobOptions();
        final CommandResult commandResult = new CommandResult();
        jobOptions.setCommand(JOB_NAME1);

        commandResult.setStdOut(Collections.singletonList(TEXT_JOB_SUBMITTED));
        commandResult.setStdErr(EMPTY_LIST);

        mockCommandCompilation(SBATCH, commandResult, SBATCH, JOB_NAME1);
        final Job result = slurmJobProvider.runJob(jobOptions);
        Mockito.verify(commandCompiler).compileCommand(engineTypeCaptor.capture(),
                commandCaptor.capture(),
                contextCaptor.capture());

        Assertions.assertEquals(expectedFilteredJob, result);
    }

    private static JobOptions.JobOptionsBuilder getSimpleJobCommand() {
        return JobOptions.builder().command(JOB_NAME1);
    }

    private static Stream<Arguments> mapObjectsToArgumentsStream(final Object... args) {
        return Stream.of(args).map(Arguments::of);
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
