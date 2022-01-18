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

package com.epam.grid.engine.entity.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A common job description.
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Job {

    /**
     * ID of job.
     */
    private int id;
    /**
     * Priority of job.
     */
    private double priority;
    /**
     * Name of job.
     */
    private String name;
    /**
     * Owner of job.
     */
    private String owner;
    /**
     * State of job.
     */
    private JobState state;
    /**
     * Submission time of job.
     */
    private LocalDateTime submissionTime;
    /**
     * Name of the queue in which the job is started.
     */
    private String queueName;
    /**
     * The number of slots that the job takes up.
     */
    private int slots;
}
