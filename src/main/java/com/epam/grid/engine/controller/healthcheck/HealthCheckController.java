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

package com.epam.grid.engine.controller.healthcheck;

import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.service.HealthCheckProviderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for status checking operations.
 * Requests received are passed to underlying layer {@link HealthCheckProviderService} for processing
 */
@RestController
@RequiredArgsConstructor
public class HealthCheckController {

    private static final String INTERNAL_ERROR = "Internal error";
    private static final String NOT_FOUND = "Grid engine is not reachable";
    private static final String SUCCESS = "Status successfully received";
    private final HealthCheckProviderService healthCheckProviderService;

    /**
     * This endpoint is responsible for the health status check of an active grid engine.
     *
     * @return {@link HealthCheckInfo}
     */
    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Check status",
            notes = "Tries to get status of grid engine")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESS),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public HealthCheckInfo checkHealth() {
        return healthCheckProviderService.checkHealth();
    }
}
