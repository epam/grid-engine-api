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
import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobLogInfo;
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
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.ONE;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.PENDING_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.RUNNING_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.SPACE;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.SUSPENDED_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.TYPE_XML;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(properties = {"grid.engine.type=SGE"})
public class SgeJobProviderTest {

    private static final String QSTAT_COMMAND = "qstat";
    private static final String QDEL_COMMAND = "qdel";
    private static final String GET_LOG_LINES_COMMAND = "get_log_lines";
    private static final String GET_LOGFILE_INFO_COMMAND = "get_logfile_info";
    private static final String FORCED_QDEL = "-f";
    private static final String USER_QDEL = "-u";
    private static final String STATE = "-s";
    private static final String SGEUSER = "sgeuser";
    private static final String SGE_USER = "sge user";
    private static final String HAS_DELETED_JOB = "sgeuser has deleted job 1";
    private static final String JOB_DOES_NOT_EXISTS = "Job does not exists";
    private static final String HAS_NOT_SUBMITTED_JOB = "user has not submitted job";
    private static final String FOUR = "4";
    private static final String EIGHT = "8";
    private static final String NINE = "9";
    private static final String ELEVEN = "11";
    private static final String THIRTEEN = "13";
    private static final String NUMBER_FIND_PATTERN = "(.)*(\\d)(.)*";
    private static final String QSUB = "qsub";
    private static final String COMMAND_SCRIPT_FILE = "demo_v.sh";
    private static final String SUCCESSFULLY_DELETED = "sgeuser has deleted job 1";
    private static final String SOME_PENDING_STATUS_CODE = "qw";
    private static final String SOME_RUNNING_STATUS_CODE = "r";
    private static final String TEST_QUEUE = "test_queue";
    private static final String JOB_COMMAND = "simple.sh";

    private static final String TEXT_JOB_SUBMITTED = "Your job 7 (\"demo_v.sh\") has been submitted";

    private static final String SOMEUSER = "someuser";
    private static final String ANOTHERUSER = "anotheruser";
    private static final String SOME_JOB_NAME_1 = "someName";
    private static final String SOME_JOB_NAME_2 = "favoriteJob";
    private static final List<String> NAME = Collections.singletonList(SOME_JOB_NAME_1);
    private static final List<Long> ID = Collections.singletonList(7L);

    private static final int SOME_JOB_ID = 10;
    private static final int SOME_LINES = 10;
    private static final int SOME_BYTES = 150;
    private static final JobLogInfo.Type SOME_LOG_TYPE = JobLogInfo.Type.ERR;
    private static final String LOG_FILE_NAME = String.format("%d.%s", SOME_JOB_ID, SOME_LOG_TYPE.getSuffix());
    private static final List<String> INFO_COMMAND_RESULT_STDOUT = Collections.singletonList(
            String.format("%d %d %s", SOME_LINES, SOME_BYTES, LOG_FILE_NAME));

    private static final String VALID_XML = "<?xml version='1.0'?>\n"
            + "<job_info  xmlns:xsd="
            + "\"http://arc.liv.ac.uk/repos/darcs/sge/source/dist/util/resources/schemas/qstat/qstat.xsd\">\n"
            + "  <queue_info>\n"
            + "  <job_list state=\"running\">\n"
            + "      <JB_job_number>8</JB_job_number>\n"
            + "      <JAT_prio>0.55500</JAT_prio>\n"
            + "      <JB_name>someName</JB_name>\n"
            + "      <JB_owner>sgeuser</JB_owner>\n"
            + "      <state>r</state>\n"
            + "      <JAT_start_time>2021-07-02T10:46:14</JAT_start_time>\n"
            + "      <queue_name>main@c242f10e1253</queue_name>\n"
            + "      <slots>1</slots>\n"
            + "    </job_list>\n"
            + "  </queue_info>\n"
            + "  <job_info>\n"
            + "    <job_list state=\"pending\">\n"
            + "      <JB_job_number>2</JB_job_number>\n"
            + "      <JAT_prio>0.00000</JAT_prio>\n"
            + "      <JB_name>someName</JB_name>\n"
            + "      <JB_owner>sgeuser</JB_owner>\n"
            + "      <state>qw</state>\n"
            + "      <JB_submission_time>2021-06-30T17:27:30</JB_submission_time>\n"
            + "      <queue_name></queue_name>\n"
            + "      <slots>1</slots>\n"
            + "    </job_list>\n"
            + "    <job_list state=\"suspended\">\n"
            + "      <JB_job_number>9</JB_job_number>\n"
            + "      <JAT_prio>0.00000</JAT_prio>\n"
            + "      <JB_name>someName</JB_name>\n"
            + "      <JB_owner>sgeuser</JB_owner>\n"
            + "      <state>s</state>\n"
            + "      <JB_submission_time>2021-06-30T17:27:30</JB_submission_time>\n"
            + "      <queue_name></queue_name>\n"
            + "      <slots>1</slots>\n"
            + "    </job_list>\n"
            + "  </job_info>\n"
            + "</job_info>";

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

