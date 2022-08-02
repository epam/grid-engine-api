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

import com.epam.grid.engine.TestPropertiesWithSgeEngine;
import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.entity.job.JobState;
import com.epam.grid.engine.entity.job.ParallelEnvOptions;
import com.epam.grid.engine.entity.job.ParallelExecutionOptions;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.PENDING_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.RUNNING_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.TYPE_XML;

@SpringBootTest
@TestPropertiesWithSgeEngine
public class SgeJobProviderTest {

    private static final long SOME_JOB_ID_1 = 10;
    private static final long SOME_JOB_ID_2 = 5;
    private static final String SOME_JOB_ID_2_STRING = String.valueOf(SOME_JOB_ID_2);

    private static final String QSTAT_COMMAND = "qstat";
    private static final String QDEL_COMMAND = "qdel";
    private static final String QDEL_USER_KEY = "-u";
    private static final String USER_NAME = "sgeuser";
    private static final String SUCCESS_JOB_DELETING_STRING_TEMPLATE = USER_NAME + " has deleted job %d";
    private static final String ERROR_JOB_DELETING_STRING_TEMPLATE = "denied: job \"%d\" does not exist";

    private static final String QSUB = "qsub";
    private static final String JOB_COMMAND = "simple.sh";
    private static final String SOME_PE_NAME = "somePe";
    private static final int SOME_PE_MIN_VALUE = 1;
    private static final int SOME_PE_MAX_VALUE = 100;
    private static final int WRONG_PE_MIN_VALUE = -1;
    private static final int WRONG_PE_MAX_VALUE = 10_000_000;
    private static final String SOME_JOB_SUBMIT_ERROR_STDOUT = "Unable to run job: job rejected";
    private static final String JOB_SUBMITTED_FORMAT_TEMPLATE = "Your job %d (\"%s\") has been submitted";
    private static final String JOB_SUBMITTED_STDOUT = String.format(JOB_SUBMITTED_FORMAT_TEMPLATE,
            SOME_JOB_ID_2, JOB_COMMAND);

    private static final String SOME_PENDING_STATUS_CODE = "qw";
    private static final String SOME_RUNNING_STATUS_CODE = "r";
    private static final String SOME_WRONG_STATUS_CODE = "lol";
    private static final String SOME_QUEUE_NAME = "test_queue";
    private static final String SOME_JOB_LOG_DIRECTORY_PATH = "/mnt/logs";

    private static final String SOME_JOB_NAME_1 = "someName";
    private static final String SOME_JOB_NAME_2 = "favoriteJob";

    private static final String SOME_JOB_STARTING_TIME_1 = "2021-06-30T17:27:30";
    private static final String SOME_JOB_STARTING_TIME_2 = "2022-07-21T18:48:59";
    private static final String SOME_JOB_PRIORITY_STRING = "0.55500";
    private static final double SOME_JOB_PRIORITY = Double.parseDouble(SOME_JOB_PRIORITY_STRING);
    private static final int SOME_SLOTS_AMOUNT = 1;

    private static final String INVALID_XML = "<?xml version='1.0'?>\n"
            + "<job_info  xmlns:xsd="
            + "\"http://arc.liv.ac.uk/repos/darcs/sge/source/dist/util/resources/schemas/qstat/qstat.xsd\">\n"
            + "<queue_info>\n"
            + "<job_list state=\"running\">\n"
            + "<JB_job_number>8</JB_job_number>\n"
            + "<JAT_prio>0.55500</JAT_prio>\n"
            + "<JB_name>STDIN</JB_name>\n"
            + "<JB_owner>sgeuser</JB_owner>\n"
            + "<state>r</state>\n"
            + " <JAT_start_time>2021-07-02T10:46:14</JAT_start_time>\n"
            + "<queue_name>main@c242f10e1253</queue_name>\n"
            + "<slots>1</slots>\n"
            + "</job_list>";

