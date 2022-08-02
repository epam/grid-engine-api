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

package com.epam.grid.engine.provider.queue;

import com.epam.grid.engine.entity.QueueFilter;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.QueueVO;
import com.epam.grid.engine.provider.CommandTypeAware;

import java.util.List;

/**
 * The interface which provides methods for different
 * types of grid engines to operate with {@link Queue}.
 */
public interface QueueProvider extends CommandTypeAware {

    /**
     * Returns a List containing Queues.
     * @return a List containing Queues
     */
    List<Queue> listQueues();

    /**
     * Returns a List containing specified Queues with respect to provided {@link QueueFilter}.
     * @param queueFilter a provided filter
     * @return a List containing specified Queues with respect to provided filter
     */
    List<Queue> listQueues(QueueFilter queueFilter);

    /**
     * Deletes the specified queue.
     *
     * @param queueName Search parameters for the queue being deleted.
     * @return Queue object which was deleted.
     * @see Queue
     */
    Queue deleteQueues(String queueName);

    /**
     * Registers a queue with specified properties in grid engine system.
     *
     * @param registrationRequest the properties of the queue to be registered
     * @return the registered {@link Queue}
     */
    Queue registerQueue(QueueVO registrationRequest);

    /**
     * Updates a queue with specified properties in grid engine system.
     *
     * @param updateRequest the properties of the queue to be updated
     * @return the updated {@link Queue}
     */
    Queue updateQueue(QueueVO updateRequest);
}
