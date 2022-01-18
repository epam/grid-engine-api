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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SgeJobMapperTest {

    private static final String SOME_JOB_NAME = "someJobName";
    private static final String SOME_OWNER_NAME = "someOwnerName";
    private static final String SOME_QUEUE_NAME = "someQueueName";
    private static final String SOME_STATE = "someState";

    private static final double SOME_PRIORITY = 10.0;
    private static final int SOME_ID = 123;
    private static final int SOME_SLOTS = 25;

    private static final List<String> PENDING_STATUS_CODE_LIST = List.of("qw", "Rq", "hqw", "hRwq");
    private static final List<String> RUNNING_STATUS_CODE_LIST = List.of("r", "hr", "t", "Rr", "Rt");
    private static final List<String> SUSPENDED_STATUS_CODE_LIST = List.of("s", "ts", "S", "tS", "T", "tT",
            "Rs", "Rts", "RS", "RtS", "RT", "RtT");
    private static final List<String> DELETED_STATUS_CODE_LIST = List.of("dr", "dt", "dRr", "dRt", "ds",
            "dS", "dT", "dRs", "dRS", "dRT");
    private static final List<String> FINISHED_STATUS_CODE_LIST = List.of("z");
    private static final List<String> ERROR_STATUS_CODE_LIST = List.of("Eqw", "Ehqw", "EhRqw");
    private static final List<String> UNKNOWN_STATUS_CODE_LIST = List.of("", "Code", "fail", "test");


    private static final SgeJobMapper jobMapper = Mappers.getMapper(SgeJobMapper.class);

    @ParameterizedTest
    @MethodSource({"provideSgeJobStateListAndExpectedJobCategory"})
    public void shouldReturnCorrectJob(final List<String> sgeJobStateCodeList,
                                       final JobState.Category expectedCategory) {
        sgeJobStateCodeList.forEach(sgeJobStateCode -> {
            final JobState expectedJobState = JobState.builder()
                    .state(SOME_STATE)
                    .stateCode(sgeJobStateCode)
                    .category(expectedCategory)
                    .build();

            final LocalDateTime testTime = LocalDateTime.now();

            final SgeJob sgeJob = SgeJob.builder()
                    .id(SOME_ID)
                    .priority(SOME_PRIORITY)
                    .name(SOME_JOB_NAME)
                    .owner(SOME_OWNER_NAME)
                    .state(SOME_STATE)
                    .stateCode(sgeJobStateCode)
                    .submissionTime(testTime)
                    .queueName(SOME_QUEUE_NAME)
                    .slots(SOME_SLOTS)
                    .build();

            final Job expectedJob = Job.builder()
                    .id(SOME_ID)
                    .priority(SOME_PRIORITY)
                    .name(SOME_JOB_NAME)
                    .owner(SOME_OWNER_NAME)
                    .state(expectedJobState)
                    .submissionTime(testTime)
                    .queueName(SOME_QUEUE_NAME)
                    .slots(SOME_SLOTS)
                    .build();

            assertEquals(expectedJob, jobMapper.sgeJobToJob(sgeJob));
        });
    }

    static Stream<Arguments> provideSgeJobStateListAndExpectedJobCategory() {
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
