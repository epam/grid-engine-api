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

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * A report containing usage summary information.
 */
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsageReport {

    /**
     * The actual time, in seconds, that elapsed between the start
     * of the first submitted job and the current instant.
     */
    private int wallClock;

    /**
     * The time, in seconds, spent executing user code within submitted jobs.
     */
    private double userTime;

    /**
     * The time, in seconds, spent executing kernel code within submitted jobs.
     */
    private double systemTime;

    /**
     * The time, in seconds, of cpu usage.
     */
    private double cpuTime;

    /**
     * The integral memory usage in Gbyte-seconds.
     */
    private double memory;

    /**
     * The amount of data transferred in input/output operations in Gbytes.
     */
    private double ioData;

    /**
     * The input/output wait time in seconds.
     */
    private double ioWaiting;
}
