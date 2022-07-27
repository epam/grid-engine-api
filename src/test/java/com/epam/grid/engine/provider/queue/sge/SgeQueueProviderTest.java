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

package com.epam.grid.engine.provider.queue.sge;

import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.QueueFilter;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.QueueVO;
import com.epam.grid.engine.entity.queue.SlotsDescription;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_STRING;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.SINGLETON_LIST_WITH_STANDARD_WARN;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.SPACE;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(properties = {"grid.engine.type=SGE"})
public class SgeQueueProviderTest {

    private static final List<String> validQueue = List.of(
            "qname                 all.q",
            "hostlist              @allhosts",
            "seq_no                0",
            "load_thresholds       np_load_avg=1.75",
            "suspend_thresholds    NONE",
            "nsuspend              1",
            "suspend_interval      00:05:00",
            "priority              0",
            "min_cpu_interval      00:05:00",
            "processors            UNDEFINED",
            "qtype                 BATCH INTERACTIVE",
            "ckpt_list             NONE",
            "pe_list               make smp mpi",
            "rerun                 FALSE",
            "slots                 1,[863431bb452c=1]",
            "tmpdir                /tmp",
            "shell                 /bin/sh",
            "prolog                NONE",
            "epilog                NONE",
            "shell_start_mode      posix_compliant",
            "starter_method        NONE",
            "suspend_method        NONE",
            "resume_method         NONE",
            "terminate_method      NONE",
            "notify                00:00:60",
            "owner_list            NONE",
            "user_lists            NONE",
            "xuser_lists           NONE",
            "subordinate_list      NONE",
            "complex_values        NONE",
            "projects              NONE",
            "xprojects             NONE",
            "calendar              NONE",
            "initial_state         default",
            "s_rt                  INFINITY",
            "h_rt                  INFINITY",
            "s_cpu                 INFINITY",
            "h_cpu                 INFINITY",
            "s_fsize               INFINITY",
            "h_fsize               INFINITY",
            "s_data                INFINITY",
            "h_data                INFINITY",
            "s_stack               INFINITY",
            "h_stack               INFINITY",
            "s_core                INFINITY",
            "h_core                INFINITY",
            "s_rss                 INFINITY",
            "h_rss                 INFINITY",
            "s_vmem                INFINITY",
            "h_vmem                INFINITY");
    private static final String COMMAND_QCONF = "qconf";
    private static final String OPTION_SQL = "-sql";
    private static final String OPTION_SQ = "-sq";
    private static final String OPTION_DQ = "-dq";
    private static final String OPTION_MQ = "-Mq";
    private static final String QUEUE_NAME1 = "all.q";
    private static final String QUEUE_NAME2 = "main";
    private static final String OPTION_AQ = "-Aq";
    private static final String QUEUE_NAME_1 = "all.q";
    private static final String QUEUE_NAME_3 = "queue";
    private static final String TEST_HOST_LIST = "host_1 host_2 host_3";
    private static final String TEST_PE_LIST = "pe_1 pe_2 pe_3";
    private static final String TEST_USER_LIST = "user_1 user_2 user_3";
    private static final String TEST_OWNER_LIST = "owner_1 owner_2 owner_3";
    private static final String QUEUE_PROPERTY_REGEX = "\\s\\S.++";
    private static final List<String> REMOVED_1 = Collections
            .singletonList("user removed \"all.q\" from cluster queue list");
    private static final List<String> REMOVED_2 = Collections.singletonList("error: invalid option argument main");
    private static final List<String> queues = Arrays.asList(QUEUE_NAME1, QUEUE_NAME2);
    private static final Integer INTEGER_ZERO = 0;
    private static final Integer INTEGER_ONE = 1;

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @Autowired
    private SgeQueueProvider sgeQueueProvider;

    private final CommandResult commandResult = new CommandResult();
    private final SlotsDescription slotsDescription = new SlotsDescription(1,
            Map.of("863431bb452c", 1));

