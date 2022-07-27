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

import com.epam.grid.engine.entity.JobFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.JobState;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.job.JobProvider;
import com.epam.grid.engine.provider.log.JobLogProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_STRING;

@SpringBootTest(properties = {"grid.engine.type=SGE"})
public class JobOperationProviderServiceTest {

    private static final long SOME_JOB_ID = 7L;
    private static final long SOME_WRONG_JOB_ID = 0L;
    private static final String SGEUSER = "sgeuser";
    private static final String SOME_JOB_NAME = "someName";
    private static final String SOME_COMMAND = "simple.sh";
    private static final String QNAME = "main@c242f10e1253";
    private static final String DATE = "1986-04-08T12:30:00";
    private static final String PENDING_STATE = "pending";
    private static final String SOME_JOB_LOG_DIRECTORY_PATH = "/mnt/logs";


    @Autowired
    private JobOperationProviderService jobOperationProviderService;

    @MockBean
    private JobProvider jobProvider;

    @MockBean
    private JobLogProvider jobLogProvider;

    @Test
    public void shouldReturnCorrectInfoDuringDeletion() {
        final DeleteJobFilter deleteJobFilter = DeleteJobFilter.builder()
                .id(SOME_JOB_ID)
                .build();
        final DeletedJobInfo expectedDeletedJobInfo = DeletedJobInfo.builder()
                .ids(List.of(SOME_JOB_ID))
                .user(SGEUSER)
                .build();
        Mockito.doReturn(expectedDeletedJobInfo).when(jobProvider).deleteJob(deleteJobFilter);
        Assertions.assertEquals(expectedDeletedJobInfo, jobOperationProviderService.deleteJob(deleteJobFilter));
    }

    @Test
    public void shouldReturnCorrectResponse() {
        final Listing<Job> jobListing = listingParser(List.of(getPendingJob()));
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setIds(Collections.singletonList(SOME_JOB_ID));

        Mockito.doReturn(jobListing).when(jobProvider).filterJobs(jobFilter);
        Assertions.assertEquals(jobListing, jobOperationProviderService.filter(jobFilter));
    }

    @ParameterizedTest
    @MethodSource("provideWrongDeleteRequests")
    public void shouldThrowsExceptionDuringDeletionBecauseNotCorrectRequest(final Long id, final String user) {
        final DeleteJobFilter deleteJobFilter = DeleteJobFilter.builder()
                .id(id)
                .user(user)
                .build();
        Assertions.assertThrows(GridEngineException.class,
                () -> jobOperationProviderService.deleteJob(deleteJobFilter));
    }

    static Stream<Arguments> provideWrongDeleteRequests() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, EMPTY_STRING),
                Arguments.of(SOME_JOB_ID, SGEUSER),
                Arguments.of(SOME_WRONG_JOB_ID, null),
                Arguments.of(SOME_WRONG_JOB_ID, EMPTY_STRING)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void shouldThrowBecauseNoCommandPassedToSubmit(final String command) {
        final JobOptions jobOptions = JobOptions.builder()
                .command(command)
                .build();
        Assertions.assertThrows(GridEngineException.class, () -> jobOperationProviderService.runJob(jobOptions));
    }

    @Test
    public void shouldReturnCorrectJobRun() {
        final JobOptions options = JobOptions.builder().command(SOME_COMMAND).build();
        final Job resultJob = getPendingJob();

        Mockito.doReturn(SOME_JOB_LOG_DIRECTORY_PATH).when(jobLogProvider).getJobLogDir();
        Mockito.doReturn(resultJob).when(jobProvider).runJob(options, SOME_JOB_LOG_DIRECTORY_PATH);
        Assertions.assertEquals(resultJob, jobOperationProviderService.runJob(options));
    }

    private static Job getPendingJob() {
        return Job.builder()
                .id(SOME_JOB_ID)
                .priority(0.55500)
                .name(SOME_JOB_NAME)
                .owner(SGEUSER)
                .state(JobState.builder()
                        .category(JobState.Category.PENDING)
                        .state(PENDING_STATE)
                        .build())
                .queueName(QNAME)
                .submissionTime(LocalDateTime.parse(DATE, DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    private static Listing<Job> listingParser(final List<Job> sgeJobs) {
        return new Listing<>(sgeJobs.stream()
                .map(sgeJob -> Job.builder()
                        .id(sgeJob.getId())
                        .priority(sgeJob.getPriority())
                        .name(sgeJob.getName())
                        .owner(sgeJob.getOwner())
                        .state(sgeJob.getState())
                        .submissionTime(sgeJob.getSubmissionTime())
                        .queueName(sgeJob.getQueueName())
                        .slots(sgeJob.getSlots())
                        .build())
                .collect(Collectors.toList()));
    }
}
