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

package com.epam.grid.engine.controller.job;

import com.epam.grid.engine.controller.AbstractControllerTest;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.job.DeleteJobFilter;
import com.epam.grid.engine.entity.job.DeletedJobInfo;
import com.epam.grid.engine.entity.job.Job;
import com.epam.grid.engine.entity.job.JobLogInfo;
import com.epam.grid.engine.entity.job.JobOptions;
import com.epam.grid.engine.entity.job.JobState;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.service.JobOperationProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobOperationController.class)
public class JobOperationControllerTest extends AbstractControllerTest {

    private static final String URI = "/jobs";
    private static final String RUN_JOB_URI = URI + "/submit";
    private static final String GET_JOB_LOG_URI = URI + "/{job_id}/logs";
    private static final String GET_JOB_LOG_FILE_URI = GET_JOB_LOG_URI + "/file";
    private static final String SGEUSER = "sgeuser";
    private static final String EMPTY_STRING = "";

    private static final long SOME_JOB_ID = 10;
    private static final String NUMBER_OF_LINES = "1";
    private static final String FROM_HEAD_VALUE = "false";
    private static final String SOME_JOB_NAME = "someJobName";
    private static final JobLogInfo.Type SOME_LOG_TYPE = JobLogInfo.Type.OUT;
    private static final String TEST_DATA_STRING = "There is some data to test a response.";


    @MockBean
    private JobOperationProviderService jobOperationProviderService;