    private static final String EMPTY_JOB_LIST = "<?xml version='1.0'?>\n"
            + "<job_info  xmlns:xsd="
            + "\"http://arc.liv.ac.uk/repos/darcs/sge/source/dist/util/resources/schemas/qstat/qstat.xsd\">\n"
            + "  <queue_info>\n"
            + "  </queue_info>\n"
            + "  <job_info>\n"
            + "  </job_info>\n"
            + "</job_info>";

    private static final String START = "<?xml version='1.0'?>\n"
            + "<job_info  xmlns:xsd="
            + "\"http://arc.liv.ac.uk/repos/darcs/sge/source/dist/util/resources/schemas/qstat/qstat.xsd\">\n"
            + " <queue_info>\n"
            + " </queue_info>\n"
            + " <job_info>\n";

    private static final String DATA = "<JAT_prio>0.55500</JAT_prio>\n"
            + "<JAT_start_time>2021-06-30T17:27:30</JAT_start_time>\n"
            + "<queue_name>test_queue</queue_name>\n"
            + "<slots>1</slots>\n"
            + "</job_list>\n";

    private static final String END = " </job_info>\n" + "</job_info>";

    private static final String RUNNING_XML = buildTemplate(Map.of(
            7, List.of(RUNNING_STRING, SOME_JOB_NAME_1, SOME_RUNNING_STATUS_CODE)));

    private static final String TWO_VALID_XML = buildTemplate(Map.of(
            7, List.of(RUNNING_STRING, SOME_JOB_NAME_1, SOME_RUNNING_STATUS_CODE),
            2, List.of(PENDING_STRING, SOME_JOB_NAME_2, SOME_PENDING_STATUS_CODE)));

    private static final String[] DEFAULT_REQUEST = new String[]{QSTAT_COMMAND, STATE, SOME_RUNNING_STATUS_CODE,
                                                                 TYPE_XML};

    @Autowired
    private SgeJobProvider sgeJobProvider;

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @MockBean
    private GridEngineCommandCompiler commandCompiler;

    @MockBean
    private JobFilter mockJobFilter;

