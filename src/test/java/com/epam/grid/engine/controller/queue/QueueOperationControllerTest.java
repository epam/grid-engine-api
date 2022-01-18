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

package com.epam.grid.engine.controller.queue;

import com.epam.grid.engine.controller.AbstractControllerTest;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.QueueVO;
import com.epam.grid.engine.entity.queue.SlotsDescription;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.service.QueueOperationProviderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QueueOperationController.class)
public class QueueOperationControllerTest extends AbstractControllerTest {

    private static final String TEST_QUEUE_NAME = "main";
    private static final String URI1 = "/queues";
    private static final String URI2 = "/queues/filter";
    private static final String URI3 = "/queues/{queue_name}";

    private static final String QNAME = "queue";
    private static final String WRONG_NAME = "wrong_name";
    private static final List<String> HOST_LIST_DEFAULT = Collections.singletonList("@allhosts");
    private static final List<String> PE_LIST_DEFAULT = Collections.singletonList("make smp mpi");
    private static final List<String> PE_LIST_MODIFIED = Collections.singletonList("mpi");
    private static final List<String> OWNER_LIST_DEFAULT = Collections.singletonList("NONE");
    private static final List<String> USER_LIST_DEFAULT = Collections.singletonList("arusers");

    @MockBean
    QueueOperationProviderService queueOperationProviderService;
    private final SlotsDescription slotsDescription = new SlotsDescription(1,
            Map.of("863431bb452c", 1));

    @Test
    public void shouldReturnJsonValueAndOkStatusForQueueNames() throws Exception {
        final Queue expectedQueue1 = Queue.builder()
                .name("all.q")
                .build();
        final Queue expectedQueue2 = Queue.builder()
                .name("main")
                .build();
        final List<Queue> expectedQueues = List.of(expectedQueue1, expectedQueue2);
        doReturn(expectedQueues).when(queueOperationProviderService).listQueues();
        final MvcResult mvcResult = performMvcRequest(MockMvcRequestBuilders.get(URI1));
        verify(queueOperationProviderService).listQueues();
        final String actual = mvcResult.getResponse().getContentAsString();

        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedQueues));
    }

    @Test
    public void shouldReturnJsonValueAndOkStatusForQueues() throws Exception {
        final Queue expectedQueue = Queue.builder()
                .name("all.q")
                .hostList(HOST_LIST_DEFAULT)
                .numberInSchedulingOrder(Integer.parseInt("0"))
                .loadThresholds(Map.of("np_load_avg", 1.75))
                .suspendThresholds(Map.of())
                .numOfSuspendedJobs(Integer.parseInt("1"))
                .interval("00:05:00")
                .jobPriority(Integer.parseInt("0"))
                .qtype("BATCH INTERACTIVE")
                .parallelEnvironmentNames(PE_LIST_DEFAULT)
                .slots(slotsDescription)
                .tmpDir("/tmp")
                .build();
        final List<Queue> expectedResult = List.of(expectedQueue);
        Mockito.when(queueOperationProviderService.listQueues(null)).thenReturn(expectedResult);
        final String response = performMvcRequest(MockMvcRequestBuilders.post(URI2))
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedResult));
    }

    @Test
    public void shouldReturnJsonValueAndOkStatusForDeletion() throws Exception {
        final Queue deletedQueue = buildQueue();

        doReturn(deletedQueue).when(queueOperationProviderService).deleteQueue(TEST_QUEUE_NAME);

        final MvcResult mvcResult =
                performMvcRequest(MockMvcRequestBuilders
                        .delete(URI3, TEST_QUEUE_NAME)
                        .contentType(MediaType.APPLICATION_JSON));

        verify(queueOperationProviderService).deleteQueue(TEST_QUEUE_NAME);
        final String actual = mvcResult.getResponse().getContentAsString();
        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(deletedQueue));
    }

    private static Queue buildQueue() {
        return Queue.builder()
                .name(QueueOperationControllerTest.TEST_QUEUE_NAME)
                .build();
    }

    @Test
    public void shouldReturnJsonValueAndOkStatusForNewQueueName() throws Exception {

        final Queue expectedQueue = Queue.builder()
                .name(QNAME)
                .hostList(HOST_LIST_DEFAULT)
                .build();
        final QueueVO registrationRequest = QueueVO.builder()
                .name(expectedQueue.getName())
                .hostList(expectedQueue.getHostList())
                .build();

        doReturn(expectedQueue).when(queueOperationProviderService).registerQueue(registrationRequest);
        final MvcResult mvcResult = performMvcResultWithContent(MockMvcRequestBuilders.post(URI1), registrationRequest);
        verify(queueOperationProviderService).registerQueue(registrationRequest);
        final String actual = mvcResult.getResponse().getContentAsString();

        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedQueue));
    }

    @Test
    public void shouldReturnErrorCodeForWrongQueueRegistrationRequest() throws Exception {

        final QueueVO registrationRequest = QueueVO.builder()
                .name(WRONG_NAME)
                .build();
        doThrow(new GridEngineException(HttpStatus.NOT_FOUND, "Grid engine failed queue registration"))
                .when(queueOperationProviderService).registerQueue(registrationRequest);
        final int responseStatus = 404;
        mvc.perform(MockMvcRequestBuilders.post(URI1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(registrationRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(responseStatus))
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getClass().equals(GridEngineException.class));
    }

    @Test
    public void shouldReturnJsonValueAndOkStatusForModification() throws Exception {

        final QueueVO updateRequest = QueueVO.builder()
                .name(QNAME)
                .parallelEnvironmentNames(PE_LIST_MODIFIED)
                .build();
        final Queue modifiedQueue = Queue.builder()
                .name(QNAME)
                .hostList(HOST_LIST_DEFAULT)
                .parallelEnvironmentNames(PE_LIST_MODIFIED)
                .ownerList(OWNER_LIST_DEFAULT)
                .allowedUserGroups(USER_LIST_DEFAULT)
                .build();

        doReturn(modifiedQueue).when(queueOperationProviderService).updateQueue(updateRequest);
        final MvcResult mvcResult = performMvcResultWithContent(MockMvcRequestBuilders.put(URI1), updateRequest);
        verify(queueOperationProviderService).updateQueue(updateRequest);
        final String actual = mvcResult.getResponse().getContentAsString();

        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(modifiedQueue));
    }

    @Test
    public void shouldReturnErrorCodeForWrongQueueModificationRequest() throws Exception {

        final QueueVO updateRequest = QueueVO.builder()
                .name(WRONG_NAME)
                .build();
        doThrow(new GridEngineException(HttpStatus.NOT_FOUND, "Grid engine failed queue update"))
                .when(queueOperationProviderService).updateQueue(updateRequest);
        final int responseStatus = 404;
        mvc.perform(MockMvcRequestBuilders.put(URI1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(responseStatus))
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getClass().equals(GridEngineException.class));
    }
}
