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
import com.epam.grid.engine.provider.job.sge.SgeJobProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

@SpringBootTest(properties = {"grid.engine.type=SGE"})
public class JobOperationProviderServiceTest {

    private static final String SGEUSER = "sgeuser";
    private static final String STDIN = "STDIN";
    private static final String QNAME = "main@c242f10e1253";
    private static final String DATE = "1986-04-08T12:30:00";
    private static final String STATE = "running";
    private static final String STATE_CODE = "r";

    @Autowired
    JobOperationProviderService jobOperationProviderService;

    @SpyBean
    SgeJobProvider jobProvider;

    @Test
    public void shouldReturnCorrectInfoDuringDeletion() {
        final DeleteJobFilter deleteJobFilter = DeleteJobFilter.builder()
                .force(false)
                .id(1L)
                .user(SGEUSER)
                .build();
        final DeletedJobInfo expectedDeletedJobInfo = DeletedJobInfo.builder()
                .ids(List.of(1L))
                .user(SGEUSER)
                .build();
        doReturn(expectedDeletedJobInfo).when(jobProvider).deleteJob(deleteJobFilter);
        Assertions.assertEquals(expectedDeletedJobInfo, jobOperationProviderService.deleteJob(deleteJobFilter));
        Mockito.verify(jobProvider, times(1)).deleteJob(deleteJobFilter);
    }

    @Test
    public void shouldReturnCorrectResponse() {
        final Listing<Job> jobListing = listingParser(listParser());
        final JobFilter jobFilter = new JobFilter();
        jobFilter.setIds(Collections.singletonList(7L));

        doReturn(jobListing).when(jobProvider).filterJobs(jobFilter);
        Assertions.assertEquals(jobListing, jobOperationProviderService.filter(jobFilter));
        Mockito.verify(jobProvider, times(1)).filterJobs(jobFilter);
    }

    @Test
    public void shouldReturnCorrectJobRun() {
        final JobOptions options = new JobOptions();
        final Job resultJob = listParser().get(0);

        doReturn(resultJob).when(jobProvider).runJob(options);
        Assertions.assertEquals(resultJob, jobOperationProviderService.runJob(options));
        Mockito.verify(jobProvider, times(1)).runJob(options);
    }

    private static List<Job> listParser() {
        return Collections.singletonList(Job.builder()
                .id(7)
                .priority(0.55500)
                .name(STDIN)
                .owner(SGEUSER)
                .state(stateParser())
                .queueName(QNAME)
                .submissionTime(dateParser())
                .build());
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

    private static JobState stateParser() {
        final JobState jobState = new JobState();
        jobState.setCategory(JobState.Category.RUNNING);
        jobState.setState(STATE);
        jobState.setStateCode(STATE_CODE);
        return jobState;
    }

    private static LocalDateTime dateParser() {
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return LocalDateTime.parse(DATE, formatter);
    }
}
