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

package com.epam.grid.engine.controller.parallelenv;

import com.epam.grid.engine.controller.AbstractControllerTest;
import com.epam.grid.engine.entity.parallelenv.AllocationRuleType;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.PeRegistrationVO;
import com.epam.grid.engine.entity.parallelenv.RuleState;
import com.epam.grid.engine.entity.parallelenv.UrgencyState;
import com.epam.grid.engine.entity.parallelenv.UrgencyStateType;
import com.epam.grid.engine.service.ParallelEnvOperationProviderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@WebMvcTest(ParallelEnvController.class)
public class PeOperationControllerTest extends AbstractControllerTest {

    private static final String GET_OR_DELETE_URI = "/parallelenv/{name}";
    private static final String POST_URI = "/parallelenv/filter";
    private static final String REGISTRATION_URI = "/parallelenv";
    private static final String MAKE = "make";
    private static final String FIVE = "5";
    private static final String TEST_PE_NAME = "cre";
    private static final List<String> EMPTY_LIST = Collections.emptyList();

    @MockBean
    private ParallelEnvOperationProviderService parallelEnvOperationProviderService;

    @Test
    public void shouldReturnPostStatusAndValue() throws Exception {
        final ParallelEnv expectedPe = buildPe();

        final List<ParallelEnv> peList = List.of(expectedPe);
        Mockito.when(parallelEnvOperationProviderService.filterParallelEnvs(null)).thenReturn(peList);

        final String response = performMvcRequest(MockMvcRequestBuilders.post(POST_URI))
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(peList));
    }

    @Test
    public void shouldReturnGetStatusAndValue() throws Exception {
        final ParallelEnv expectedPe = buildPe();

        Mockito.when(parallelEnvOperationProviderService.getParallelEnv(MAKE))
                .thenReturn(expectedPe);

        final String response = performMvcRequest(MockMvcRequestBuilders.get(GET_OR_DELETE_URI, MAKE)
                .contentType(MediaType.APPLICATION_JSON))
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedPe));
    }

    private static ParallelEnv buildPe() {
        return ParallelEnv.builder()
                .name(MAKE)
                .slots(999)
                .allowedUserGroups(EMPTY_LIST)
                .forbiddenUserGroups(EMPTY_LIST)
                .startProcArgs(EMPTY_LIST)
                .stopProcArgs(EMPTY_LIST)
                .allocationRule(RuleState.builder()
                        .allocationRule(AllocationRuleType.SLOTS_ON_ASSIGNED_HOST)
                        .originalState(FIVE)
                        .stateNumber(5)
                        .build())
                .controlSlaves(true)
                .jobIsFirstTask(true)
                .urgencySlots(UrgencyState.builder()
                        .urgencyStateType(UrgencyStateType.NUMBER)
                        .state(5)
                        .build())
                .accountingSummary(true)
                .build();
    }

    @Test
    public void shouldReturnJsonAndOkStatusForDeletion() throws Exception {
        final ParallelEnv deletedParallelEnv = buildParallelEnv();

        doReturn(deletedParallelEnv).when(parallelEnvOperationProviderService).deleteParallelEnv(TEST_PE_NAME);

        final MvcResult mvcResult =
                performMvcRequest(MockMvcRequestBuilders
                        .delete(GET_OR_DELETE_URI, TEST_PE_NAME)
                        .contentType(MediaType.APPLICATION_JSON));

        verify(parallelEnvOperationProviderService).deleteParallelEnv(TEST_PE_NAME);
        final String actual = mvcResult.getResponse().getContentAsString();
        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(deletedParallelEnv));
    }

    @Test
    public void shouldReturnJsonValueAndOkStatusForNewPeName() throws Exception {

        final ParallelEnv expectedPe = ParallelEnv.builder()
                .name(TEST_PE_NAME)
                .slots(4)
                .build();
        final PeRegistrationVO registrationRequest = PeRegistrationVO.builder()
                .name(expectedPe.getName())
                .slots(expectedPe.getSlots())
                .build();

        doReturn(expectedPe).when(parallelEnvOperationProviderService).registerParallelEnv(registrationRequest);
        final MvcResult mvcResult = performMvcResultWithContent(MockMvcRequestBuilders
                .post(REGISTRATION_URI), registrationRequest);
        verify(parallelEnvOperationProviderService).registerParallelEnv(registrationRequest);
        final String actual = mvcResult.getResponse().getContentAsString();

        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedPe));
    }

    private static ParallelEnv buildParallelEnv() {
        return ParallelEnv.builder()
                .name(PeOperationControllerTest.TEST_PE_NAME)
                .build();
    }
}