    private static final String QSTAT_STDOUT_TEMPLATE = "<?xml version='1.0'?>\n"
            + "<job_info  xmlns:xsd="
            + "\"http://arc.liv.ac.uk/repos/darcs/sge/source/dist/util/resources/schemas/qstat/qstat.xsd\">\n"
            + "  <queue_info>\n%s</queue_info>\n"
            + "  <job_info>\n%s</job_info>\n"
            + "</job_info>";

    private static final String QSTAT_STDOUT_JOB_TEMPLATE = "<job_list state=\"%s\">\n"
            + "<JB_job_number>%d</JB_job_number>\n"
            + "<JAT_prio>%s</JAT_prio>\n"
            + "<JB_name>%s</JB_name>\n"
            + "<JB_owner>%s</JB_owner>\n"
            + "<state>%s</state>\n"
            + "<JAT_start_time>%s</JAT_start_time>\n"
            + "<queue_name>%s</queue_name>\n"
            + "<slots>%d</slots>\n"
            + "</job_list>\n";

    private static final String RUNNING_JOB_XML = String.format(QSTAT_STDOUT_JOB_TEMPLATE,
            RUNNING_STRING, SOME_JOB_ID_1, SOME_JOB_PRIORITY_STRING, SOME_JOB_NAME_1, USER_NAME,
            SOME_RUNNING_STATUS_CODE, SOME_JOB_STARTING_TIME_1, SOME_QUEUE_NAME, SOME_SLOTS_AMOUNT);

    private static final String PENDING_JOB_XLM = String.format(QSTAT_STDOUT_JOB_TEMPLATE,
            PENDING_STRING, SOME_JOB_ID_2, SOME_JOB_PRIORITY_STRING, SOME_JOB_NAME_2, USER_NAME,
            SOME_PENDING_STATUS_CODE, SOME_JOB_STARTING_TIME_2, SOME_QUEUE_NAME, SOME_SLOTS_AMOUNT);

    private static final List<String> EMPTY_JOB_LIST_STDOUT = List.of(String.format(QSTAT_STDOUT_TEMPLATE,
            EMPTY_STRING, EMPTY_STRING));
    private static final List<String> TWO_JOBS_QSTAT_STDOUT = List.of(String.format(QSTAT_STDOUT_TEMPLATE,
            RUNNING_JOB_XML, PENDING_JOB_XLM));

    private static final List<String> ONE_PENDING_JOB_QSTAT_STDOUT = List.of(String.format(QSTAT_STDOUT_TEMPLATE,
            EMPTY_STRING, PENDING_JOB_XLM));

    @Autowired
    private SgeJobProvider sgeJobProvider;

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @MockBean
    private GridEngineCommandCompiler commandCompiler;

