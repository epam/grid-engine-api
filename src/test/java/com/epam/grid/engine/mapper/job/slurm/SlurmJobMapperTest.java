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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlurmJobMapperTest {

    private static final String SOME_JOB_NAME = "jobName";
    private static final String SOME_OWNER_NAME = "randomUser";
    private static final String SOME_QUEUE_NAME = "partitionName";

    private static final double SOME_PRIORITY = 0.99998474121093;
    private static final int SOME_ID = 5;

    private static final List<List<String>> FINISHED_STATUS_CODE_LIST = List.of(
            List.of("CD", "COMPLETED")
    );
    private static final List<List<String>> PENDING_STATUS_CODE_LIST = List.of(
            List.of("CF", "CONFIGURING"),
            List.of("PD", "PENDING"),
            List.of("RF", "REQUEUE_FED"),
            List.of("RH", "REQUEUE_HOLD"),
            List.of("RQ", "REQUEUED")
    );
    private static final List<List<String>> RUNNING_STATUS_CODE_LIST = List.of(
            List.of("CG", "COMPLETING"),
            List.of("R", "RUNNING"),
            List.of("RS", "RESIZING"),
            List.of("SI", "SIGNALING"),
            List.of("SO", "STAGE_OUT")
    );
    private static final List<List<String>> SUSPENDED_STATUS_CODE_LIST = List.of(
            List.of("RD", "RESV_DEL_HOLD"),
            List.of("RV", "REVOKED"),
            List.of("ST", "STOPPED"),
            List.of("S", "SUSPENDED")
    );
    private static final List<List<String>> DELETED_STATUS_CODE_LIST = List.of(
            List.of("SE", "SPECIAL_EXIT"),
            List.of("CA", "CANCELLED"),
            List.of("TO", "TIMEOUT")
    );
    private static final List<List<String>> ERROR_STATUS_CODE_LIST = List.of(
            List.of("BF", "BOOT_FAIL"),
            List.of("DL", "DEADLINE"),
            List.of("F", "FAILED"),
            List.of("NF", "NODE_FAIL"),
            List.of("OOM", "OUT_OF_MEMORY"),
            List.of("PR", "PREEMPTED")
    );
    private static final List<List<String>> UNKNOWN_STATUS_CODE_LIST = List.of(
            List.of("", "")
    );

    private static final SlurmJobMapper jobMapper = Mappers.getMapper(SlurmJobMapper.class);

    @ParameterizedTest
    @MethodSource({"getSlurmJobStateListAndExpectedJobCategory"})
    public void shouldReturnCorrectJob(final List<List<String>> slurmJobStateCodeList,
                                       final JobState.Category expectedCategory) {
        slurmJobStateCodeList.forEach(statusList -> {
            final JobState expectedJobState = JobState.builder()
                    .state(statusList.get(1))
                    .stateCode(statusList.get(0))
                    .category(expectedCategory)
                    .build();

            final LocalDateTime testTime = LocalDateTime.now();

            final SlurmJob slurmJob = SlurmJob.builder()
                    .jobId(SOME_ID)
                    .priority(SOME_PRIORITY)
                    .name(SOME_JOB_NAME)
                    .userName(SOME_OWNER_NAME)
                    .state(statusList.get(1))
                    .stateCompact(statusList.get(0))
                    .submissionTime(testTime)
                    .partition(SOME_QUEUE_NAME)
                    .build();

            final Job expectedJob = Job.builder()
                    .id(SOME_ID)
                    .priority(SOME_PRIORITY)
                    .name(SOME_JOB_NAME)
                    .owner(SOME_OWNER_NAME)
                    .state(expectedJobState)
                    .submissionTime(testTime)
                    .queueName(SOME_QUEUE_NAME)
                    .build();

            assertEquals(expectedJob, jobMapper.slurmJobToJob(slurmJob));
        });
    }

    static Stream<Arguments> getSlurmJobStateListAndExpectedJobCategory() {
        return Stream.of(
                Arguments.of(PENDING_STATUS_CODE_LIST, JobState.Category.PENDING),
                Arguments.of(RUNNING_STATUS_CODE_LIST, JobState.Category.RUNNING),
                Arguments.of(SUSPENDED_STATUS_CODE_LIST, JobState.Category.SUSPENDED),
                Arguments.of(DELETED_STATUS_CODE_LIST, JobState.Category.DELETED),
                Arguments.of(FINISHED_STATUS_CODE_LIST, JobState.Category.FINISHED),
                Arguments.of(ERROR_STATUS_CODE_LIST, JobState.Category.ERROR),
                Arguments.of(UNKNOWN_STATUS_CODE_LIST, JobState.Category.UNKNOWN));
    }
}
