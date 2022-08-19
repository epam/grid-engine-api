package com.epam.grid.engine.mapper.queue.slurm;

import com.epam.grid.engine.entity.host.slurm.SlurmHost;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.SlotsDescription;
import com.epam.grid.engine.entity.queue.slurm.SlurmQueue;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlurmQueueMapperTest {
    private static final SlurmQueueMapper queueMapper = Mappers.getMapper(SlurmQueueMapper.class);
    private static final String PARTITION_NAME = "partitionName";
    private static final List<String> nodeNamesList = List.of("worker1", "worker2");
    private static final List<SlurmHost> nodeList = List.of(
            SlurmHost.builder().nodeName("worker1").cpuTotal(2).build(),
            SlurmHost.builder().nodeName("worker2").cpuTotal(2).build()
    );
    private static final List<String> userGroups = List.of("ALL");
    private static final Map<String, Integer> slotDescriptionNodes = Map.of("worker1", 2, "worker2", 2);
    private static final SlotsDescription slotsDescription = new SlotsDescription(4, slotDescriptionNodes);

    @Test
    public void shouldConvertSlurmQueueToQueue() {
        final SlurmQueue slurmQueue = SlurmQueue.builder()
                .partition(PARTITION_NAME)
                .nodelist(nodeList)
                .groups(userGroups)
                .build();
        final Queue expectedQueue = Queue.builder()
                .name(PARTITION_NAME)
                .hostList(nodeNamesList)
                .slots(slotsDescription)
                .allowedUserGroups(userGroups)
                .build();
        final Queue mappedQueue = queueMapper.slurmQueueToQueue(slurmQueue);
        assertEquals(expectedQueue, mappedQueue);
    }
}
