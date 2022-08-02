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

package com.epam.grid.engine.controller.job;

import com.epam.grid.engine.controller.AbstractRestController;
import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.entity.job.JobLogInfo;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.service.JobOperationProviderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * This controller is responsible for job management operations.
 */
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobOperationController extends AbstractRestController {

    private static final String JOB_ID = "jobId";
    private static final String JOB_ID_LOGS_URL_SECTION = "/{" + JOB_ID + ":[\\d]+}/logs";
    private static final String LOG_TYPE_ID = "type";
    private static final String INTERNAL_ERROR = "Internal error";
    private static final String MISSING_OR_INVALID_REQUEST_BODY = "Missing or invalid request body";
    private static final String NOT_FOUND = "Specified job(-s) not found";
    private static final String SUCCESSFULLY_RECEIVED = "Job received successfully";
    private static final String SUCCESSFULLY_DELETED = "Job deleted successfully";
    private static final String SUCCESSFULLY_SUBMITTED = "Job submitted successfully";
    private static final String SUCCESSFULLY_RECEIVED_LOG = "Log received successfully";

    /**
     * This field contains the service class to which the data received from the user is transmitted.
     */
    private final JobOperationProviderService providerService;

    /**
     * Returns a list of jobs after applying the filter specified by the user.
     *
     * @param jobFilter An object with the job selection parameters.
     * @return list of jobs.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Filter jobs",
            notes = "Returns list that contains information about specific jobs regarding to filter",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_RECEIVED),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public Listing<Job> filterJobs(@RequestBody(required = false) final JobFilter jobFilter) {
        return providerService.filter(jobFilter);
    }

    /**
     * Deletes job and returns information about this job.
     *
     * @param deleteJobFilter An object with the task deletion parameters.
     * @return Information about deleted job.
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete job",
            notes = "Tries to delete one or more jobs by username or ids. If successful,"
                    + " returns the message and information about deleted jobs"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_DELETED),
            @ApiResponse(code = 400, message = MISSING_OR_INVALID_REQUEST_BODY),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public Listing<DeletedJobInfo> deleteJob(@RequestBody final DeleteJobFilter deleteJobFilter) {
        return providerService.deleteJob(deleteJobFilter);
    }

    /**
     * Returns a job submitted with the specific options.
     *
     * @param options Job launch parameters.
     * @return Job submission result.
     */
    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Submits a job into a cluster",
            notes = "Tries to add a job to the queue, if successful, returns the index of the job.",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_SUBMITTED),
            @ApiResponse(code = 400, message = MISSING_OR_INVALID_REQUEST_BODY),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public Job runJob(@RequestBody final JobOptions options) {
        return providerService.runJob(options);
    }

    /**
     * Returns a requested job log lines and information about job log file.
     *
     * @param jobId    The job identifier.
     * @param logType  The log file type to obtain information from.
     * @param lines    The number of lines.
     * @param fromHead if it's true, lines are taken from the head of the log file, otherwise from the tail.
     * @return an object contained a list of requested job log lines and information about job log file.
     * @see JobLogInfo
     */
    @GetMapping(JOB_ID_LOGS_URL_SECTION)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Obtain a list of job log lines and information about log file",
            notes = "Tries to get job log information",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_RECEIVED_LOG),
            @ApiResponse(code = 400, message = MISSING_OR_INVALID_REQUEST_BODY),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR),
    })
    public JobLogInfo getJobLogInfo(@PathVariable(JOB_ID) final long jobId,
                                    @RequestParam(value = LOG_TYPE_ID,
                                            required = false, defaultValue = "ERR") final JobLogInfo.Type logType,
                                    @RequestParam(value = "lines",
                                            required = false, defaultValue = "0") final int lines,
                                    @RequestParam(value = "fromHead", required = false) final boolean fromHead) {
        return providerService.getJobLogInfo(jobId, logType, lines, fromHead);
    }

    /**
     * This endpoint is responsible for the obtaining the job log file.
     *
     * @param jobId   The job identifier.
     * @param logType The type of required log file.
     */
    @GetMapping(JOB_ID_LOGS_URL_SECTION + "/file")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get the job log file",
            notes = "Tries to get of the job log file")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_RECEIVED_LOG),
            @ApiResponse(code = 400, message = MISSING_OR_INVALID_REQUEST_BODY),
            @ApiResponse(code = 404, message = NOT_FOUND)
    })
    public void getJobLogFile(final HttpServletResponse response,
                              @PathVariable(JOB_ID) final long jobId,
                              @RequestParam(LOG_TYPE_ID) final JobLogInfo.Type logType) {
        try (InputStream inputStream = providerService.getJobLogFile(jobId, logType)) {
            writeStreamToResponse(response, inputStream, String.format("%d.%s", jobId, logType.getSuffix()));
        } catch (final IOException e) {
            throw new GridEngineException(HttpStatus.NOT_FOUND,
                    "Something went wrong while reading the job log file", e);
        }
    }
}
