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

package com.epam.grid.engine.mapper.queue.slurm;

import com.epam.grid.engine.entity.host.slurm.SlurmHost;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.SlotsDescription;
import com.epam.grid.engine.entity.queue.slurm.SlurmQueue;
import org.apache.commons.collections4.ListUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SlurmQueueMapper {

    @Mapping(target = "numberInSchedulingOrder", ignore = true)
    @Mapping(target = "loadThresholds", ignore = true)
    @Mapping(target = "suspendThresholds", ignore = true)
    @Mapping(target = "numOfSuspendedJobs", ignore = true)
    @Mapping(target = "interval", ignore = true)
    @Mapping(target = "jobPriority", ignore = true)
    @Mapping(target = "qtype", ignore = true)
    @Mapping(target = "parallelEnvironmentNames", ignore = true)
    @Mapping(target = "ownerList", ignore = true)
    @Mapping(target = "tmpDir", ignore = true)
    @Mapping(source = "partition", target = "name")
    @Mapping(source = "nodelist", target = "hostList")
    @Mapping(source = "groups", target = "allowedUserGroups")
    Queue slurmQueueToQueue(SlurmQueue slurmQueue);

    @AfterMapping
    default void fillSlots(final SlurmQueue slurmQueue, final @MappingTarget Queue queue) {
        queue.setSlots(mapSlurmHostListToSlots(slurmQueue.getNodelist()));
    }

    default List<String> slurmHostListToNames(final List<SlurmHost> hostList) {
        return hostList.stream().map(SlurmHost::getNodeName).collect(Collectors.toList());
    }

    static SlotsDescription mapSlurmHostListToSlots(final List<SlurmHost> nodeList) {
        final SlotsDescription slotsDescription = new SlotsDescription();
        final Map<String, Integer> slots = ListUtils.emptyIfNull(nodeList).stream()
                .collect(Collectors.toMap(
                        SlurmHost::getNodeName,
                        SlurmHost::getCpuTotal)
                );
        slotsDescription.setSlots(nodeList.stream().mapToInt(SlurmHost::getCpuTotal).sum());
        slotsDescription.setSlotsDetails(slots);
        return slotsDescription;
    }
}