    @Test
    public void shouldFailWithInvalidXml() {
        final CommandResult commandResult = new CommandResult(List.of(INVALID_XML), 0, EMPTY_LIST);
        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);
        Assertions.assertThrows(GridEngineException.class, () -> sgeJobProvider.filterJobs(new JobFilter()));
    }

    @Test
    public void shouldReturnEmptyJobListDuringFiltration() {
        final CommandResult commandResult = new CommandResult(EMPTY_JOB_LIST_STDOUT, 0, EMPTY_LIST);
        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);
        Assertions.assertTrue(sgeJobProvider.filterJobs(new JobFilter()).getElements().isEmpty());
    }

    @Test
    public void shouldReturnCorrectJobsWhenGettingJobList() {
        final CommandResult commandResult = new CommandResult(TWO_JOBS_QSTAT_STDOUT, 0, EMPTY_LIST);
        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);

        final Listing<Job> result = sgeJobProvider.filterJobs(new JobFilter());
        Assertions.assertEquals(2, result.getElements().size());
        Assertions.assertTrue(result.getElements().contains(pendingJobTemplate()));
        Assertions.assertTrue(result.getElements().contains(runningJobTemplate()));
    }

    @ParameterizedTest
    @MethodSource("provideCasesForFiltration")
    public void shouldReturnCorrectFiltration(final JobFilter jobFilter, final List<String> stdOut,
                                              final Job expectedJob) {
        final CommandResult commandResult = new CommandResult(stdOut, 0, EMPTY_LIST);
        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);

        final Listing<Job> result = sgeJobProvider.filterJobs(jobFilter);
        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(expectedJob, result.getElements().get(0));
    }

    static Stream<Arguments> provideCasesForFiltration() {
        return Stream.of(
                Arguments.of(JobFilter.builder().names(List.of(SOME_JOB_NAME_1)).build(), TWO_JOBS_QSTAT_STDOUT,
                        runningJobTemplate()),
                Arguments.of(JobFilter.builder().ids(List.of(SOME_JOB_ID_2)).build(), TWO_JOBS_QSTAT_STDOUT,
                        pendingJobTemplate()),
                Arguments.of(JobFilter.builder().state(PENDING_STRING).build(), ONE_PENDING_JOB_QSTAT_STDOUT,
                        pendingJobTemplate())
        );
    }

    @Test
    public void shouldThrowWhenCommandResultFiltrationWithErrorCode() {
        final CommandResult commandResult = new CommandResult(EMPTY_LIST, 1, EMPTY_LIST);
        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);
        Assertions.assertThrows(GridEngineException.class, () -> sgeJobProvider.filterJobs(new JobFilter()));
    }

    @Test
    public void shouldThrowWhenPassWrongStatusCodeDuringFiltration() {
        final JobFilter jobFilter = JobFilter.builder().state(SOME_WRONG_STATUS_CODE).build();
        Assertions.assertThrows(GridEngineException.class, () -> sgeJobProvider.filterJobs(jobFilter));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidPeOptions")
    public void shouldThrowWhenPassWrongPeOptionsDuringJobSubmitting(final String peName,
                                                                     final int peMin, final int peMax) {
        final JobOptions jobOptions = JobOptions.builder()
                .command(JOB_COMMAND)
                .parallelEnvOptions(new ParallelEnvOptions(peName, peMin, peMax))
                .build();
        Assertions.assertThrows(GridEngineException.class,
                () -> sgeJobProvider.runJob(jobOptions, SOME_JOB_LOG_DIRECTORY_PATH));
    }

    static Stream<Arguments> provideInvalidPeOptions() {
        return Stream.of(
                Arguments.of(EMPTY_STRING, SOME_PE_MIN_VALUE, SOME_PE_MAX_VALUE),
                Arguments.of(SOME_PE_NAME, WRONG_PE_MIN_VALUE, SOME_PE_MAX_VALUE),
                Arguments.of(SOME_PE_NAME, WRONG_PE_MAX_VALUE, SOME_PE_MAX_VALUE),
                Arguments.of(SOME_PE_NAME, SOME_PE_MIN_VALUE, WRONG_PE_MIN_VALUE),
                Arguments.of(SOME_PE_NAME, SOME_PE_MIN_VALUE, WRONG_PE_MAX_VALUE),
                Arguments.of(SOME_PE_NAME, SOME_PE_MAX_VALUE, SOME_PE_MIN_VALUE)
        );
    }

    @ParameterizedTest
    @MethodSource("provideBadCommandResultForJobSubmitting")
    public void shouldThrowWhenBadCommandResultDuringJobSubmitting(final CommandResult commandResult) {
        final JobOptions jobOptions = JobOptions.builder().command(JOB_COMMAND).build();
        mockCommandCompilation(QSUB, commandResult, QSUB, JOB_COMMAND);
        Assertions.assertThrows(GridEngineException.class,
                () -> sgeJobProvider.runJob(jobOptions, SOME_JOB_LOG_DIRECTORY_PATH));
    }

    static Stream<Arguments> provideBadCommandResultForJobSubmitting() {
        return Stream.of(
                        new CommandResult(EMPTY_LIST, 1, EMPTY_LIST),
                        new CommandResult(EMPTY_LIST, 0, EMPTY_LIST),
                        new CommandResult(List.of(SOME_JOB_SUBMIT_ERROR_STDOUT), 0, EMPTY_LIST))
                .map(Arguments::of);
    }

    @Test
    public void shouldReturnSubmittingJob() {
        final Job expectedJob = Job.builder()
                .id(SOME_JOB_ID_2)
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .build())
                .build();
        final JobOptions jobOptions = JobOptions.builder().command(JOB_COMMAND).build();

        final CommandResult commandResult = new CommandResult(List.of(JOB_SUBMITTED_STDOUT), 0, EMPTY_LIST);
        mockCommandCompilation(QSUB, commandResult, QSUB, JOB_COMMAND);
        Assertions.assertEquals(expectedJob, sgeJobProvider.runJob(jobOptions, SOME_JOB_LOG_DIRECTORY_PATH));
    }

    @Test
    public void shouldThrowExceptionBecauseParallelExecOptionsAreUsed() {
        final JobOptions jobOptions = JobOptions.builder()
                .command(JOB_COMMAND)
                .parallelExecutionOptions(new ParallelExecutionOptions())
                .build();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> sgeJobProvider.runJob(jobOptions, SOME_JOB_LOG_DIRECTORY_PATH));
    }

    @ParameterizedTest
    @MethodSource("provideValidCasesForJobDeleting")
    public void shouldReturnCorrectDeletedJobInfo(final String requestUser, final String expectedUser,
                                                  final List<Long> ids) {
        final DeleteJobFilter jobFilter = DeleteJobFilter.builder().user(requestUser).ids(ids).build();
        final CommandResult qstatCommandResult = new CommandResult(ONE_PENDING_JOB_QSTAT_STDOUT, 0, EMPTY_LIST);
        mockCommandCompilation(QSTAT_COMMAND, qstatCommandResult, QSTAT_COMMAND, TYPE_XML);

        final CommandResult commandResult = CommandResult.builder()
                .stdOut(List.of(String.format(SUCCESS_JOB_DELETING_STRING_TEMPLATE, SOME_JOB_ID_2)))
                .stdErr(EMPTY_LIST).exitCode(0).build();

        mockCommandCompilation(QDEL_COMMAND, commandResult, QDEL_COMMAND, QDEL_USER_KEY,
                USER_NAME, SOME_JOB_ID_2_STRING);
        final Listing<DeletedJobInfo> result = sgeJobProvider.deleteJob(jobFilter);
        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(new DeletedJobInfo(SOME_JOB_ID_2, expectedUser), result.getElements().get(0));
    }

    static Stream<Arguments> provideValidCasesForJobDeleting() {
        return Stream.of(
                Arguments.of(USER_NAME, USER_NAME, EMPTY_LIST),
                Arguments.of(null, USER_NAME, List.of(SOME_JOB_ID_2)),
                Arguments.of(USER_NAME, USER_NAME, List.of(SOME_JOB_ID_1, SOME_JOB_ID_2))
        );
    }

    @Test
    public void shouldThrowWhenJobOwnerIsNotFoundDuringJobDeletion() {
        final DeleteJobFilter jobFilter = DeleteJobFilter.builder().ids(List.of(SOME_JOB_ID_1)).build();
        final CommandResult squeueCommandResult = new CommandResult(EMPTY_JOB_LIST_STDOUT, 0, EMPTY_LIST);
        mockCommandCompilation(QSTAT_COMMAND, squeueCommandResult, TYPE_XML);
        Assertions.assertThrows(GridEngineException.class, () -> sgeJobProvider.deleteJob(jobFilter));
    }

    @Test
    public void shouldThrowWhenResultWithErrorCodeDuringJobDeletion() {
        final DeleteJobFilter jobFilter = DeleteJobFilter.builder().user(USER_NAME).ids(List.of(SOME_JOB_ID_2)).build();
        final CommandResult qstatCommandResult = new CommandResult(ONE_PENDING_JOB_QSTAT_STDOUT, 0, EMPTY_LIST);
        mockCommandCompilation(QSTAT_COMMAND, qstatCommandResult, QSTAT_COMMAND, TYPE_XML);

        final CommandResult commandResult = new CommandResult(EMPTY_LIST, 1, EMPTY_LIST);
        mockCommandCompilation(QDEL_COMMAND, commandResult, QDEL_COMMAND, QDEL_USER_KEY,
                USER_NAME, SOME_JOB_ID_2_STRING);

        Assertions.assertThrows(GridEngineException.class, () -> sgeJobProvider.deleteJob(jobFilter));
    }

    @Test
    public void shouldReturnValidDeletedJobInfoWhenOnlyOneSuccessDeletedJob() {
        final DeleteJobFilter jobFilter = DeleteJobFilter.builder().ids(List.of(SOME_JOB_ID_1, SOME_JOB_ID_2)).build();

        final CommandResult qstatCommandResult = new CommandResult(TWO_JOBS_QSTAT_STDOUT, 0, EMPTY_LIST);
        mockCommandCompilation(QSTAT_COMMAND, qstatCommandResult, QSTAT_COMMAND, TYPE_XML);

        final CommandResult qdelCommandResult = CommandResult.builder()
                .stdOut(List.of(String.format(ERROR_JOB_DELETING_STRING_TEMPLATE, SOME_JOB_ID_1),
                                String.format(SUCCESS_JOB_DELETING_STRING_TEMPLATE, SOME_JOB_ID_2)))
                .stdErr(EMPTY_LIST).exitCode(1).build();

        mockCommandCompilation(QDEL_COMMAND, qdelCommandResult, QDEL_COMMAND,
                String.format("%d,%d", SOME_JOB_ID_1, SOME_JOB_ID_2));

        final Listing<DeletedJobInfo> result = sgeJobProvider.deleteJob(jobFilter);
        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(new DeletedJobInfo(SOME_JOB_ID_2, USER_NAME), result.getElements().get(0));
    }

    private static Job runningJobTemplate() {
        return Job.builder()
                .id(SOME_JOB_ID_1)
                .name(SOME_JOB_NAME_1)
                .priority(SOME_JOB_PRIORITY)
                .owner(USER_NAME)
                .queueName(SOME_QUEUE_NAME)
                .submissionTime(LocalDateTime.parse(SOME_JOB_STARTING_TIME_1))
                .slots(SOME_SLOTS_AMOUNT)
                .state(JobState.builder()
                        .category(JobState.Category.RUNNING)
                        .state(RUNNING_STRING)
                        .stateCode(SOME_RUNNING_STATUS_CODE).build())
                .build();
    }

    private static Job pendingJobTemplate() {
        return Job.builder()
                .id(SOME_JOB_ID_2)
                .name(SOME_JOB_NAME_2)
                .priority(SOME_JOB_PRIORITY)
                .owner(USER_NAME)
                .queueName(SOME_QUEUE_NAME)
                .submissionTime(LocalDateTime.parse(SOME_JOB_STARTING_TIME_2))
                .slots(SOME_SLOTS_AMOUNT)
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .state(PENDING_STRING)
                        .stateCode(SOME_PENDING_STATUS_CODE)
                        .build())
                .build();
    }

    private void mockCommandCompilation(final String command, final CommandResult commandResult,
                                        final String... compiledArray) {
        Mockito.doReturn(compiledArray).when(commandCompiler).compileCommand(Mockito.eq(CommandType.SGE),
                Mockito.matches(command), Mockito.any());
        Mockito.doReturn(commandResult).when(mockCmdExecutor).execute(compiledArray);
    }
}
