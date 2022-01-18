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

package com.epam.grid.engine.controller.usage;

import com.epam.grid.engine.entity.usage.UsageReport;
import com.epam.grid.engine.entity.usage.UsageReportFilter;
import com.epam.grid.engine.service.UsageOperationProviderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller handles usage-related requests addressed to preassigned grid engine system.
 */
@RestController
@RequestMapping("/usage")
@RequiredArgsConstructor
public class UsageOperationController {

    private static final String NOT_FOUND = "Requested usage report not found";
    private static final String INTERNAL_ERROR = "Internal error";
    private static final String SUCCESSFULLY_RECEIVED = "Usage report received successfully";
    private static final String MISSING_OR_INVALID_REQUEST_BODY = "Missing or invalid request body";
    private final UsageOperationProviderService usageOperationProviderService;

    /**
     * Returns a report containing usage summary information.
     *
     * @param filter List of keys for setting filters.
     * @return the usage report
     * @see UsageReport
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Provides usage report",
            notes = "Returns a report containing usage summary information",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_RECEIVED),
            @ApiResponse(code = 400, message = MISSING_OR_INVALID_REQUEST_BODY),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public UsageReport getUsageReport(@RequestBody final UsageReportFilter filter) {
        return usageOperationProviderService.getUsageReport(filter);
    }
}
