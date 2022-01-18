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

package com.epam.grid.engine.controller.host;

import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.service.HostOperationProviderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This controller is responsible for host management operations.
 * Requests received are passed to underlying layer {@link HostOperationProviderService} for processing.
 */
@RestController
@RequestMapping("/hosts")
@RequiredArgsConstructor
public class HostController {

    private static final String NOT_FOUND = "Requested hosts not found";
    private static final String INTERNAL_ERROR = "Internal error";
    private static final String SUCCESS = "Hosts received successfully";
    private final HostOperationProviderService hostOperationProviderService;

    /**
     * This endpoint is responsible for hosts listing
     * Optional filtering details are passed in the request body.
     * In case of the empty filter - all the hosts will be returned.
     *
     * @param hostFilter names of hosts needed
     * @return {@link Listing} of {@link Host}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "List host nodes",
            notes = "Returns list that contains information about specific hosts regarding to filter",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESS),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public Listing<Host> listHosts(@RequestBody(required = false) final HostFilter hostFilter) {
        return hostOperationProviderService.filter(hostFilter);
    }
}
