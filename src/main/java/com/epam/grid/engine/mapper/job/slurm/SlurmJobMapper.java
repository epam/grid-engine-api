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

package com.epam.grid.engine.mapper.job.slurm;

import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobState;
import com.epam.grid.engine.entity.job.slurm.SlurmJob;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Set;

/**
 * This interface consists of the configuration of a mapper for implementation by the MapStruct processor
 * for creation a {@link Job} object out of a {@link SlurmJob} object.
 * To get more information about grid engine job status values see <a href="https://manpages.debian.org/testing/gridengine-common/sge_status.5.en.html">
 * Grid Engine job status values</a>.
 */
@Mapper(componentModel = "spring")
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SLURM")
public interface SlurmJobMapper {

    String FINISHED_STATUS_CODE = "COMPLETED";
    Set<String> PENDING_STATUS_CODE_LIST = Set.of("CONFIGURING", "PENDING", "REQUEUE_FED", "REQUEUE_HOLD", "REQUEUED");
    Set<String> RUNNING_STATUS_CODE_LIST = Set.of("COMPLETING", "RUNNING", "RESIZING", "SIGNALING", "STAGE_OUT");
    Set<String> SUSPENDED_STATUS_CODE_LIST = Set.of("RESV_DEL_HOLD", "REVOKED", "STOPPED", "SUSPENDED");
    Set<String> DELETED_STATUS_CODE_LIST = Set.of("SPECIAL_EXIT", "CANCELLED", "TIMEOUT");
    Set<String> ERROR_STATUS_CODE_LIST = Set.of("BOOT_FAIL", "DEADLINE", "FAILED", "NODE_FAIL", "OUT_OF_MEMORY",
            "PREEMPTED");

    /**
     * The actual mapping method expects the source object as parameter and returns the target object.
     *
     * @param slurmJob is a mapping object
     * @return The mapped object
     */
    @Mapping(target = "id", source = "slurmJob.jobId")
    @Mapping(target = "priority", source = "slurmJob.priority")
    @Mapping(target = "name", source = "slurmJob.name")
    @Mapping(target = "owner", source = "slurmJob.userName")
    @Mapping(target = "queueName", source = "slurmJob.partition")
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "state", ignore = true)
    Job slurmJobToJob(final SlurmJob slurmJob);

    @AfterMapping
    default void fillState(final SlurmJob slurmJob, final @MappingTarget Job job) {
        job.setState(mapJobState(slurmJob.getState(), slurmJob.getStateCompact()));
    }

    default JobState mapJobState(final String state, final String stateCode) {
        return JobState.builder()
                .category(defineCategory(state))
                .state(state)
                .stateCode(stateCode)
                .build();
    }

    default JobState.Category defineCategory(final String state) {
        if (state.equals(FINISHED_STATUS_CODE)) {
            return JobState.Category.FINISHED;
        }
        if (PENDING_STATUS_CODE_LIST.contains(state)) {
            return JobState.Category.PENDING;
        }
        if (RUNNING_STATUS_CODE_LIST.contains(state)) {
            return JobState.Category.RUNNING;
        }
        if (SUSPENDED_STATUS_CODE_LIST.contains(state)) {
            return JobState.Category.SUSPENDED;
        }
        if (DELETED_STATUS_CODE_LIST.contains(state)) {
            return JobState.Category.DELETED;
        }
        if (ERROR_STATUS_CODE_LIST.contains(state)) {
            return JobState.Category.ERROR;
        }
        return JobState.Category.UNKNOWN;
    }
}
