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

package com.epam.grid.engine.mapper.hostgroup.sge;

import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.entity.hostgroup.sge.SgeHostGroup;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.thymeleaf.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.grid.engine.utils.TextConstants.SPACE;

@Mapper(componentModel = "spring")
public interface SgeHostGroupMapper {

    /**
     * The actual mapping method expects the source object as parameter and returns the target object.
     *
     * @param sgeHostGroup the source object
     * @return The mapped object
     */
    HostGroup sgeHostGroupToHostGroup(SgeHostGroup sgeHostGroup);

    /**
     * Maps raw output to {@link SgeHostGroup} object.
     * @param hostGroupDescription Map of Strings containing full information about SgeHostGroup
     * @return {@link SgeHostGroup}
     */
    @Mapping(target = "hostGroupName", source = "group_name")
    @Mapping(target = "hostGroupEntry", ignore = true)
    SgeHostGroup mapToSgeHostGroup(Map<String, String> hostGroupDescription);

    @AfterMapping
    default void fillHostGroupDescription(final Map<String, String> hostGroupDescription,
                                          final @MappingTarget SgeHostGroup sgeHostGroup) {
        final String hostList = "hostlist";
        if (StringUtils.isEmpty(hostGroupDescription.get(hostList))) {
            sgeHostGroup.setHostGroupEntry(Collections.emptyList());
        }
        sgeHostGroup.setHostGroupEntry(List.of(hostGroupDescription.get(hostList).split(SPACE)));
    }
}
