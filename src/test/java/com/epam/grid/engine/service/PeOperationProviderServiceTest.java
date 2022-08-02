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

import com.epam.grid.engine.TestPropertiesWithSgeEngine;
import com.epam.grid.engine.entity.ParallelEnvFilter;
import com.epam.grid.engine.entity.parallelenv.AllocationRuleType;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.PeRegistrationVO;
import com.epam.grid.engine.entity.parallelenv.RuleState;
import com.epam.grid.engine.entity.parallelenv.UrgencyState;
import com.epam.grid.engine.entity.parallelenv.UrgencyStateType;
import com.epam.grid.engine.provider.parallelenv.ParallelEnvProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

@SpringBootTest
@TestPropertiesWithSgeEngine
public class PeOperationProviderServiceTest {

    private static final String FIVE = "5";
    private static final String MAKE = "make";
    private static final String FILL_UP = "$fill_up";
    private static final String TEST_PE_NAME = "cre";
    private static final List<String> EMPTY_LIST = Collections.emptyList();

    @Autowired
    private ParallelEnvOperationProviderService parallelEnvOperationProviderService;

    @MockBean
    private ParallelEnvProvider parallelEnvProvider;

    @Test
    public void shouldReturnCorrectPostResponse() {
        final ParallelEnv expectedPe = buildPe();

        final List<ParallelEnv> peList = List.of(expectedPe);
        final ParallelEnvFilter parallelEnvFilter = new ParallelEnvFilter(List.of(MAKE));

        doReturn(peList).when(parallelEnvProvider).listParallelEnv(parallelEnvFilter);
        Assertions.assertEquals(peList, parallelEnvOperationProviderService.filterParallelEnvs(parallelEnvFilter));
        Mockito.verify(parallelEnvProvider, times(1)).listParallelEnv(parallelEnvFilter);
    }

    @Test
    public void shouldReturnCorrectGetResponse() {
        final ParallelEnv expectedPe = buildPe();

        doReturn(expectedPe).when(parallelEnvProvider).getParallelEnv(MAKE);
        Assertions.assertEquals(expectedPe, parallelEnvOperationProviderService.getParallelEnv(MAKE));
        Mockito.verify(parallelEnvProvider, times(1)).getParallelEnv(MAKE);
    }

    @Test
    public void shouldReturnCorrectInfoDuringDeletion() {
        final ParallelEnv deletedParallelEnv = ParallelEnv.builder()
                .name(TEST_PE_NAME)
                .build();

        doReturn(deletedParallelEnv).when(parallelEnvProvider).deleteParallelEnv(TEST_PE_NAME);
        Assertions.assertEquals(deletedParallelEnv, parallelEnvOperationProviderService
                .deleteParallelEnv(TEST_PE_NAME));
        Mockito.verify(parallelEnvProvider).deleteParallelEnv(TEST_PE_NAME);
    }

    @Test
    public void shouldReturnCorrectInfoDuringRegistration() {
        final ParallelEnv expectedPe = buildPe();
        final PeRegistrationVO registrationRequest = PeRegistrationVO.builder()
                .name(expectedPe.getName())
                .slots(expectedPe.getSlots())
                .allocationRule(FILL_UP)
                .build();

        doReturn(expectedPe).when(parallelEnvProvider).registerParallelEnv(registrationRequest);
        Assertions.assertEquals(expectedPe, parallelEnvOperationProviderService
                .registerParallelEnv(registrationRequest));
        Mockito.verify(parallelEnvProvider).registerParallelEnv(registrationRequest);
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
                        .allocationRule(AllocationRuleType.FILL_UP)
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
}
