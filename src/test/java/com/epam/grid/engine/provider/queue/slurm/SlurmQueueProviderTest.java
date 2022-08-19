package com.epam.grid.engine.provider.queue.slurm;

import com.epam.grid.engine.TestPropertiesWithSlurmEngine;
import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
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

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.Mockito.doReturn;

@SpringBootTest
@TestPropertiesWithSlurmEngine
public class SlurmQueueProviderTest {

    private static final List<String> validStdout = List.of("newPartition|worker1|1|ALL");
    private static final List<String> validListOut = List.of("normal|worker1|1|ALL", "partition1|worker1|1|admin",
            "partition1|worker2|1|admin", "partition2|worker2|1|slurm");
    private static final List<String> validStdoutUpdatedPartition = List.of(
            "updatingPartitionName|worker1|1|ALL", "updatingPartitionName|worker2|1|ALL");
    private static final List<String> validStdoutPartitionOneNode = List.of("updatingPartitionName|worker1|2|ALL");
    private static final String[] UPDATE_COMMAND_PATTERN = new String[]{"scontrol UPDATE "
            + "PartitionName=updatingPartitionName AllowGroups=ALL Nodes=worker1,worker2"};
    private static final String[] SINFO_COMMAND_PATTERN = new String[]{"sinfo --partition=updatingPartitionName "
            + " -h -o \"%R|%N|%c|%g\""};
    private static final String SCONTROL_COMMAND_TEMPLATE = "scontrol";
    private static final String SINFO_COMMAND_TEMPLATE = "sinfo";
    private static final String UPDATING_PARTITION_NAME = "updatingPartition";
    private static final String NEW_PARTITION_NAME = "newPartition";
    private static final String DELETED_PARTITION_NAME = "deletedPartition";
    private static final List<String> ownerList = List.of("root", "slurm");
    private static final List<String> parallelEnvList = List.of("someVar1", "someVar1");
    private static final List<String> nodeListOneNode = List.of("worker1");
    private static final List<String> nodeListTwoNodes = List.of("worker1", "worker2");
    private static final List<String> groupsList = List.of("ALL");

    private final Map<String, Integer> slotDescriptionOneNode = Map.of("worker1", 1);

    @Autowired
    private SlurmQueueProvider slurmQueueProvider;

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @MockBean
    private GridEngineCommandCompiler mockCommandCompiler;

    @ParameterizedTest
    @MethodSource("incorrectDataForPartitionCreateAndUpdate")
    public void registerAndUpdatePartitionIncorrectDataException(final QueueVO incorrectRequest) {
        final GridEngineException gridExceptionCreate = Assertions.assertThrows(GridEngineException.class,
                () -> slurmQueueProvider.registerQueue(incorrectRequest));
        Assertions.assertNotNull(gridExceptionCreate.getMessage());

        final GridEngineException gridExceptionUpdate = Assertions.assertThrows(GridEngineException.class,
                () -> slurmQueueProvider.updateQueue(incorrectRequest));
        Assertions.assertNotNull(gridExceptionUpdate.getMessage());
    }

    static Stream<Arguments> incorrectDataForPartitionCreateAndUpdate() {
        return Stream.of(
                Arguments.of(QueueVO.builder()
                        .name(NEW_PARTITION_NAME)
                        .ownerList(ownerList)
                        .build()),
                Arguments.of(QueueVO.builder()
                        .name(NEW_PARTITION_NAME)
                        .parallelEnvironmentNames(parallelEnvList)
                        .build()),
                Arguments.of(QueueVO.builder()
                        .allowedUserGroups(groupsList)
                        .hostList(nodeListTwoNodes)
                        .build())
        );
    }

    @Test
    public void registerPartitionSuccessful() {
        final CommandResult creationResult = CommandResult.builder()
                .stdOut(validStdout)
                .stdErr(Collections.emptyList())
                .build();
        doReturn(creationResult).when(mockCmdExecutor).execute(Mockito.any());

        final QueueVO correctRequest = QueueVO.builder()
                .name(NEW_PARTITION_NAME)
                .hostList(nodeListOneNode)
                .allowedUserGroups(groupsList)
                .build();
        final Queue createdPartitionEntity = Queue.builder()
                .name(NEW_PARTITION_NAME)
                .allowedUserGroups(groupsList)
                .hostList(nodeListOneNode)
                .slots(new SlotsDescription(1, slotDescriptionOneNode))
                .build();
        final Queue createdPartition = slurmQueueProvider.registerQueue(correctRequest);
        Assertions.assertEquals(createdPartitionEntity, createdPartition);
    }

    @Test
    public void updatePartitionNoChangesInPartitionException() {
        final CommandResult sinfoResult = CommandResult.builder()
                .stdOut(validStdoutUpdatedPartition)
                .stdErr(Collections.emptyList())
                .build();
        doReturn(sinfoResult).when(mockCmdExecutor).execute(Mockito.any());

        final QueueVO updateRequest = QueueVO.builder()
                .name("updatingPartitionName")
                .hostList(nodeListTwoNodes)
                .allowedUserGroups(groupsList)
                .build();
        final GridEngineException gridExceptionNoChanges = Assertions.assertThrows(GridEngineException.class,
                () -> slurmQueueProvider.updateQueue(updateRequest));
        Assertions.assertEquals("New partition properties and the current one are equal",
                gridExceptionNoChanges.getMessage());
    }

