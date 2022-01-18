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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * This class describes possible user conditions for forming a command to get report usage.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsageReportFilter {

    /**
     * The owner of the job.
     */
    private String owner;
    /**
     * The queue in which the job was performed.
     */
    private String queue;
    /**
     * The parallel environment in which the job was performed.
     */
    private String parallelEnv;
    /**
     * The name or ID of the job for which the selection is performed.
     */
    private String jobIdOrName;
    /**
     * The earliest start time for jobs to be summarized.
     */
    private LocalDateTime startTime;
    /**
     * The latest start time for jobs to be summarized.
     */
    private LocalDateTime endTime;
    /**
     * The number of days to summarize and print accounting information on.
     */
    private Integer days;
}