    @Test
    public void shouldReturnCorrectQueueInformation() {
        final Queue expectedQueue = Queue.builder()
                .name("all.q")
                .hostList(Collections.singletonList("@allhosts"))
                .numberInSchedulingOrder(INTEGER_ZERO)
                .loadThresholds(Map.of("np_load_avg", 1.75))
                .suspendThresholds(Map.of())
                .numOfSuspendedJobs(INTEGER_ONE)
                .interval("00:05:00")
                .jobPriority(INTEGER_ZERO)
                .qtype("BATCH INTERACTIVE")
                .parallelEnvironmentNames(List.of("make", "smp", "mpi"))
                .ownerList(EMPTY_LIST)
                .allowedUserGroups(EMPTY_LIST)
                .slots(slotsDescription)
                .tmpDir("/tmp")
                .build();
        commandResult.setStdOut(validQueue);
        commandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);
        doReturn(commandResult).when(mockCmdExecutor).execute(COMMAND_QCONF, OPTION_SQ, QUEUE_NAME1);
        final QueueFilter queueFilter = new QueueFilter();
        queueFilter.setQueues(List.of(QUEUE_NAME1));
        doReturn(commandResult).when(mockCmdExecutor).execute(COMMAND_QCONF, OPTION_SQ, QUEUE_NAME_1);
        queueFilter.setQueues(List.of(QUEUE_NAME_1));
        final List<Queue> resultQueue = sgeQueueProvider.listQueues(queueFilter);

