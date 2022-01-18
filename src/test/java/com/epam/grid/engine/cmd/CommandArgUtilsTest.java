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

package com.epam.grid.engine.cmd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class CommandArgUtilsTest {

    @ParameterizedTest
    @MethodSource("provideQhostStringCommandAndExpectedCommand")
    public void shouldMakeRightQhostCommand(final String commandString, final String[] expectedCommand) {
        Assertions.assertArrayEquals(expectedCommand, CommandArgUtils.splitCommandIntoArgs(commandString));
    }

    static Stream<Arguments> provideQhostStringCommandAndExpectedCommand() {
        return Stream.of(
                Arguments.of("qhost -h \"current_host1\" \"current_host2\" \"current_host3\" -xml",
                        new String[]{"qhost", "-h", "\"current_host1\" \"current_host2\" \"current_host3\"", "-xml"}),
                Arguments.of("qhost -h \"current host\",\"current_host\" -xml",
                        new String[]{"qhost", "-h", "\"current host\",\"current_host\"", "-xml"}),
                Arguments.of("qhost -h current host current_host -xml",
                        new String[]{"qhost", "-h", "current", "host", "current_host", "-xml"}),
                Arguments.of("qhost -h \"current_host1\" \"current_host2\" -xml",
                        new String[]{"qhost", "-h", "\"current_host1\" \"current_host2\"", "-xml"}),
                Arguments.of("\"current host\"",
                        new String[]{"\"current host\""}),
                Arguments.of("qhost \\ -xml",
                        new String[]{"qhost", "-xml"}),
                Arguments.of("qhost -xml",
                        new String[]{"qhost", "-xml"}),
                Arguments.of("qhost\r\n\r\n-h\r\n \r\n current_host\r\n \r\n\r\n-xml\r\n",
                        new String[]{"qhost", "-h", "current_host", "-xml"}),
                Arguments.of("\"test current host\"",
                        new String[]{"\"test current host\""})
        );
    }
}
