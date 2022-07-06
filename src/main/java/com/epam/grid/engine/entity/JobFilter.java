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

package com.epam.grid.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The class represents a description of the filter for getting jobs.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobFilter {
    /**
     * List of job IDs.
     */
    private List<Long> ids;
    /**
     * State of job.
     */
    private String state;
    /**
     * List of titles of jobs.
     */
    private List<String> names;
    /**
     * A list of job owners.
     */
    private List<String> owners;
}