    private static String buildTemplate(final Map<Integer, List<String>> jobParams) {
        final String jobBody = jobParams.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    final Integer jobId = e.getKey();
                    final String jobState = e.getValue().get(0);
                    final String jobName = e.getValue().get(1);
                    final String jobCodeState = e.getValue().get(2);
                    return String.format("<job_list state=\"%s\">\n", jobState)
                            + String.format("<JB_job_number>%d</JB_job_number>\n", jobId)
                            + String.format("<JB_name>%s</JB_name>\n", jobName)
                            + "<JB_owner>sgeuser</JB_owner>\n"
                            + String.format("<state>%s</state>\n", jobCodeState);
                }).collect(Collectors.joining(DATA));
        return START + jobBody + DATA + END;
    }

    @Test
    public void shouldFailWithInvalidXml() {
        final List<String> hostList = Collections.singletonList(INVALID_XML);
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(hostList)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);
        Assertions.assertEquals(EngineType.SGE, sgeJobProvider.getProviderType());
        final JobFilter jobFilter = new JobFilter();
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                sgeJobProvider.filterJobs(jobFilter));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    public void shouldNotFailWithEmptyJobList() {
        final List<String> hostList = Collections.singletonList(EMPTY_JOB_LIST);
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(hostList)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);
        final List<Job> result = sgeJobProvider.filterJobs(new JobFilter()).getElements();

        Assertions.assertEquals(0, result.size());
        Assertions.assertEquals(EngineType.SGE, sgeJobProvider.getProviderType());
    }

    @Test
    public void shouldLoadXml() {
        final Job pendingJob = Job.builder()
                .id(2)
                .name(SOME_JOB_NAME_2)
                .priority(0.555)
                .owner(SGEUSER)
                .queueName(TEST_QUEUE)
                .submissionTime(LocalDateTime.parse("2021-06-30T17:27:30"))
                .slots(1)
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .state(PENDING_STRING)
                        .stateCode(SOME_PENDING_STATUS_CODE)
                        .build())
                .build();
        final Job runningJob = runningJobTemplate();
        final List<String> hostList = Collections.singletonList(TWO_VALID_XML);
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(hostList)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);
        final Listing<Job> result = sgeJobProvider.filterJobs(new JobFilter());

        Assertions.assertEquals(2, result.getElements().size());
        Assertions.assertEquals(pendingJob, result.getElements().get(0));
        Assertions.assertEquals(runningJob, result.getElements().get(1));
    }

    @Test
    public void shouldReturnCorrectOwnerFiltration() {
        final Job runningJob = runningJobTemplate();
        final List<String> hostList = Collections.singletonList(RUNNING_XML);
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setOwners(Collections.singletonList(SGEUSER));
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(hostList)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, USER_QDEL, SGEUSER, TYPE_XML);
        final Listing<Job> result = sgeJobProvider.filterJobs(jobFilter);

        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(runningJob, result.getElements().get(0));
    }

    @Test
    public void shouldReturnCorrectStateFiltration() {
        final Job runningJob = runningJobTemplate();
        final List<String> hostList = Collections.singletonList(RUNNING_XML);
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setState(RUNNING_STRING);
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(hostList)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(QSTAT_COMMAND, commandResult, DEFAULT_REQUEST);
        final Listing<Job> result = sgeJobProvider.filterJobs(jobFilter);

        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(runningJob, result.getElements().get(0));
    }

    @Test
    public void shouldReturnCorrectIdFiltration() {
        final Job runningJob = runningJobTemplate();
        final List<String> hostList = Collections.singletonList(TWO_VALID_XML);
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setIds(ID);
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(hostList)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);
        final Listing<Job> result = sgeJobProvider.filterJobs(jobFilter);

        Assertions.assertEquals(1, result.getElements().size());
        Assertions.assertEquals(runningJob, result.getElements().get(0));
    }

    @Test
    public void shouldReturnCorrectNameFiltration() {
        final List<String> hostList = Collections.singletonList(TWO_VALID_XML);
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setNames(NAME);
        final Job runningJob = runningJobTemplate();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(hostList)
                .stdErr(EMPTY_LIST)
                .build();

        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);
        final Listing<Job> result = sgeJobProvider.filterJobs(jobFilter);

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

        mockCommandCompilation(QSTAT_COMMAND, commandResult, QSTAT_COMMAND, TYPE_XML);
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class,
                () -> sgeJobProvider.filterJobs(jobFilter));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideBadParameters")
    public void shouldThrowExceptionDuringBuildingCommand(final String state, final List<String> owners) {
        final JobFilter jobFilter = JobFilter.builder()
                .state(state)
                .owners(owners)
                .build();
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class,
                () -> sgeJobProvider.filterJobs(jobFilter));
        Assertions.assertNotNull(thrown.getMessage());
    }

    static Stream<Arguments> provideBadParameters() {
        return Stream.of(
                Arguments.of("lol", null),
                Arguments.of("had", Collections.singletonList(SGEUSER)),
                Arguments.of("runing", List.of(SGEUSER, SOMEUSER, ANOTHERUSER))
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidJobOptions")
    public void shouldThrowExceptionMakingQsubCommand(final JobOptions jobOptions) {
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class,
                () -> sgeJobProvider.runJob(jobOptions));
        Assertions.assertNotNull(thrown.getMessage());
    }

    static Stream<Arguments> provideInvalidJobOptions() {
        return Stream.of(
                Arguments.of(JobOptions.builder().command(null).build()),
                Arguments.of(JobOptions.builder().command(EMPTY_STRING).build()),
                Arguments.of(JobOptions.builder().parallelEnvOptions(new ParallelEnvOptions(EMPTY_STRING, 1, 100))
                        .command(JOB_COMMAND).build()),
                Arguments.of(JobOptions.builder().parallelEnvOptions(new ParallelEnvOptions(null, 1, 100))
                        .command(JOB_COMMAND).build())
        );
    }

    @Test
    public void shouldThrowsExceptionBecauseNoCommand() {
        final JobOptions jobOptions = new JobOptions();
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                sgeJobProvider.runJob(jobOptions));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    public void shouldThrowExceptionBecauseParallelExecOptionsAreUsed() {
        final JobOptions jobOptions = new JobOptions();
        jobOptions.setCommand(COMMAND_SCRIPT_FILE);
        jobOptions.setParallelExecutionOptions(ParallelExecutionOptions.builder().numTasks(1).numTasksPerNode(1)
                .cpusPerTask(1).nodes(1).build());
        final Throwable thrown = Assertions.assertThrows(UnsupportedOperationException.class, () ->
                sgeJobProvider.runJob(jobOptions));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideCorrectQsubCommands")
    public void shouldReturnScheduledJobIndex(final String[] command) {
        final Job expectedFilteredJob = correctBuild();
        final JobOptions jobOptions = new JobOptions();
        final CommandResult commandResult = new CommandResult();

        jobOptions.setCommand(COMMAND_SCRIPT_FILE);
        commandResult.setStdOut(Collections.singletonList(TEXT_JOB_SUBMITTED));
        commandResult.setStdErr(EMPTY_LIST);

        mockCommandCompilation(QSUB, commandResult, command);
        final Job result = sgeJobProvider.runJob(jobOptions);

        Assertions.assertEquals(expectedFilteredJob.getId(), result.getId());
    }

    static Stream<Arguments> provideCorrectQsubCommands() {
        return mapObjectsToArgumentsStream(
                new String[]{QSUB, "-wd", "/out/", COMMAND_SCRIPT_FILE},
                new String[]{QSUB, "-pe", EMPTY_STRING, "1-100", COMMAND_SCRIPT_FILE},
                new String[]{QSUB, "-pe", EMPTY_STRING, "100-10", COMMAND_SCRIPT_FILE},
                new String[]{QSUB, "-pe", EMPTY_STRING, "1-0", COMMAND_SCRIPT_FILE},
                new String[]{QSUB, "-pe", EMPTY_STRING, "1", COMMAND_SCRIPT_FILE}
        );
    }

    @Test
    public void shouldReturnRightJob() {
        final Job expectedFilteredJob = correctBuild();
        final JobOptions jobOptions = new JobOptions();
        final CommandResult commandResult = new CommandResult();
        jobOptions.setCommand(COMMAND_SCRIPT_FILE);

        commandResult.setStdOut(Collections.singletonList(TEXT_JOB_SUBMITTED));
        commandResult.setStdErr(EMPTY_LIST);

        mockCommandCompilation(QSUB, commandResult, QSUB, COMMAND_SCRIPT_FILE);
        final Job result = sgeJobProvider.runJob(jobOptions);

        Assertions.assertEquals(expectedFilteredJob, result);
    }

    private static Job correctBuild() {
        return Job.builder()
                .id(runningJobTemplate().getId())
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .build())
                .build();
    }

    private static Job runningJobTemplate() {
        return Job.builder()
                .id(7)
                .name(SOME_JOB_NAME_1)
                .priority(0.555)
                .owner(SGEUSER)
                .queueName(TEST_QUEUE)
                .submissionTime(LocalDateTime.parse("2021-06-30T17:27:30"))
                .slots(1)
                .state(JobState.builder()
                        .category(JobState.Category.RUNNING)
                        .state(RUNNING_STRING)
                        .stateCode(SOME_RUNNING_STATUS_CODE).build())
                .build();
    }

    @Test
    public void shouldReturnCorrectDeletedJobInfo() {
        final DeleteJobFilter deleteJobFilter = DeleteJobFilter.builder()
                .force(false)
                .id(1L)
                .user(SGEUSER)
                .build();
        final DeletedJobInfo expectedDeletedJobInfo = DeletedJobInfo.builder()
                .ids(List.of(1L))
                .user(SGEUSER)
                .build();
        final CommandResult commandResult = new CommandResult();
        commandResult.setStdOut(List.of(SUCCESSFULLY_DELETED));
        mockCommandCompilation(QDEL_COMMAND, commandResult, QDEL_COMMAND, USER_QDEL, SGEUSER, ONE);
        doReturn(commandResult).when(mockCmdExecutor).execute(QDEL_COMMAND, USER_QDEL, SGEUSER, ONE);
        final DeletedJobInfo result = sgeJobProvider.deleteJob(deleteJobFilter);
        Assertions.assertEquals(expectedDeletedJobInfo, result);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectCommands")
    public void shouldReturnResponseAndDontThrowsException(final String[] command) {
        final DeleteJobFilter deleteJobFilter = DeleteJobFilter.builder()
                .force(false)
                .id(1L)
                .user(SGEUSER)
                .build();
        final DeletedJobInfo expectedDeletedJobInfo = DeletedJobInfo.builder()
                .ids(List.of(1L))
                .user(SGEUSER)
                .build();
        final CommandResult commandResult = new CommandResult();
        commandResult.setStdOut(List.of(SUCCESSFULLY_DELETED));
        mockCommandCompilation(QDEL_COMMAND, commandResult, command);
        doReturn(commandResult).when(mockCmdExecutor).execute(command);
        final DeletedJobInfo result = sgeJobProvider.deleteJob(deleteJobFilter);
        Assertions.assertEquals(expectedDeletedJobInfo, result);
    }

    static Stream<Arguments> provideCorrectCommands() {
        return mapObjectsToArgumentsStream(
                new String[]{QDEL_COMMAND, FOUR},
                new String[]{QDEL_COMMAND, USER_QDEL, SGEUSER},
                new String[]{QDEL_COMMAND, ELEVEN},
                new String[]{QDEL_COMMAND, USER_QDEL, SGEUSER, ONE},
                new String[]{QDEL_COMMAND, FORCED_QDEL, EIGHT},
                new String[]{QDEL_COMMAND, FORCED_QDEL, SGEUSER},
                new String[]{QDEL_COMMAND, FORCED_QDEL, SGEUSER, THIRTEEN}
        );
    }

    @ParameterizedTest
    @MethodSource("provideBadCommands")
    public void shouldThrowsExceptionWithNotEmptyMessage(final String[] command) {
        final MockQdelCmdExecutor mockQdelCmdExecutor = new MockQdelCmdExecutor();
        final CommandResult commandResult = mockQdelCmdExecutor.execute(command);
        mockCommandCompilation(QDEL_COMMAND, commandResult, command);
        doReturn(commandResult).when(mockCmdExecutor).execute(command);
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                sgeJobProvider.deleteJob(new DeleteJobFilter()));
        Assertions.assertNotNull(thrown.getMessage());
    }

    static Stream<Arguments> provideBadCommands() {
        return mapObjectsToArgumentsStream(
                new String[]{QDEL_COMMAND, FORCED_QDEL, SGE_USER},
                new String[]{QDEL_COMMAND, USER_QDEL, SGE_USER},
                new String[]{QDEL_COMMAND, NINE},
                new String[]{QDEL_COMMAND, USER_QDEL},
                new String[]{QDEL_COMMAND, FORCED_QDEL, NINE}
        );
    }

    private static class MockQdelCmdExecutor implements CmdExecutor {
        @Override
        public final CommandResult execute(final String... arguments) {
            final int exitCode;
            final List<String> out = new ArrayList<>();
            final List<String> err = new ArrayList<>();
            final String argumentsAsString = Arrays.stream(arguments).map(Object::toString)
                    .collect(Collectors.joining(SPACE));

            if (Arrays.asList(arguments).contains(NINE)) {
                exitCode = 1;
                out.add(JOB_DOES_NOT_EXISTS);
            } else if (Arrays.asList(arguments).contains(FORCED_QDEL)
                    && (Arrays.asList(arguments).contains(SGEUSER)
                    || argumentsAsString.matches(NUMBER_FIND_PATTERN))) {
                exitCode = 1;
                out.add(HAS_DELETED_JOB);
            } else if (Arrays.asList(arguments).contains(USER_QDEL) && !Arrays.asList(arguments).contains(SGEUSER)) {
                exitCode = 1;
                out.add(HAS_NOT_SUBMITTED_JOB);
            } else if (argumentsAsString.matches(NUMBER_FIND_PATTERN)
                    || Arrays.asList(arguments).contains(USER_QDEL)
                    && Arrays.asList(arguments).contains(SGEUSER)) {
                exitCode = 0;
                out.add(HAS_DELETED_JOB);
            } else {
                exitCode = 1;
                out.add(JOB_DOES_NOT_EXISTS);
            }
            return new CommandResult(out, exitCode, err);
        }
    }

    private static Stream<Arguments> mapObjectsToArgumentsStream(final Object... args) {
        return Stream.of(args).map(Arguments::of);
    }

    private void mockCommandCompilation(final String command, final CommandResult commandResult,
                                        final String... compiledArray) {
        doReturn(compiledArray).when(commandCompiler).compileCommand(Mockito.eq(EngineType.SGE),
                Mockito.matches(command),
                Mockito.any());
        doReturn(commandResult).when(mockCmdExecutor).execute(compiledArray);
    }

    @ParameterizedTest
    @MethodSource("provideWrongDeleteRequests")
    public void shouldThrowsExceptionDuringDeletionBecauseNotCorrectRequest(final boolean isForce, final Long id,
                                                                            final String user) {
        final DeleteJobFilter deleteJobFilter = DeleteJobFilter.builder()
                .force(isForce)
                .id(id)
                .user(user)
                .build();
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                sgeJobProvider.deleteJob(deleteJobFilter));
        Assertions.assertNotNull(thrown.getMessage());
    }

    static Stream<Arguments> provideWrongDeleteRequests() {
        return Stream.of(
                Arguments.of(false, 0L, SGEUSER),
                Arguments.of(true, null, EMPTY_STRING),
                Arguments.of(false, null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void shouldReturnCorrectStateCommand(final String jobState) {
        final List<Job> expectedFilteredJob = correctJobFilling();

        final CommandResult commandResult = new CommandResult();
        commandResult.setStdOut(Collections.singletonList(VALID_XML));
        commandResult.setStdErr(EMPTY_LIST);

        mockCommandCompilation(QSTAT_COMMAND, commandResult, TYPE_XML);
        doReturn(jobState).when(mockJobFilter).getState();
        final Listing<Job> result = sgeJobProvider.filterJobs(null);

        if (jobState.equals(PENDING_STRING)) {
            Assertions.assertEquals(expectedFilteredJob.get(1), result.getElements().get(0));
        }
        if (jobState.equals(SUSPENDED_STRING)) {
            Assertions.assertEquals(expectedFilteredJob.get(2), result.getElements().get(1));
        }
        if (jobState.equals(RUNNING_STRING)) {
            Assertions.assertEquals(expectedFilteredJob.get(0), result.getElements().get(2));
        }
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                        RUNNING_STRING,
                        PENDING_STRING,
                        SUSPENDED_STRING)
                .map(Arguments::of);
    }

    private static List<Job> correctJobFilling() {
        final Job running = Job.builder()
                .id(8)
                .name(SOME_JOB_NAME_1)
                .priority(0.55500)
                .owner(SGEUSER)
                .queueName("main@c242f10e1253")
                .submissionTime(LocalDateTime.parse("2021-07-02T10:46:14"))
                .slots(1)
                .state(JobState
                        .builder()
                        .category(JobState.Category.RUNNING)
                        .state(RUNNING_STRING)
                        .stateCode("r")
                        .build())
                .build();
        final Job pending = Job.builder()
                .id(2)
                .name(SOME_JOB_NAME_1)
                .priority(0.00000)
                .owner(SGEUSER)
                .queueName(EMPTY_STRING)
                .submissionTime(LocalDateTime.parse("2021-06-30T17:27:30"))
                .slots(1)
                .state(JobState
                        .builder()
                        .category(JobState.Category.PENDING)
                        .state(PENDING_STRING)
                        .stateCode("qw")
                        .build())
                .build();
        final Job suspended = Job.builder()
                .id(9)
                .name(SOME_JOB_NAME_1)
                .priority(0.00000)
                .owner(SGEUSER)
                .queueName(EMPTY_STRING)
                .submissionTime(LocalDateTime.parse("2021-06-30T17:27:30"))
                .slots(1)
                .state(JobState
                        .builder()
                        .category(JobState.Category.SUSPENDED)
                        .state(SUSPENDED_STRING)
                        .stateCode("s")
                        .build())
                .build();
        return Arrays.asList(running, pending, suspended);
    }

    @Test
    void shouldReturnCorrectObjectWhenGettingJobLogInfo() {
        final List<String> testStdOut = Collections.singletonList("Test line for StdOut.");
        final JobLogInfo expectedJobLogInfo = new JobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE,
                testStdOut, SOME_LINES, SOME_BYTES);

        final CommandResult infoCommandResult = new CommandResult();
        infoCommandResult.setStdOut(INFO_COMMAND_RESULT_STDOUT);
        infoCommandResult.setStdErr(EMPTY_LIST);

        final CommandResult linesCommandResult = new CommandResult();
        linesCommandResult.setStdOut(testStdOut);
        linesCommandResult.setStdErr(EMPTY_LIST);

        mockCommandCompilation(GET_LOGFILE_INFO_COMMAND, infoCommandResult, "wc", "-l", "-c", LOG_FILE_NAME);
        mockCommandCompilation(GET_LOG_LINES_COMMAND, linesCommandResult, "tail", "-n", "1", LOG_FILE_NAME);

        final JobLogInfo result = sgeJobProvider.getJobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE, 1, false);
        Assertions.assertEquals(expectedJobLogInfo, result);
    }

    @Test
    void shouldTrowsExceptionWhenGettingJobLogInfoWithBadRequest() {
        final int someBadCountLines = -5;
        final GridEngineException thrown = Assertions.assertThrows(GridEngineException.class,
                () -> sgeJobProvider.getJobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE, someBadCountLines, false));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, thrown.getHttpStatus());
    }

    @ParameterizedTest
    @MethodSource("provideCommandResultsForGettingJobLogInfoTesting")
    void shouldTrowsExceptionWhenGettingJobLogInfoWhenReceivedBadCommandResult(
            final int infoCommandResultStatus,
            final int linesCommandResultStatus,
            final List<String> infoCommandResultStdOut,
            final HttpStatus expectedHttpStatus) {
        final CommandResult infoCommandResult = new CommandResult();
        infoCommandResult.setStdOut(infoCommandResultStdOut);
        infoCommandResult.setExitCode(infoCommandResultStatus);

        final CommandResult linesCommandResult = new CommandResult();
        linesCommandResult.setExitCode(linesCommandResultStatus);

        mockCommandCompilation(GET_LOGFILE_INFO_COMMAND, infoCommandResult, "wc", "-l", "-c", LOG_FILE_NAME);
        mockCommandCompilation(GET_LOG_LINES_COMMAND, linesCommandResult, "tail", "-n", "1", LOG_FILE_NAME);

        final GridEngineException result = Assertions.assertThrows(GridEngineException.class,
                () -> sgeJobProvider.getJobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE, 1, false));
        Assertions.assertEquals(expectedHttpStatus, result.getHttpStatus());
    }

    static Stream<Arguments> provideCommandResultsForGettingJobLogInfoTesting() {
        return Stream.of(
                Arguments.of(0, 1, INFO_COMMAND_RESULT_STDOUT, HttpStatus.NOT_FOUND),
                Arguments.of(1, 0, INFO_COMMAND_RESULT_STDOUT, HttpStatus.NOT_FOUND),
                Arguments.of(1, 1, INFO_COMMAND_RESULT_STDOUT, HttpStatus.NOT_FOUND),
                Arguments.of(0, 0, Collections.singletonList(LOG_FILE_NAME), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
