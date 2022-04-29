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

import com.epam.grid.engine.controller.hostgroup.HostGroupOperationController;
import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.entity.HostGroupFilter;
import com.epam.grid.engine.provider.hostgroup.HostGroupProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The class which processes the information received from {@link HostGroupOperationController}
 * and calls the corresponding HostGroupProvider type.
 */
@Service
@RequiredArgsConstructor
public class HostGroupOperationProviderService {

    private final HostGroupProvider hostGroupProvider;

    /**
     * Returns a List of {@link HostGroup}s according to {@link HostGroupFilter} parameter.
     *
     * @param hostGroupFilter a provided filter
     * @return a List of HostGroups according to hostGroupFilter parameter
     */
    public List<HostGroup> listHostGroups(final HostGroupFilter hostGroupFilter) {
        return hostGroupProvider.listHostGroups(hostGroupFilter);
    }

    /**
     * Returns {@link HostGroup} by specified group name.
     *
     * @param groupName The specified group name.
     * @return Information about the group.
     */
    public HostGroup getHostGroup(final String groupName) {
        return hostGroupProvider.getHostGroup(groupName);
    }

}
