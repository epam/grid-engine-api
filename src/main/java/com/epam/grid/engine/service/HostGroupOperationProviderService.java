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
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.entity.HostGroupFilter;
import com.epam.grid.engine.provider.hostgroup.HostGroupProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The class which processes the information received from {@link HostGroupOperationController}
 * and calls the corresponding HostGroupProvider type.
 */
@Service
public class HostGroupOperationProviderService {

    private final EngineType engineType;

    private Map<EngineType, HostGroupProvider> providers;

    /**
     * Sets grid engine type in the context.
     * @param engineType type of grid engine
     * @see EngineType
     */
    public HostGroupOperationProviderService(@Value("${grid.engine.type}") final EngineType engineType) {
        this.engineType = engineType;
    }

    /**
     * Returns a List of {@link HostGroup}s according to {@link HostGroupFilter} parameter.
     * @param hostGroupFilter a provided filter
     * @return a List of HostGroups according to hostGroupFilter parameter
     */
    public List<HostGroup> listHostGroups(final HostGroupFilter hostGroupFilter) {
        return getQueueProvider().listHostGroups(hostGroupFilter);
    }

    /**
     * Returns {@link HostGroup} by specified group name.
     *
     * @param groupName The specified group name.
     * @return Information about the group.
     */
    public HostGroup getHostGroup(final String groupName) {
        return getQueueProvider().getHostGroup(groupName);
    }

    /**
     * Injects all created {@link HostGroupProvider} beans.
     * @param providers list of existing HostGroupProviders
     * @see HostGroupProvider
     */
    @Autowired
    public void setProviders(final List<HostGroupProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(HostGroupProvider::getProviderType, Function.identity()));
    }

    private HostGroupProvider getQueueProvider() {
        final HostGroupProvider hostGroupProvider = providers.get(engineType);
        Assert.notNull(hostGroupProvider, String.format("Provides for type '%s' is not supported", engineType));
        return hostGroupProvider;
    }
}
