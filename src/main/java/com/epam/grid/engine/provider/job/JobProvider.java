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
import com.epam.grid.engine.entity.job.JobLogInfo;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.provider.GridProviderAware;

import java.io.InputStream;

/**
 * This interface specifies methods for the job provider.
 */
public interface JobProvider extends GridProviderAware {

    /**
     * Gets a list of jobs for the specified filters.
     *
     * @param jobFilter The specified filter.
     * @return List of jobs.
     */
    Listing<Job> filterJobs(JobFilter jobFilter);

    /**
     * Deletes the job being executed according to the specified parameters.
     *
     * @param deleteJobFilter Search parameters for the job being deleted.
     * @return Information about the deleted job.
     */
    DeletedJobInfo deleteJob(DeleteJobFilter deleteJobFilter);

    /**
     * Launches the job with the specified parameters.
     *
     * @param options Parameters for launching the job.
     * @return Launched job.
     */
    Job runJob(JobOptions options);

    /**
     * This method provides information about the log file and obtains the specified number of lines from it.
     *
     * @param jobId    The job identifier.
     * @param logType  The log file type to obtain information from.
     * @param lines    The number of lines.
     * @param fromHead if it's true, lines are taken from the head of the log file, otherwise from the tail.
     * @return The object of {@link JobLogInfo}
     */
    JobLogInfo getJobLogInfo(final int jobId, final JobLogInfo.Type logType, final int lines, final boolean fromHead);

    /**
     * Gets a job log file.
     *
     * @param jobId   The job identifier.
     * @param logType The type of required log file.
     * @return The job log file like a stream.
     */
    InputStream getJobLogFile(final int jobId, final JobLogInfo.Type logType);

}
