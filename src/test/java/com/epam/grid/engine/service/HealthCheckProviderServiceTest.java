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

import com.epam.grid.engine.entity.healthcheck.GridEngineStatus;
import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.entity.healthcheck.StatusInfo;
import com.epam.grid.engine.provider.healthcheck.HealthCheckProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class HealthCheckProviderServiceTest {

    private static final String SOME_INFO = "SomeInfo";

    @Autowired
    private HealthCheckProviderService healthCheckProviderService;
    @SpyBean
    private HealthCheckProvider healthCheckProvider;

    @Test
    public void shouldReturnCorrectHealthCheckInfo() {
        final StatusInfo expectedStatusInfo = StatusInfo.builder()
                .code(0L)
                .status(GridEngineStatus.OK)
                .info(SOME_INFO)
                .build();
        final HealthCheckInfo expectedHealthCheckInfo = HealthCheckInfo.builder()
                .statusInfo(expectedStatusInfo)
                .startTime(LocalDateTime.of(1992, 12, 18, 4, 0, 0))
                .checkTime(LocalDateTime.now())
                .build();

        doReturn(expectedHealthCheckInfo).when(healthCheckProvider).checkHealth();
        Assertions.assertEquals(expectedHealthCheckInfo, healthCheckProviderService.checkHealth());
        verify(healthCheckProvider, times(1)).checkHealth();
    }
}
