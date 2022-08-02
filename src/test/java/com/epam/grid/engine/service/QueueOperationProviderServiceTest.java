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

package com.epam.grid.engine.service;

import com.epam.grid.engine.TestPropertiesWithSgeEngine;
import com.epam.grid.engine.entity.QueueFilter;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.QueueVO;
import com.epam.grid.engine.provider.queue.QueueProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

@SpringBootTest
@TestPropertiesWithSgeEngine
public class QueueOperationProviderServiceTest {

    private static final String COMMAND_QCONF = "qconf";
    private static final String OPTION_SQ = "-sq";
    private static final String TEST_QUEUE_NAME = "all.q";
    private static final String TEST_MAIN_NAME = "main";
    private static final String TEST_OWNER_LIST = "sgeuser";

    @Autowired
    QueueOperationProviderService queueOperationProviderService;

    @MockBean
    QueueProvider queueProvider;

    @Test
    public void shouldReturnCorrectQueue() {
        final Queue expectedQueue = Queue.builder()
                .name("all.q")
                .build();
        final List<Queue> queues = Collections.singletonList(expectedQueue);
        final QueueFilter queueFilter = new QueueFilter();
        queueFilter.setQueues(List.of(COMMAND_QCONF, OPTION_SQ, TEST_QUEUE_NAME));

        doReturn(queues).when(queueProvider).listQueues(queueFilter);
        Assertions.assertEquals(expectedQueue, queueOperationProviderService.listQueues(queueFilter).get(0));
        Mockito.verify(queueProvider, times(1)).listQueues(queueFilter);
    }

    @Test
    public void shouldReturnCorrectQueueWithEmptyResponse() {
        final Queue expectedQueue = Queue.builder()
                .name("all.q")
                .build();
        final List<Queue> queues = Collections.singletonList(expectedQueue);

        doReturn(queues).when(queueProvider).listQueues();
        Assertions.assertEquals(expectedQueue, queueOperationProviderService.listQueues().get(0));
        Mockito.verify(queueProvider, times(1)).listQueues();
    }

    @Test
    public void shouldReturnCorrectInfoDuringDeletion() {
        final Queue deletedQueue = Queue.builder()
                .name(TEST_MAIN_NAME)
                .build();

        doReturn(deletedQueue).when(queueProvider).deleteQueues(TEST_MAIN_NAME);
        Assertions.assertEquals(deletedQueue, queueOperationProviderService.deleteQueue(TEST_MAIN_NAME));
        Mockito.verify(queueProvider, times(1)).deleteQueues(TEST_MAIN_NAME);
    }

    @Test
    public void shouldReturnCorrectQueueDuringModification() {
        final QueueVO updateRequest = QueueVO.builder()
                .name(TEST_MAIN_NAME)
                .ownerList(Collections.singletonList(TEST_OWNER_LIST))
                .build();
        final Queue updatedQueue = Queue.builder()
                .name(TEST_MAIN_NAME)
                .ownerList(Collections.singletonList(TEST_OWNER_LIST))
                .build();

        doReturn(updatedQueue).when(queueProvider).updateQueue(updateRequest);
        Assertions.assertEquals(updatedQueue, queueOperationProviderService.updateQueue(updateRequest));
        Mockito.verify(queueProvider, times(1)).updateQueue(updateRequest);
    }
}
