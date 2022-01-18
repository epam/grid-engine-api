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

package com.epam.grid.engine.mapper.job.sge;

import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobState;
import com.epam.grid.engine.entity.job.sge.SgeJob;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * This interface consists of the configuration of a mapper for implementation by the MapStruct processor
 * for creation a {@link Job} object out of a {@link SgeJob} object.
 * To get more information about grid engine job status values see <a href="https://manpages.debian.org/testing/gridengine-common/sge_status.5.en.html">
 * Grid Engine job status values</a>.
 */
@Mapper(componentModel = "spring")
public interface SgeJobMapper {

    /**
     * The actual mapping method expects the source object as parameter and returns the target object.
     *
     * @param sgeJob an mapping object
     * @return The mapped object
     */
    @Mapping(target = "state", ignore = true)
    Job sgeJobToJob(SgeJob sgeJob);

    /**
     * The method maps the state attribute of the source object to the target object.
     *
     * @param sgeJob an mapping object
     * @param job    an target object
     */
    @AfterMapping
    default void fillState(final SgeJob sgeJob, final @MappingTarget Job job) {
        job.setState(mapJobState(sgeJob.getState(), sgeJob.getStateCode()));
    }

    default JobState mapJobState(final String state, final String stateCode) {
        return JobState.builder()
                .category(determineStateCategory(stateCode))
                .state(state)
                .stateCode(stateCode)
                .build();
    }

    default JobState.Category determineStateCategory(final String stateCode) {
        final String finishedStatusCode = "z";
        final List<String> pendingStatusCodeList = List.of("qw", "Rq", "hqw", "hRwq");
        final List<String> runningStatusCodeList = List.of("r", "hr", "t", "Rr", "Rt");
        final List<String> suspendedStatusCodeList = List.of("s", "ts", "S", "tS", "T", "tT",
                "Rs", "Rts", "RS", "RtS", "RT", "RtT");
        final List<String> deletedStatusCodeList = List.of("dr", "dt", "dRr", "dRt", "ds",
                "dS", "dT", "dRs", "dRS", "dRT");
        final List<String> errorStatusCodeList = List.of("Eqw", "Ehqw", "EhRqw");

        if (stateCode.equals(finishedStatusCode)) {
            return JobState.Category.FINISHED;
        }
        if (pendingStatusCodeList.contains(stateCode)) {
            return JobState.Category.PENDING;
        }
        if (runningStatusCodeList.contains(stateCode)) {
            return JobState.Category.RUNNING;
        }
        if (suspendedStatusCodeList.contains(stateCode)) {
            return JobState.Category.SUSPENDED;
        }
        if (deletedStatusCodeList.contains(stateCode)) {
            return JobState.Category.DELETED;
        }
        if (errorStatusCodeList.contains(stateCode)) {
            return JobState.Category.ERROR;
        }
        return JobState.Category.UNKNOWN;
    }
}
