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

package com.epam.grid.engine.service;

import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.JobLogInfo;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.provider.job.JobProvider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class defines operations for processing information from the user
 * and returns the results to the controller {@link com.epam.grid.engine.controller.job.JobOperationController}.
 */
@Slf4j
@Service
public class JobOperationProviderService {

    private final String logDir;
    private final JobProvider jobProvider;

    /**
     * Constructor, sets created jobProvider bean to the class field and the path to job log.
     *
     * @param jobProvider created JobProvider
     * @param logDir      the path to the directory where all log files will be stored
     *                    occurred when processing the job
     * @see JobProvider
     */

    public JobOperationProviderService(final JobProvider jobProvider, @Value("${job.log.dir}") final String logDir) {
        this.jobProvider = jobProvider;
        this.logDir = logDir;
    }

    /**
     * Returns a list of jobs after using the filter selected by the user.
     *
     * @param jobFilter An object with the job selection parameters.
     * @return list of jobs.
     */
    public Listing<Job> filter(final JobFilter jobFilter) {
        return jobProvider.filterJobs(jobFilter);
    }

    /**
     * Deletes the job and returns information about this job.
     *
     * @param deleteJobFilter An object with the task deletion parameters.
     * @return Information about deleted job.
     */
    public DeletedJobInfo deleteJob(final DeleteJobFilter deleteJobFilter) {
        return jobProvider.deleteJob(deleteJobFilter);
    }

    /**
     * Returns a job started with the specified options.
     *
     * @param options Job launch parameters.
     * @return Running job.
     */
    public Job runJob(final JobOptions options) {
        return jobProvider.runJob(options);
    }

    /**
     * This method passes the request into {@link JobProvider} and returns information
     * about the job log file and a list of required log lines.
     *
     * @param jobId    The job identifier.
     * @param logType  The log file type to obtain information from.
     * @param lines    The number of lines.
     * @param fromHead if it's true, lines are taken from the head of the log file, otherwise from the tail.
     * @return {@link JobLogInfo}
     */
    public JobLogInfo getJobLogInfo(final int jobId, final JobLogInfo.Type logType,
                                    final int lines, final boolean fromHead) {
        return jobProvider.getJobLogInfo(jobId, logType, lines, fromHead);
    }

    /**
     * This method passes the request into {@link JobProvider}
     * and returns the job log file.
     *
     * @param jobId   The job identifier.
     * @param logType The type of required log file.
     * @return The job log file like a stream.
     */
    public InputStream getJobLogFile(final int jobId, final JobLogInfo.Type logType) {
        return jobProvider.getJobLogFile(jobId, logType);
    }

    /**
     * This method checks for job log directory existence and write accessibility,
     * in case of failure, the application hasn't to start.
     */
    @PostConstruct
    public void checkLogDirAvailability() {
        final Path logPath = Path.of(logDir);
        if (!Files.isDirectory(logPath) || !Files.isWritable(logPath)) {
            final String message = "The directory to log files was not found or write permissions are missing: "
                    + logDir;
            log.error(message);
            throw new IllegalStateException(message);
        }
    }

}
