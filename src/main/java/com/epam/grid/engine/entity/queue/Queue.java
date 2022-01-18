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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * A common object that represents a Queue entity.
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Queue {

    /**
     * The name of the cluster queue.
     */
    private String name;

    /**
     * The list of host identifiers.
     */
    private List<String> hostList;

    /**
     * The position for this queue in the scheduling order
     * withing the suitable queues for a job to be dispatched.
     */
    private Integer numberInSchedulingOrder;

    /**
     * Load thresholds.
     */
    private Map<String, Double> loadThresholds;

    /**
     * Load thresholds with the same semantics as that of
     * the {@link Queue#loadThresholds} parameter except that
     * exceeding one of the denoted thresholds initiates suspension
     * of one of multiple jobs in the queue.
     */
    private Map<String, Double> suspendThresholds;

    /**
     * The number of jobs which are suspended/enabled per time
     * interval.
     */
    private Integer numOfSuspendedJobs;

    /**
     * The time interval in which further suspended jobs are
     * suspended.
     */
    private String interval;

    /**
     * The value at which jobs in this queue will be run.
     */
    private Integer jobPriority;

    /**
     * The type of queue.
     */
    private String qtype;

    /**
     * The list of administrator-defined parallel environment
     * names  to  be  associated  with  the  queue.
     */
    private List<String> parallelEnvironmentNames;

    /**
     * The object that provides a number and descriptions of
     * slots (concurrently executing jobs allowed in the queue).
     */
    private SlotsDescription slots;

    /**
     * The list of usernames who are authorized to disable and suspend this queue.
     */
    private List<String> ownerList;

    /**
     * The list of user groups with access permissions to queues.
     */
    private List<String> allowedUserGroups;

    /**
     * The absolute path to the base of the temporary directory filesystem.
     */
    private String tmpDir;
}
