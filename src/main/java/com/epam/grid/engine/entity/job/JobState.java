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
import lombok.NoArgsConstructor;

/**
 * This class represents job states.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobState {
    /**
     * Category of job.
     */
    private Category category;
    /**
     * State of job.
     */
    private String state;
    /**
     * State code of job.
     */
    private String stateCode;

    /**
     * Possible job categories.
     * To get more information about grid engine job category values see <a href="https://manpages.debian.org/testing/gridengine-common/sge_status.5.en.html">
     * Grid Engine job category values</a>.
     */
    public enum Category {
        PENDING,
        RUNNING,
        SUSPENDED,
        ERROR,
        DELETED,
        FINISHED,
        UNKNOWN
    }
}
