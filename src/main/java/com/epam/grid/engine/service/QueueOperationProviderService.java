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

import com.epam.grid.engine.controller.queue.QueueOperationController;
import com.epam.grid.engine.entity.QueueFilter;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.QueueVO;
import com.epam.grid.engine.provider.queue.QueueProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The class which processes the information received from {@link QueueOperationController} and calls the corresponding
 * QueueProvider type.
 */
@Service
@RequiredArgsConstructor
public class QueueOperationProviderService {

    private final QueueProvider queueProvider;

    /**
     * Returns a List of existing Queues with their names.
     *
     * @return a List of existing Queues with their names
     */
    public List<Queue> listQueues() {
        return queueProvider.listQueues();
    }

    /**
     * Returns a List of Queues according to {@link QueueFilter} parameter.
     *
     * @param queueFilter a provided filter
     * @return a List of Queues according to queueFilter parameter
     */
    public List<Queue> listQueues(final QueueFilter queueFilter) {
        return queueProvider.listQueues(queueFilter);
    }

    /**
     * Deletes queue.
     *
     * @param queueName Name of queue to delete.
     * @return Queue object which was deleted.
     */
    public Queue deleteQueue(final String queueName) {
        return queueProvider.deleteQueues(queueName);
    }

    /**
     * Registers a queue with specified properties in preassigned grid engine system.
     *
     * @param registrationRequest the properties of the queue to be registered
     * @return the registered {@link Queue}
     */
    public Queue registerQueue(final QueueVO registrationRequest) {
        return queueProvider.registerQueue(registrationRequest);
    }


    /**
     * Updates a queue with specified properties in preassigned grid engine system.
     *
     * @param updateRequest the properties of the queue to be updated
     * @return the updated {@link Queue}
     */
    public Queue updateQueue(final QueueVO updateRequest) {
        return queueProvider.updateQueue(updateRequest);
    }

}