    @Test
    public void shouldReturnJsonValueAndOkStatus() throws Exception {
        final Job expectedFirstJob = Job.builder()
                .id(8)
                .name(SOME_JOB_NAME)
                .priority(0.555)
                .owner(SGEUSER)
                .queueName("main@c242f10e1253")
                .submissionTime(LocalDateTime.parse("2021-07-02T10:46:14"))
                .slots(1)
                .state(JobState.builder().category(JobState.Category.RUNNING).state("running").stateCode("r").build())
                .build();
        final Job expectedSecondJob = Job.builder()
                .id(2)
                .name(SOME_JOB_NAME)
                .priority(0.0)
                .owner(SGEUSER)
                .queueName(EMPTY_STRING)
                .submissionTime(LocalDateTime.parse("2021-06-30T17:27:30"))
                .slots(1)
                .state(JobState.builder().category(JobState.Category.PENDING).state("pending").stateCode("qw").build())
                .build();
        final List<Job> jobList = Arrays.asList(expectedFirstJob, expectedSecondJob);
        final Listing<Job> expectedResult = new Listing<>();
        expectedResult.setElements(jobList);
        doReturn(expectedResult).when(jobOperationProviderService).filter(null);

        final MvcResult mvcResult = performMvcRequest(MockMvcRequestBuilders.post(URI));

        verify(jobOperationProviderService).filter(null);
        final String actual = mvcResult.getResponse().getContentAsString();
        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedResult));
    }

    @Test
    public void shouldReturnJsonValueAndOkStatusForDeletion() throws Exception {
        final DeleteJobFilter deleteJobFilter = DeleteJobFilter.builder()
                .force(false)
                .ids(List.of(SOME_JOB_ID))
                .user(SGEUSER)
                .build();
        final Listing<DeletedJobInfo> expectedResult = new Listing<>(List.of(new DeletedJobInfo(SOME_JOB_ID, SGEUSER)));
        doReturn(expectedResult).when(jobOperationProviderService).deleteJob(deleteJobFilter);

        final MvcResult mvcResult =
                performMvcResultWithContent(MockMvcRequestBuilders.delete(URI), deleteJobFilter);

        verify(jobOperationProviderService).deleteJob(deleteJobFilter);
        final String actual = mvcResult.getResponse().getContentAsString();
        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedResult));
    }

    @ParameterizedTest
    @MethodSource("provideBadCasesForDeletion")
    public void shouldReturnBadStatusesAndThrowsExceptionForDeletion(
            final HttpStatus httpStatus,
            final DeleteJobFilter deleteJobFilter) throws Exception {

        doThrow(new GridEngineException(httpStatus, "job with specified id not found"))
                .when(jobOperationProviderService).deleteJob(deleteJobFilter);

        mvc.perform(MockMvcRequestBuilders.delete(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(deleteJobFilter))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(httpStatus.value()))
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getClass().equals(GridEngineException.class));
    }

    static Stream<Arguments> provideBadCasesForDeletion() {
        return Stream.of(
                Arguments.of(HttpStatus.NOT_FOUND, DeleteJobFilter.builder()
                        .force(false)
                        .ids(List.of(SOME_JOB_ID))
                        .user(SGEUSER)
                        .build()),
                Arguments.of(HttpStatus.BAD_REQUEST, DeleteJobFilter.builder()
                        .force(false)
                        .ids(List.of(0L))
                        .user(SGEUSER)
                        .build()),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, DeleteJobFilter.builder()
                        .force(true)
                        .ids(List.of(2L))
                        .user(SGEUSER)
                        .build())
        );
    }

    @Test
    public void shouldReturnCorrectJsonValueAndOkStatusForRunningNewJob() throws Exception {
        final JobOptions runningRequest = JobOptions.builder()
                .command("simple.sh")
                .build();

        final Job expectedJob = Job.builder()
                .id(1)
                .state(JobState.builder()
                        .state(EMPTY_STRING)
                        .stateCode(EMPTY_STRING)
                        .category(JobState.Category.PENDING)
                        .build())
                .build();

        doReturn(expectedJob).when(jobOperationProviderService).runJob(runningRequest);

        final MvcResult mvcResult = performMvcResultWithContent(
                MockMvcRequestBuilders.post(RUN_JOB_URI), runningRequest);
        verify(jobOperationProviderService).runJob(runningRequest);

        final String actual = mvcResult.getResponse().getContentAsString();
        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedJob));
    }

    @ParameterizedTest
    @MethodSource("provideBadCasesForRunning")
    public void shouldReturnBadStatusesAndThrowsExceptionForRunning(
            final HttpStatus httpStatus,
            final JobOptions runningRequest) throws Exception {
        doThrow(new GridEngineException(httpStatus, "Something went wrong when running a new job."))
                .when(jobOperationProviderService).runJob(runningRequest);

        mvc.perform(MockMvcRequestBuilders.post(RUN_JOB_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(runningRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(httpStatus.value()))
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getClass().equals(GridEngineException.class));
    }

    static Stream<Arguments> provideBadCasesForRunning() {
        return Stream.of(
                Arguments.of(HttpStatus.BAD_REQUEST, JobOptions.builder()
                        .name(SOME_JOB_NAME)
                        .build()),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, JobOptions.builder()
                        .name(SOME_JOB_NAME)
                        .build())
        );
    }

    @Test
    public void shouldReturnCorrectJsonValueWhenGettingJobLogInfo() throws Exception {
        final JobLogInfo expectedInfo = JobLogInfo.builder()
                .jobId(SOME_JOB_ID)
                .type(SOME_LOG_TYPE)
                .lines(List.of(TEST_DATA_STRING))
                .totalCount(5)
                .bytes(45)
                .build();

        doReturn(expectedInfo).when(jobOperationProviderService)
                .getJobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE, Integer.parseInt(NUMBER_OF_LINES),
                        Boolean.parseBoolean(FROM_HEAD_VALUE));

        final MvcResult mvcResult = performMvcRequest(
                MockMvcRequestBuilders.get(GET_JOB_LOG_URI, SOME_JOB_ID)
                        .queryParam("type", SOME_LOG_TYPE.name())
                        .queryParam("lines", NUMBER_OF_LINES)
                        .queryParam("fromHead", FROM_HEAD_VALUE));
        verify(jobOperationProviderService)
                .getJobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE, Integer.parseInt(NUMBER_OF_LINES),
                        Boolean.parseBoolean(FROM_HEAD_VALUE));

        final String actual = mvcResult.getResponse().getContentAsString();
        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedInfo));
    }

    @ParameterizedTest
    @MethodSource("provideBadCasesForGettingJobLogInfo")
    public void shouldReturnBadStatusesAndThrowsExceptionForGettingJobLogInfo(
            final HttpStatus httpStatus) throws Exception {
        doThrow(new GridEngineException(httpStatus, "Something went wrong when getting a log info."))
                .when(jobOperationProviderService)
                .getJobLogInfo(SOME_JOB_ID, SOME_LOG_TYPE, Integer.parseInt(NUMBER_OF_LINES),
                        Boolean.parseBoolean(FROM_HEAD_VALUE));

        mvc.perform(MockMvcRequestBuilders.get(GET_JOB_LOG_URI, SOME_JOB_ID)
                        .queryParam("type", SOME_LOG_TYPE.name())
                        .queryParam("lines", NUMBER_OF_LINES)
                        .queryParam("fromHead", FROM_HEAD_VALUE))
                .andExpect(status().is(httpStatus.value()))
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getClass().equals(GridEngineException.class));
    }

    static Stream<Arguments> provideBadCasesForGettingJobLogInfo() {
        return Stream.of(
                        HttpStatus.BAD_REQUEST,
                        HttpStatus.NOT_FOUND,
                        HttpStatus.INTERNAL_SERVER_ERROR)
                .map(Arguments::of);
    }

    @Test
    public void shouldReturnCorrectValueWhenGettingJobLogFile() throws Exception {
        final String expectedContentType = "application/octet-stream";
        final InputStream testInputStream = new ByteArrayInputStream(TEST_DATA_STRING.getBytes());

        doReturn(testInputStream).when(jobOperationProviderService)
                .getJobLogFile(SOME_JOB_ID, SOME_LOG_TYPE);

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(GET_JOB_LOG_FILE_URI, SOME_JOB_ID)
                        .queryParam("type", SOME_LOG_TYPE.name())
                        .accept(expectedContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(expectedContentType))
                .andReturn();

        verify(jobOperationProviderService).getJobLogFile(SOME_JOB_ID, SOME_LOG_TYPE);

        final String result = mvcResult.getResponse().getContentAsString();
        assertEquals(TEST_DATA_STRING, result);
    }
}
