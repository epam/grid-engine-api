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

package com.epam.grid.engine.controller.healthcheck;

import com.epam.grid.engine.controller.AbstractControllerTest;
import com.epam.grid.engine.entity.healthcheck.GridEngineStatus;
import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.entity.healthcheck.StatusInfo;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.service.HealthCheckProviderService;
import lombok.SneakyThrows;
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

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthCheckController.class)
public class HealthCheckControllerTest extends AbstractControllerTest {

    private static final String URI = "/check";
    private static final String SOME_INFO = "Some Info";

    @MockBean
    private HealthCheckProviderService healthCheckProviderService;

    @Test
    public void shouldReturnOkStatusAndJsonValue() throws Exception {
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

        doReturn(expectedHealthCheckInfo).when(healthCheckProviderService).checkHealth();

        final MvcResult mvcResult = performMvcRequest(MockMvcRequestBuilders.get(URI));

        verify(healthCheckProviderService).checkHealth();
        final String actual = mvcResult.getResponse().getContentAsString();
        assertThat(actual).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedHealthCheckInfo));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideBadCases")
    public void shouldReturnBadStatusesAndThrowsExceptionDuringCheckHealth(
            final HttpStatus httpStatus,
            final int expectedStatus
    ) {
        doThrow(new GridEngineException(httpStatus, "Grid engine is not reachable"))
                .when(healthCheckProviderService).checkHealth();

        mvc.perform(MockMvcRequestBuilders.get(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus))
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getClass().equals(GridEngineException.class));
    }

    static Stream<Arguments> provideBadCases() {
        return Stream.of(
                Arguments.of(HttpStatus.NOT_FOUND, 404),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, 500)
        );
    }
}