        Assertions.assertEquals(expectedQueue, resultQueue.get(0));
    }

    @Test
    public void shouldListQueues() {
        final List<String> expected = Arrays.asList(QUEUE_NAME1, QUEUE_NAME2);
        commandResult.setStdOut(queues);
        commandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);
        doReturn(commandResult).when(mockCmdExecutor).execute(COMMAND_QCONF, OPTION_SQL);
        final List<Queue> queues = sgeQueueProvider.listQueues();
        final List<String> result = new ArrayList<>();
        for (Queue queue : queues) {
            result.add(queue.getName());
        }

        Assertions.assertEquals(CommandType.SGE, sgeQueueProvider.getProviderType());
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void shouldFailIfExitCodeNotNull() {
        final QueueFilter queueFilter = new QueueFilter();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(EMPTY_LIST)
                .exitCode(1)
                .build();

        doReturn(commandResult).when(mockCmdExecutor).execute(COMMAND_QCONF, OPTION_SQL);

        final Throwable emptyListThrown = Assertions.assertThrows(GridEngineException.class,
                sgeQueueProvider::listQueues);
        final Throwable filledListThrown = Assertions.assertThrows(GridEngineException.class,
                () -> sgeQueueProvider.listQueues(queueFilter));
        Assertions.assertNotNull(emptyListThrown.getMessage());
        Assertions.assertNotNull(filledListThrown.getMessage());
    }

    @Test
    public void shouldThrowsExceptionBecauseIncorrectCommand() {
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                sgeQueueProvider.deleteQueues(null));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    public void shouldReturnCorrectDeleteResponse() {
        final CommandResult commandResultOne = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(REMOVED_1)
                .exitCode(0)
                .build();
        final CommandResult commandResultTwo = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(REMOVED_2)
                .exitCode(1)
                .build();

        doReturn(commandResultOne).when(mockCmdExecutor).execute(COMMAND_QCONF, OPTION_DQ, QUEUE_NAME1);
        doReturn(commandResultTwo).when(mockCmdExecutor).execute(COMMAND_QCONF, OPTION_DQ, QUEUE_NAME2);
        final Queue response = sgeQueueProvider.deleteQueues(QUEUE_NAME1);
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                sgeQueueProvider.deleteQueues(QUEUE_NAME2));
        final Queue expected = Queue.builder()
                .name(QUEUE_NAME1)
                .build();

        Assertions.assertEquals(expected, response);
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    public void registerQueueShouldInvokeRegisterCommandExecution() {
        final List<String> validRegisteredQueue = new ArrayList<>(validQueue);
        replaceListValue(validRegisteredQueue, "qname", QUEUE_NAME_3);
        replaceListValue(validRegisteredQueue, "hostlist", TEST_HOST_LIST);
        replaceListValue(validRegisteredQueue, "pe_list", TEST_PE_LIST);
        replaceListValue(validRegisteredQueue, "owner_list", TEST_OWNER_LIST);
        replaceListValue(validRegisteredQueue, "user_lists", TEST_USER_LIST);
        final Queue expectedQueue = Queue.builder()
                .name(QUEUE_NAME_3)
                .hostList(Arrays.asList(TEST_HOST_LIST.split(SPACE)))
                .parallelEnvironmentNames(Arrays.asList(TEST_PE_LIST.split(SPACE)))
                .ownerList(Arrays.asList(TEST_OWNER_LIST.split(SPACE)))
                .allowedUserGroups(Arrays.asList(TEST_USER_LIST.split(SPACE)))
                .build();
        final QueueVO registrationRequest
                = QueueVO.builder()
                .name(expectedQueue.getName())
                .hostList(expectedQueue.getHostList())
                .parallelEnvironmentNames(expectedQueue.getParallelEnvironmentNames())
                .ownerList(expectedQueue.getOwnerList())
                .allowedUserGroups(expectedQueue.getAllowedUserGroups())
                .build();
        commandResult.setStdOut(validRegisteredQueue);
        commandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);
        doReturn(commandResult).when(mockCmdExecutor)
                .execute(Mockito.matches(COMMAND_QCONF), Mockito.matches(OPTION_AQ), Mockito.anyString());
        final Queue resultQueue = sgeQueueProvider.registerQueue(registrationRequest);

        Assertions.assertEquals(CommandType.SGE, sgeQueueProvider.getProviderType());
        Assertions.assertEquals(expectedQueue.getName(), resultQueue.getName());
        Assertions.assertEquals(expectedQueue.getHostList(), resultQueue.getHostList());
        Assertions.assertEquals(expectedQueue.getParallelEnvironmentNames(), resultQueue.getParallelEnvironmentNames());
        Assertions.assertEquals(expectedQueue.getOwnerList(), resultQueue.getOwnerList());
        Assertions.assertEquals(expectedQueue.getAllowedUserGroups(), resultQueue.getAllowedUserGroups());
    }

    @Test
    public void shouldFailIfExitCodeNotNullOnRegisterCommand() {
        final QueueVO registrationRequest
                = QueueVO.builder()
                .name(QUEUE_NAME_3)
                .build();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(EMPTY_LIST)
                .exitCode(1)
                .build();

        doReturn(commandResult).when(mockCmdExecutor)
                .execute(Mockito.matches(COMMAND_QCONF), Mockito.matches(OPTION_AQ), Mockito.anyString());

        final Throwable registrationUnsuccessfulException = Assertions.assertThrows(GridEngineException.class,
                () -> sgeQueueProvider.registerQueue(registrationRequest));

        Assertions.assertNotNull(registrationUnsuccessfulException.getMessage());
    }

    @Test
    public void shouldFailIfRequestIsIncorrectOnRegisterCommand() {
        final QueueVO registrationRequest
                = QueueVO.builder()
                .name(EMPTY_STRING)
                .build();

        final Throwable emptyQueueNameException = Assertions.assertThrows(GridEngineException.class,
                () -> sgeQueueProvider.registerQueue(registrationRequest));
        Assertions.assertNotNull(emptyQueueNameException.getMessage());
    }

    @Test
    public void updateQueueShouldInvokeUpdateCommandExecution() {
        final List<String> validModifiedQueue = new ArrayList<>(validQueue);
        replaceListValue(validModifiedQueue, "qname", QUEUE_NAME_3);
        replaceListValue(validModifiedQueue, "hostlist", TEST_HOST_LIST);
        replaceListValue(validModifiedQueue, "pe_list", TEST_PE_LIST);
        replaceListValue(validModifiedQueue, "owner_list", TEST_OWNER_LIST);
        replaceListValue(validModifiedQueue, "user_lists", TEST_USER_LIST);
        final Queue expectedQueue = Queue.builder()
                .name(QUEUE_NAME_3)
                .hostList(Arrays.asList(TEST_HOST_LIST.split(SPACE)))
                .parallelEnvironmentNames(Arrays.asList(TEST_PE_LIST.split(SPACE)))
                .ownerList(Arrays.asList(TEST_OWNER_LIST.split(SPACE)))
                .allowedUserGroups(Arrays.asList(TEST_USER_LIST.split(SPACE)))
                .build();
        final QueueVO updateRequest
                = QueueVO.builder()
                .name(expectedQueue.getName())
                .hostList(expectedQueue.getHostList())
                .parallelEnvironmentNames(expectedQueue.getParallelEnvironmentNames())
                .ownerList(expectedQueue.getOwnerList())
                .allowedUserGroups(expectedQueue.getAllowedUserGroups())
                .build();
        final CommandResult listQueueCommandResult = new CommandResult();
        listQueueCommandResult.setStdOut(validQueue);
        listQueueCommandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);
        doReturn(listQueueCommandResult).when(mockCmdExecutor)
                        .execute(Mockito.matches(COMMAND_QCONF), Mockito.matches(OPTION_SQ), Mockito.anyString());

        commandResult.setStdOut(validModifiedQueue);
        commandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);
        doReturn(commandResult).when(mockCmdExecutor)
                .execute(Mockito.matches(COMMAND_QCONF), Mockito.matches(OPTION_MQ), Mockito.anyString());
        final Queue resultQueue = sgeQueueProvider.updateQueue(updateRequest);

        Assertions.assertEquals(CommandType.SGE, sgeQueueProvider.getProviderType());
        Assertions.assertEquals(expectedQueue.getName(), resultQueue.getName());
        Assertions.assertEquals(expectedQueue.getHostList(), resultQueue.getHostList());
        Assertions.assertEquals(expectedQueue.getParallelEnvironmentNames(), resultQueue.getParallelEnvironmentNames());
        Assertions.assertEquals(expectedQueue.getOwnerList(), resultQueue.getOwnerList());
        Assertions.assertEquals(expectedQueue.getAllowedUserGroups(), resultQueue.getAllowedUserGroups());
    }

    @ParameterizedTest
    @MethodSource("provideUpdateRequests")
    public void shouldFailIfRequestIsIncorrectOnUpdateCommand(final QueueVO updateRequest) {
        final Throwable incorrectRequestException = Assertions.assertThrows(GridEngineException.class,
                () -> sgeQueueProvider.updateQueue(updateRequest));
        Assertions.assertNotNull(incorrectRequestException.getMessage());
    }

    static Stream<Arguments> provideUpdateRequests() {
        final QueueVO updateRequestEmptyName = QueueVO.builder()
                .name(EMPTY_STRING)
                .hostList(Arrays.asList(TEST_HOST_LIST.split(SPACE)))
                .build();
        final QueueVO updateRequestWithNoParams = QueueVO.builder()
                .name(QUEUE_NAME_3)
                .build();
        return Stream.of(
                Arguments.of(updateRequestEmptyName),
                Arguments.of(updateRequestWithNoParams)
        );
    }

    @Test
    public void shouldFailIfExitCodeNotNullOnUpdateCommand() {
        final QueueVO updateRequest = QueueVO.builder()
                .name(QUEUE_NAME_3)
                .hostList(Arrays.asList(TEST_HOST_LIST.split(SPACE)))
                .build();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(EMPTY_LIST)
                .exitCode(1)
                .build();

        final CommandResult listQueueCommandResult = new CommandResult();
        listQueueCommandResult.setStdOut(validQueue);
        listQueueCommandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);

        doReturn(listQueueCommandResult).when(mockCmdExecutor)
                .execute(Mockito.matches(COMMAND_QCONF), Mockito.matches(OPTION_SQ), Mockito.anyString());
        doReturn(commandResult).when(mockCmdExecutor)
                .execute(Mockito.matches(COMMAND_QCONF), Mockito.matches(OPTION_MQ), Mockito.anyString());

        final Throwable updateUnsuccessfulException = Assertions.assertThrows(GridEngineException.class,
                () -> sgeQueueProvider.updateQueue(updateRequest));
        Assertions.assertNotNull(updateUnsuccessfulException.getMessage());
    }

    private static void replaceListValue(final List<String> list, final String key, final String replacement) {
        for (int i = 0; i < list.size(); i++) {
            final String line = list.get(i);
            if (line.startsWith(key)) {
                list.set(i, line.replaceAll(QUEUE_PROPERTY_REGEX, replacement));
            }
        }
    }
}
