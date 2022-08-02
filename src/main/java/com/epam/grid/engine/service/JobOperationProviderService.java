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
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.job.JobProvider;

import com.epam.grid.engine.provider.log.JobLogProvider;
import com.epam.grid.engine.provider.utils.DirectoryPathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Optional;

/**
 * This class defines operations for processing information from the user
 * and returns the results to the controller {@link com.epam.grid.engine.controller.job.JobOperationController}.
 */
@Slf4j
@Service
public class JobOperationProviderService {

    private final String gridSharedFolder;
    private final JobProvider jobProvider;
    private final JobLogProvider jobLogProvider;

    /**
     * Constructor, sets created jobProvider bean to the class field and the path to job log.
     *
     * @param jobProvider      created JobProvider
     * @param gridSharedFolder the path to the primary directory from properties, where log and working directories
     *                         should be stored
     * @see JobProvider
     */

    public JobOperationProviderService(final JobProvider jobProvider,
                                       final JobLogProvider jobLogProvider,
                                       @Value("${grid.engine.shared.folder}") final String gridSharedFolder) {
        this.jobProvider = jobProvider;
        this.jobLogProvider = jobLogProvider;
        this.gridSharedFolder = gridSharedFolder;
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
     * Deletes jobs and returns information about these jobs.
     *
     * @param deleteJobFilter An object with the task deletion parameters.
     * @return Information about deleted job.
     */
    public Listing<DeletedJobInfo> deleteJob(final DeleteJobFilter deleteJobFilter) {
        if (!StringUtils.hasText(deleteJobFilter.getUser()) && CollectionUtils.isEmpty(deleteJobFilter.getIds())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, String.format("Incorrect filling in %s. "
                    + "Either at least one `id` or `user` must be specified to delete jobs!", deleteJobFilter));
        }
        if (StringUtils.hasText(deleteJobFilter.getUser()) && CollectionUtils.isNotEmpty(deleteJobFilter.getIds())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, String.format("Incorrect filling in %s. "
                    + "Only 'ids' or 'name' can be specified for jobs removal!", deleteJobFilter));
        }
        ListUtils.emptyIfNull(deleteJobFilter.getIds()).stream()
                .filter(id -> id == null || id <= 0)
                .findFirst()
                .ifPresent(id -> {
                    throw new GridEngineException(HttpStatus.BAD_REQUEST, String.format("At least one `id` is "
                                    + "incorrect specified in %s for job removal!", deleteJobFilter));
                });
        return jobProvider.deleteJob(deleteJobFilter);
    }

    /**
     * Returns a job started with the specified options.
     *
     * @param options Job launch parameters.
     * @return Running job.
     */
    public Job runJob(final JobOptions options) {
        if (!StringUtils.hasText(options.getCommand())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Command should be specified!");
        }
        Optional.ofNullable(options.getWorkingDir()).ifPresent(workingDir -> {
            final String workingDirAbsolutePath =
                    DirectoryPathUtils.resolvePathToAbsolute(gridSharedFolder, workingDir);
            if (!workingDir.equals(workingDirAbsolutePath)) {
                options.setWorkingDir(workingDirAbsolutePath);
                log.info("Working directory was changed from " + workingDir + " to " + workingDirAbsolutePath);
            }
        });
        return jobProvider.runJob(options, jobLogProvider.getJobLogDir());
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
    public JobLogInfo getJobLogInfo(final long jobId, final JobLogInfo.Type logType,
                                    final int lines, final boolean fromHead) {
        return jobLogProvider.getJobLogInfo(jobId, logType, lines, fromHead);
    }

    /**
     * This method passes the request into {@link JobProvider}
     * and returns the job log file.
     *
     * @param jobId   The job identifier.
     * @param logType The type of required log file.
     * @return The job log file like a stream.
     */
    public InputStream getJobLogFile(final long jobId, final JobLogInfo.Type logType) {
        return jobLogProvider.getJobLogFile(jobId, logType);
    }
}
