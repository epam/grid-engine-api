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

package com.epam.grid.engine.provider.utils.sge.healthcheck;

import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;

public class QpingCommandParserTest {
    private static final String SOME_OUT = "some out";
    private static final String SOME_ERROR = "some error";
    private static final String CANT_FIND_CONNECTION = "can't find connection via specified port "
            + "or id";
    private static final String SOME_INFO = "some info";
    private static final List<String> NOT_EMPTY_STD_OUT = Collections.singletonList(SOME_OUT);
    private static final List<String> NOT_EMPTY_STD_ERR = Collections.singletonList(SOME_ERROR);

    @ParameterizedTest
    @MethodSource("provideBadQpingResults")
    public void shouldThrowExceptionDuringParsingResult(
            final int exitCode,
            final List<String> stdOut,
            final List<String> stdErr
    ) {
        final CommandResult commandResult = CommandResult.builder()
                .exitCode(exitCode)
                .stdOut(stdOut)
                .stdErr(stdErr)
                .build();
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class,
                () -> QpingCommandParser.parseQpingResult(commandResult));
        Assertions.assertNotNull(thrown.getMessage());
    }

    static Stream<Arguments> provideBadQpingResults() {
        return Stream.of(
                Arguments.of(1, NOT_EMPTY_STD_OUT, EMPTY_LIST),
                Arguments.of(0, EMPTY_LIST, EMPTY_LIST),
                Arguments.of(0, NOT_EMPTY_STD_OUT, NOT_EMPTY_STD_ERR),
                Arguments.of(0, null, NOT_EMPTY_STD_ERR),
                Arguments.of(0, NOT_EMPTY_STD_OUT, null),
                Arguments.of(1, Collections.singletonList(CANT_FIND_CONNECTION),
                        NOT_EMPTY_STD_ERR),
                Arguments.of(0, NOT_EMPTY_STD_OUT, EMPTY_LIST),
                Arguments.of(0, List.of("09/07/2021 19:24:00:", "start time: 12/18/1992 04:00:00 (724651200)",
                        "status: 7", "info:   " + SOME_INFO), EMPTY_LIST),
                Arguments.of(0, List.of("09/07/2021 19:24:00:", "start time: 12/18/1992 04:00:00 (724651200)",
                        "status: 0", SOME_INFO), EMPTY_LIST),
                Arguments.of(0, List.of("09-07-2021 19:24:00:", "start time: 12/18/1992 04:00:00 (724651200)",
                        "status: 0", "info:   " + SOME_INFO), EMPTY_LIST),
                Arguments.of(0, List.of("09/07/2021 19:24:00:", "start time: 12-18-1992 04:00:00 (724651200)",
                        "status: 0", "info:   " + SOME_INFO), EMPTY_LIST),
                Arguments.of(0, List.of("09/07/2021 19:24:00:", "12/18/1992 04:00:00 (724651200)", "status: 0",
                        "info:   " + SOME_INFO), EMPTY_LIST),
                Arguments.of(0, List.of("15/07/2021 19:24:00:", "start time: 12/18/1992 04:00:00 (724651200)",
                        "status: 0", "info:   " + SOME_INFO), EMPTY_LIST),
                Arguments.of(0, List.of("11/07/2021 19:24:00:", "start time: 12/54/1992 04:00:00 (724651200)",
                        "status: 0", "info:   " + SOME_INFO), EMPTY_LIST)
        );
    }

    @Test
    public void shouldThrowExceptionDuringParsingHostName() {
        final CommandResult commandResult = CommandResult.builder()
                .exitCode(1)
                .stdOut(EMPTY_LIST)
                .stdErr(NOT_EMPTY_STD_ERR)
                .build();

        final Throwable thrown = Assertions.assertThrows(GridEngineException.class,
                () -> QpingCommandParser.parseQmasterHostName(commandResult));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideCorrectQpingResults")
    public void shouldNotThrowExceptionBecauseOfCorrectStdOut(final List<String> stdOut) {
        final CommandResult commandResult = CommandResult.builder()
                .exitCode(0)
                .stdOut(stdOut)
                .stdErr(EMPTY_LIST)
                .build();
        Assertions.assertDoesNotThrow(() -> QpingCommandParser.parseQpingResult(commandResult));
    }

    static Stream<Arguments> provideCorrectQpingResults() {
        return Stream.of(
                Arguments.of(List.of("09/07/2021 19:24:00:", "start time: 12/18/1992 04:00:00 (724651200)", "status: "
                        + "0", "info:   " + SOME_INFO)),
                Arguments.of(List.of("01/01/2001 00:00:00:", "start time: 12/31/2100 23:59:59 (724651200)", "status: 1",
                        "info:" + SOME_INFO)),
                Arguments.of(List.of("11/11/2011 00:00:00:", "start time: 12/31/2100 23:59:59 (724651200)", "status: 2",
                        "info:" + SOME_INFO)),
                Arguments.of(List.of("10/11/2012 13:10:10:", "start time: 08/08/2000 23:59:59 (724651200)", "status: 3",
                        "info:" + SOME_INFO)),
                Arguments.of(List.of("06/07/2011 12:00:00:", "start time: 01/28/2003 00:00:01 (1)", "status: 99999",
                        "info:" + SOME_INFO))
        );
    }
}
