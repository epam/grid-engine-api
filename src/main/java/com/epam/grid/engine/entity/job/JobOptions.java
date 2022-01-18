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

import java.util.List;
import java.util.Map;

/**
 * This class contains job submission options.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOptions {

    /**
     * Either the path to the script file or an executable command to be invoked.
     */
    private String command;
    /**
     * Custom job name.
     */
    private String name;
    /**
     * Flag of the executable file.
     */
    private boolean canBeBinary;
    /**
     * Indication of the use of all environment variables.
     */
    private boolean useAllEnvVars;
    /**
     * The path to the folder where you want to save the results of the job.
     */
    private String workingDir;
    /**
     * Job priority.
     */
    private int priority;
    /**
     * A list of queues in which the job should be processed.
     */
    private List<String> queues;
    /**
     * Collection of used environment variables.
     */
    private Map<String, String> envVariables;
    /**
     * Settings of the parallel processing environment.
     */
    private ParallelEnvOptions parallelEnvOptions;
    /**
     * List of arguments for the command being processed.
     */
    private List<String> arguments;
}