    @Test
    public void updatePartitionNotFoundException() {
        final CommandResult sinfoEmptyResult = CommandResult.builder()
                .stdOut(Collections.emptyList())
                .stdErr(Collections.emptyList())
                .build();
        doReturn(sinfoEmptyResult).when(mockCmdExecutor).execute(Mockito.any());
        final QueueVO updateRequestWithIncorrectPartitionName = QueueVO.builder()
                .name("nonexistentPartitionName")
                .hostList(nodeListTwoNodes)
                .allowedUserGroups(groupsList)
                .build();
        final GridEngineException gridExceptionPartitionNotFound = Assertions.assertThrows(GridEngineException.class,
                () -> slurmQueueProvider.updateQueue(updateRequestWithIncorrectPartitionName));
        Assertions.assertNotNull(gridExceptionPartitionNotFound.getMessage());
    }

    @Test
    public void updatePartitionSuccessful() {
        final CommandResult sinfoResult = CommandResult.builder()
                .stdOut(validStdoutPartitionOneNode)
                .stdErr(Collections.emptyList())
                .build();
        doReturn(SINFO_COMMAND_PATTERN).when(mockCommandCompiler).compileCommand(Mockito.eq(CommandType.SLURM),
                Mockito.matches(SINFO_COMMAND_TEMPLATE), Mockito.any());
        doReturn(sinfoResult).when(mockCmdExecutor).execute(SINFO_COMMAND_PATTERN);

        final CommandResult updateCommandResult = CommandResult.builder()
                .stdErr(Collections.emptyList())
                .build();
        doReturn(UPDATE_COMMAND_PATTERN).when(mockCommandCompiler).compileCommand(Mockito.eq(CommandType.SLURM),
                Mockito.matches(SCONTROL_COMMAND_TEMPLATE), Mockito.any());
        doReturn(updateCommandResult).when(mockCmdExecutor).execute(UPDATE_COMMAND_PATTERN);

        final QueueVO updateRequest = QueueVO.builder()
                .name(UPDATING_PARTITION_NAME)
                .hostList(nodeListTwoNodes)
                .allowedUserGroups(groupsList)
                .build();
        Assertions.assertDoesNotThrow(() -> slurmQueueProvider.updateQueue(updateRequest));
    }

    @Test
    public void listPartitionsSuccessful() {
        final CommandResult listResult = CommandResult.builder()
                .stdOut(validListOut)
                .stdErr(Collections.emptyList())
                .build();
        doReturn(listResult).when(mockCmdExecutor).execute(Mockito.any());

        final List<Queue> partitionsName = List.of(
                Queue.builder()
                        .name("normal")
                        .build(),
                Queue.builder()
                        .name("partition1")
                        .build(),
                Queue.builder()
                        .name("partition2")
                        .build());
        final List<Queue> partitionNames = slurmQueueProvider.listQueues();
        Assertions.assertEquals(partitionsName, partitionNames);
    }

    @Test
    public void listPartitionsWithFilterSuccessful() {
        final CommandResult listResult = CommandResult.builder()
                .stdOut(validStdout)
                .stdErr(Collections.emptyList())
                .build();
        doReturn(listResult).when(mockCmdExecutor).execute(Mockito.any());

        final List<Queue> filtredPartition =
                List.of(Queue.builder()
                        .name(NEW_PARTITION_NAME)
                        .hostList(nodeListOneNode)
                        .slots(new SlotsDescription(1,
                                slotDescriptionOneNode))
                        .allowedUserGroups(groupsList)
                        .build());
        final QueueFilter filter = QueueFilter.builder().queues(List.of("normal")).build();
        final List<Queue> partitionsFiltrationResult = slurmQueueProvider.listQueues(filter);
        Assertions.assertEquals(filtredPartition, partitionsFiltrationResult);
    }

    @Test
    public void deletePartitionNotFound() {
        final CommandResult deletionFailedResult = CommandResult.builder()
                .stdOut(Collections.emptyList())
                .stdErr(List.of("delete_partition PartitionName=deletedPartition: Invalid partition name "
                        + "specified"))
                .build();
        doReturn(deletionFailedResult).when(mockCmdExecutor).execute(Mockito.any());

        final QueueVO incorrectDeletionData = QueueVO.builder()
                .name(DELETED_PARTITION_NAME)
                .build();
        final GridEngineException gridExceptionUpdate = Assertions.assertThrows(GridEngineException.class,
                () -> slurmQueueProvider.updateQueue(incorrectDeletionData));
        Assertions.assertNotNull(gridExceptionUpdate.getMessage());
    }

    @Test
    public void deletePartitionSuccessful() {
        final CommandResult deletePartitionResult = CommandResult.builder()
                .stdErr(Collections.emptyList())
                .build();

        doReturn(deletePartitionResult).when(mockCmdExecutor).execute(Mockito.any());

        final Queue deletedPartition = Queue.builder()
                .name(DELETED_PARTITION_NAME)
                .build();
        final Queue deletedPartitionResult = slurmQueueProvider.deleteQueues(DELETED_PARTITION_NAME);
        Assertions.assertEquals(deletedPartition, deletedPartitionResult);
    }

}
