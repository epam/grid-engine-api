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

package com.epam.grid.engine.controller.hostgroup;

import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.entity.HostGroupFilter;
import com.epam.grid.engine.service.HostGroupOperationProviderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The controller that receives web requests from users and transmits it
 * for processing to the {@link HostGroupOperationProviderService}.
 */
@RestController
@RequestMapping("/clusters")
@RequiredArgsConstructor
public class HostGroupOperationController {

    private static final String NOT_FOUND = "Required host group doesn't exist";
    private static final String INTERNAL_ERROR = "Internal error";
    private static final String SUCCESS = "Host groups list successfully received";
    private static final String GET_GROUP_SUCCESS = "Host group successfully received";
    private static final String WRONG_GROUP_NAME = "Wrong host group name";
    private final HostGroupOperationProviderService hostGroupOperationProviderService;

    /**
     * Returns a List containing specified {@link HostGroup}s with respect to provided {@link HostGroupFilter}.
     * @param hostGroupFilter a provided filter
     * @return a List containing specified HostGroups with respect to provided filter
     */
    @PostMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Filters host groups",
            notes = "Returns the list which contains information about host groups with respect to provided filters",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SUCCESS),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public List<HostGroup> listHostGroups(@RequestBody(required = false) final HostGroupFilter hostGroupFilter) {
        return hostGroupOperationProviderService.listHostGroups(hostGroupFilter);
    }

    /**
     * Returns {@link HostGroup} by group name.
     *
     * @param groupName The group name passed by the user.
     * @return Information about the group.
     */
    @GetMapping("/{groupname}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Info about selected host group",
            notes = "Returns information about the selected host group",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = GET_GROUP_SUCCESS),
            @ApiResponse(code = 400, message = WRONG_GROUP_NAME),
            @ApiResponse(code = 404, message = NOT_FOUND),
            @ApiResponse(code = 500, message = INTERNAL_ERROR)
    })
    public HostGroup getHostGroup(@PathVariable("groupname") final String groupName) {
        return hostGroupOperationProviderService.getHostGroup(groupName);
    }
}
