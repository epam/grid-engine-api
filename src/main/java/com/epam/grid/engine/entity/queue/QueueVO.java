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

package com.epam.grid.engine.entity.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Representation of the queue to be registered.
 */
@Data
@Builder
@AllArgsConstructor
public class QueueVO {

    /**
     * The name of the cluster queue.
     */
    private String name;

    /**
     * The list of host identifiers.
     */
    private List<String> hostList;

    /**
     *The list of administrator-defined parallel environment names to be associated with the queue.
     */
    private List<String> parallelEnvironmentNames;

    /**
     * The list of usernames who are authorized to disable and suspend this queue.
     */
    private List<String> ownerList;

    /**
     * The list of user groups with access permissions to queues.
     */
    private List<String> allowedUserGroups;
}
