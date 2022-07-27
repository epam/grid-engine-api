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

package com.epam.grid.engine.provider.hostgroup;

import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.entity.HostGroupFilter;
import com.epam.grid.engine.provider.CommandTypeAware;

import java.util.List;

/**
 * The interface which provides methods for different
 * types of grid engines to operate with {@link HostGroup}.
 */
public interface HostGroupProvider extends CommandTypeAware {

    /**
     * Returns a List containing specified {@link HostGroup}s with respect to provided {@link HostGroupFilter}.
     * @param hostGroupFilter a provided filter
     * @return a List containing specified HostGroups with respect to provided filter
     */
    List<HostGroup> listHostGroups(HostGroupFilter hostGroupFilter);

    /**
     * Returns {@link HostGroup} by specified group name.
     *
     * @param groupName The specified group name.
     * @return Information about the group.
     */
    HostGroup getHostGroup(String groupName);
}
