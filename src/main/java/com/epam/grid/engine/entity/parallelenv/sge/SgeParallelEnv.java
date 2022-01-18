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

package com.epam.grid.engine.entity.parallelenv.sge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * This class provides an object that contains full description of the SGE PE state.
 * To get more information about load parameters see <a href="http://gridscheduler.sourceforge.net/htmlman/htmlman5/sge_pe.html">
 * Grid Engine default load parameters</a>.
 *
 * @see com.epam.grid.engine.entity.parallelenv.ParallelEnv
 */
@Data
@Builder
@AllArgsConstructor
public class SgeParallelEnv {

    /**
     * The name by which the parallel environment will be known to Grid Engine.
     */
    private String name;

    /**
     * The maximum number of job slots that the parallel environment is allowed to occupy at once.
     */
    private int slots;

    /**
     * The list of user groups with access permissions to parallel environments.
     */
    private List<String> allowedUserGroups;

    /**
     * The list of user groups who are not allowed to use parallel environments.
     */
    private List<String> forbiddenUserGroups;

    /**
     * The path to the startup script for the parallel environment.
     */
    private List<String> startProcArgs;

    /**
     * The path to the shutdown script for the parallel environment followed by any needed arguments.
     */
    private List<String> stopProcArgs;

    /**
     * This setting controls how job slots are assigned to hosts.
     */
    private String allocationRule;

    /**
     * This setting tells Grid Engine whether the parallel environment integration is "tight" or "loose".
     */
    private boolean controlSlaves;

    /**
     *  This setting tells Grid Engine whether the first task of the parallel job
     *  is actually a job task or whether it's just there to kick off the rest of the jobs.
     */
    private boolean jobIsFirstTask;

    /**
     * This setting affect how resource requests affect job priority for parallel jobs.
     */
    private String urgencySlots;

    /**
     * A value that indicates how many accounting record is written to the accounting file
     * of the job.
     */
    private boolean accountingSummary;

    /**
     * This is sge qsort command args.
     */
    private List<String> qsortArgs;
}

