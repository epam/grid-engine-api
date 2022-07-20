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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class CommandArgUtilsTest {

    private static final String EMPTY_STRING = "";
    private static final String ONE_VARIABLE_NAME = "oneVariable";
    private static final String ONE_VALUE_WITH_SPACES = "a value with spaces";
    private static final String QUOTE_TEST_TOKEN = "some \"token \\\"with \\\\\"quotes";
    private static final String QUOTE_ESCAPED_TEST_TOKEN = "some \\\"token \\\\\"with \\\\\\\"quotes";

    @ParameterizedTest
    @MethodSource("provideCasesToParseCommand")
    public void shouldSplitCommandIntoTokenArgs(final String commandString, final String[] expectedCommand) {
        Assertions.assertArrayEquals(expectedCommand, CommandArgUtils.splitCommandIntoArgs(commandString));
    }

    static Stream<Arguments> provideCasesToParseCommand() {
        return Stream.of(
                Arguments.of("qhost -h current_host1 current_host2 current_host3 -xml",
                        new String[]{"qhost", "-h", "current_host1", "current_host2", "current_host3", "-xml"}),

                Arguments.of("qhost\n-h\n \n current_host\n \n\n-xml\n",
                        new String[]{"qhost", "-h", "current_host", "-xml"}),

                Arguments.of("qhost\r\n\r\n-h\r\n \r\n current_host\r\n \r\n\r\n-xml\r\n",
                        new String[]{"qhost", "-h", "current_host", "-xml"}),

                Arguments.of("\"\\\" \t some test argument \\\"\"",
                        new String[]{"\" \t some test argument \""}),

                Arguments.of("sbatch --export\n\n    \n    \n        \"ALL,additionalProp1=\\\"a value with spaces\\"
                                + "\",additionalProp2=\\\"a value with \\\"quotes and spaces\\\"\\\",additionalProp3"
                                + "=\\\"a value with \\\\\"an escaped quotes\\\\\"\\\"\"\n    \n\n\n\n\n    --job-na"
                                + "me=someName\n\n\n\n    --chdir=/data\n\n/data/test.py\n\n    \n    \"a value with"
                                + " spaces\"\n  \"a value with \\\"quotes and spaces\\\"\"\n   \"a value with \\\\\""
                                + "an escaped quotes\\\\\"\"\n    \n\n",

                        new String[]{"sbatch", "--export", "ALL,additionalProp1=\"a value with spaces\",additionalPr"
                                        + "op2=\"a value with \"quotes and spaces\"\",additionalProp3=\"a value with"
                                        + " \\\"an escaped quotes\\\"\"", "--job-name=someName", "--chdir=/data",
                                     "/data/test.py", "a value with spaces", "a value with \"quotes and spaces\"",
                                     "a value with \\\"an escaped quotes\\\""})
        );
    }

    @ParameterizedTest
    @MethodSource("provideEnvironmentVariablesCases")
    public void shouldReturnEnvironmentVariablesAsCommandArgument(final String expectedArgument,
                                                                  final Map<String, String> variables) {
        Assertions.assertEquals(expectedArgument, CommandArgUtils.envVariablesMapToString(variables));
    }

    static Stream<Arguments> provideEnvironmentVariablesCases() {
        return Stream.of(
                Arguments.of(EMPTY_STRING, Map.of(EMPTY_STRING, EMPTY_STRING)),
                Arguments.of(ONE_VARIABLE_NAME, Map.of(ONE_VARIABLE_NAME, EMPTY_STRING)),
                Arguments.of(String.format("%s=\\\"%s\\\"", ONE_VARIABLE_NAME, ONE_VALUE_WITH_SPACES),
                        Map.of(ONE_VARIABLE_NAME, ONE_VALUE_WITH_SPACES))
        );
    }

    @ParameterizedTest
    @MethodSource("provideCasesToEscapeQuotes")
    public void shouldReturnQuoteEscapedToken(final String expectedToken, final String token) {
        Assertions.assertEquals(expectedToken, CommandArgUtils.toEscapeQuotes(token));
    }

    static Stream<Arguments> provideCasesToEscapeQuotes() {
        return Stream.of(
                Arguments.of(EMPTY_STRING, EMPTY_STRING),
                Arguments.of(ONE_VALUE_WITH_SPACES, ONE_VALUE_WITH_SPACES),
                Arguments.of(QUOTE_ESCAPED_TEST_TOKEN, QUOTE_TEST_TOKEN)
        );
    }

    @Test
    public void shouldReturnQuoteEscapedTokenList() {
        Assertions.assertEquals(
                List.of(EMPTY_STRING, ONE_VALUE_WITH_SPACES, QUOTE_ESCAPED_TEST_TOKEN),
                CommandArgUtils.toEscapeQuotes(List.of(EMPTY_STRING, ONE_VALUE_WITH_SPACES, QUOTE_TEST_TOKEN))
        );
    }

    @Test
    public void shouldReturnEnclosedInQuotesToken() {
        Assertions.assertEquals(
                "\"someToken\"", CommandArgUtils.toEncloseInQuotes("someToken")
        );
    }
}
