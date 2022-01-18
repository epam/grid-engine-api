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

package com.epam.grid.engine.controller.parallelenv;

import com.epam.grid.engine.entity.ParallelEnvFilter;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.PeRegistrationVO;
import com.epam.grid.engine.service.ParallelEnvOperationProviderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * This controller is responsible for parallel environment management operations.
 * Requests received are passed to underlying layer {@link ParallelEnvOperationProviderService} for processing.
 */
@RestController
@RequestMapping("/parallelenv")
@RequiredArgsConstructor
public class ParallelEnvController {

    private static final String INTERNAL_ERROR = "Internal error";
    private static final String PE_SUCCESS = "PE successfully retrieved";
    private static final String INVALID_REQUEST = "Missing or invalid request body";
    private static final String NOT_FOUND = "Requested parallel environments not found";
    private static final String SUCCESSFULLY_RECEIVED = "Parallel environments received successfully";
    private static final String SUCCESSFULLY_DELETED = "PE was successfully deleted";
    private static final String DELETION_DENIED = "Deletion denied";
    private static final String SUCCESSFULLY_REGISTERED = "Parallel environment registered successfully";
    private static final String PE_REGISTRATION_PARAMETERS_NOT_FOUND = "PE registration failed due to registration"
            + "parameter were not found";
    private static final String REGISTRATION_DENIED = "Registration denied";

    private final ParallelEnvOperationProviderService parallelEnvOperationProviderService;

    /**
     * This endpoint is responsible for PE listing
     * Optional filtering details are passed in the request body.
     * In case of the empty filter - all the PE will be returned.
     *
     * @param parallelEnvFilter names of PE needed
     * @return Listing of {@link ParallelEnv}
     */
    @PostMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "List parallel environments",
            notes = "Returns list that contains all PE or information about specific PE regarding to filter",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_RECEIVED),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public List<ParallelEnv> listParallelEnv(@RequestBody(required = false)
                                                 final ParallelEnvFilter parallelEnvFilter) {
        return parallelEnvOperationProviderService.filterParallelEnvs(parallelEnvFilter);
    }

    /**
     * This endpoint retrieves information about specific PE.
     *
     * @param peName name of PE to retrieve
     * @return object of {@link ParallelEnv}
     */
    @GetMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Provides specific parallel environment",
            notes = "Returns object that contains information about specific PE",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = PE_SUCCESS),
            @ApiResponse(code = 400, message = INVALID_REQUEST),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public ParallelEnv getParallelEnv(@PathVariable("name") final String peName) {
        return parallelEnvOperationProviderService.getParallelEnv(peName);
    }

    /**
     * Delete specified parallel environment.
     * @param parallelEnvName the name of the deleting parallel environment
     * @return {@link ParallelEnv} which was deleted
     */
    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete parallel environment",
            notes = "Deletes parallel environment"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_DELETED),
            @ApiResponse(code = 400, message = INVALID_REQUEST),
            @ApiResponse(code = 403, message = DELETION_DENIED),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public ParallelEnv deleteParallelEnv(@PathVariable("name") final String parallelEnvName) {
        return parallelEnvOperationProviderService.deleteParallelEnv(parallelEnvName);
    }

    /**
     * Registers a {@link ParallelEnv} with specified in the
     * {@link PeRegistrationVO} properties.
     *
     * @param registrationRequest the properties of the parallel environment to be registered
     * @return the registered {@link ParallelEnv}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Registers parallel environment",
            notes = "Registers the parallel environment with specified properties",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESSFULLY_REGISTERED),
            @ApiResponse(code = 400, message = INVALID_REQUEST),
            @ApiResponse(code = 403, message = REGISTRATION_DENIED),
            @ApiResponse(code = 404, message = PE_REGISTRATION_PARAMETERS_NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public ParallelEnv registerParallelEnv(@RequestBody final PeRegistrationVO registrationRequest) {
        return parallelEnvOperationProviderService.registerParallelEnv(registrationRequest);
    }
}
