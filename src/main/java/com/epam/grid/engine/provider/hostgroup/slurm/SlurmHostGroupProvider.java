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

package com.epam.grid.engine.provider.hostgroup.slurm;

import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.HostGroupFilter;
import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.provider.hostgroup.HostGroupProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SLURM")
public class SlurmHostGroupProvider implements HostGroupProvider {

    @Override
    public EngineType getProviderType() {
        return EngineType.SLURM;
    }

    /**
     * This method only throws an error because Slurm doesn't have an approach of stored host groups.
     * @throws UnsupportedOperationException - slurm doesn't have an approach of stored host groups
     */
    @Override
    public List<HostGroup> listHostGroups(final HostGroupFilter hostGroupFilter) {
        throw new UnsupportedOperationException("Slurm doesn't provide approach of stored host group.");
    }

    /**
     * This method only throws an error because Slurm doesn't have an approach of stored host groups.
     * @throws UnsupportedOperationException - slurm doesn't have an approach of stored host groups
     */
    @Override
    public HostGroup getHostGroup(final String groupName) {
        throw new UnsupportedOperationException("Slurm doesn't provide approach of stored host group.");
    }
}
