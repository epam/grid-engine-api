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

package com.epam.grid.engine.provider.healthcheck.sge;

import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.healthcheck.GridEngineStatus;
import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.entity.healthcheck.StatusInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
public class SgeHealthCheckProviderTest {

    private static final String SOME_HOST = "someHost";
    private static final String SOME_INFO = "some info";

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @Autowired
    private SgeHealthCheckProvider sgeHealthCheckProvider;

    @ParameterizedTest
    @MethodSource("provideCorrectStdOutAndExpectedHealthCheckInfo")
    public void shouldReturnCorrectHealthCheckInfo(
            final long code,
            final GridEngineStatus gridEngineStatus,
            final String info,
            final LocalDateTime startTime,
            final LocalDateTime checkTime,
            final List<String> stdOut
    ) {
        final HealthCheckInfo expectedHealthCheckInfo = HealthCheckInfo.builder()
                .statusInfo(StatusInfo.builder()
                        .code(code)
                        .status(gridEngineStatus)
                        .info(info)
                        .build())
                .startTime(startTime)
                .checkTime(checkTime)
                .build();

        final CommandResult defineQmasterCommandResult = new CommandResult();
        defineQmasterCommandResult.setStdOut(Collections.singletonList(SOME_HOST));

        final CommandResult qpingCommandResult = new CommandResult();
        qpingCommandResult.setStdOut(stdOut);
        qpingCommandResult.setStdErr(EMPTY_LIST);

        doReturn(defineQmasterCommandResult).when(mockCmdExecutor).execute(Mockito.any());
        doReturn(qpingCommandResult).when(mockCmdExecutor).execute(Mockito.any());

        final HealthCheckInfo result = sgeHealthCheckProvider.checkHealth();
        assertEquals(expectedHealthCheckInfo, result);
    }

    static Stream<Arguments> provideCorrectStdOutAndExpectedHealthCheckInfo() {
        return Stream.of(
                Arguments.of(0L, GridEngineStatus.OK, SOME_INFO,
                        LocalDateTime.of(1992, 12, 18, 4, 0, 0),
                        LocalDateTime.of(2021, 9, 7, 19, 24, 0),
                        List.of("09/07/2021 19:24:00:", "start time: 12/18/1992 04:00:00 (724651200)",
                                "status: 0", "info:   " + SOME_INFO)),

                Arguments.of(99999L, GridEngineStatus.NOT_PROVIDED, SOME_INFO,
                        LocalDateTime.of(1900, 1, 1, 23, 59, 59),
                        LocalDateTime.of(2221, 12, 12, 0, 0, 0),
                        List.of("12/12/2221 00:00:00:", "start time: 01/01/1900 23:59:59 (724651200)",
                                "status: 99999", "info:" + SOME_INFO)),

                Arguments.of(2L, GridEngineStatus.ERROR, EMPTY_STRING,
                        LocalDateTime.of(1999, 2, 28, 10, 30, 30),
                        LocalDateTime.of(2001, 1, 2, 1, 2, 3),
                        List.of("01/02/2001 01:02:03:", "start time: 02/28/1999 10:30:30 (1)", "status: 2", "info:")));
    }
}
