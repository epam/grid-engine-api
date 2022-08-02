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

package com.epam.grid.engine.provider.job;

import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.provider.CommandTypeAware;

/**
 * This interface specifies methods for the job provider.
 */
public interface JobProvider extends CommandTypeAware {

    /**
     * Gets a list of jobs for the specified filters.
     *
     * @param jobFilter The specified filter.
     * @return List of jobs.
     */
    Listing<Job> filterJobs(JobFilter jobFilter);

    /**
     * Deletes jobs being executed according to the specified parameters.
     *
     * @param deleteJobFilter Search parameters for the job being deleted.
     * @return Information about deleted jobs.
     */
    Listing<DeletedJobInfo> deleteJob(DeleteJobFilter deleteJobFilter);

    /**
     * Launches the job with the specified parameters.
     *
     * @param options Parameters for launching the job.
     * @param logDir  the path to the directory where all log files will be stored
     *                occurred when processing the job
     * @return Launched job.
     */
    Job runJob(JobOptions options, String logDir);

}
