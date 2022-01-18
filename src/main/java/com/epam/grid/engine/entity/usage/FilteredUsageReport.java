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

package com.epam.grid.engine.entity.usage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * A report extends {@link UsageReport} class with new fields containing additional usage info.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FilteredUsageReport extends UsageReport {

    /**
     * The set of job owners.
     */
    private Set<String> owners;

    /**
     * The set of queues in which jobs were performed.
     */
    private Set<String> queues;

    /**
     * The set of hosts on which jobs were performed.
     */
    private Set<String> hosts;

    /**
     * The cluster on which jobs were performed.
     */
    private String cluster;

    /**
     * The set of parallel environments in which jobs were performed.
     */
    private Set<String> parallelEnvs;
}
