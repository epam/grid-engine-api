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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Representation of information about status of grid engine cluster.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobLogInfo {
    /**
     * The job index in Grid engine.
     */
    private int jobId;
    /**
     * The type index in Grid engine.
     */
    private Type type;
    /**
     * Lines received from a job log file.
     */
    private List<String> lines;
    /**
     * The total number of lines in the log file.
     */
    private int totalCount;
    /**
     * The log file size in bytes.
     */
    private long bytes;
    /**
     * Possible job log types.
     */

    @AllArgsConstructor
    @Getter
    public enum Type {
        ERR("err"),
        OUT("out");

        private final String suffix;
    }
}
