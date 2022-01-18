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

package com.epam.grid.engine.provider.utils;

import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.NumberParseUtils.convertHumanReadableMemoryToGbytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NumberParseUtilsTest {

    private static final String DASH = "-";

    @ParameterizedTest
    @MethodSource("provideValuesToLongAndDoubleParse")
    public void shouldParseLongAndDoubleValue(
            final Long expectedLong, final Double expectedDouble,
            final String valueToParse
    ) {
        assertEquals(expectedLong, NumberParseUtils.toLong(valueToParse));
        assertEquals(expectedDouble, NumberParseUtils.toDouble(valueToParse));
    }

    static Stream<Arguments> provideValuesToLongAndDoubleParse() {
        return Stream.of(
                Arguments.of(11000000000L, 11000000000.0, "11G"),
                Arguments.of(11000000L, 11000000.0, "11M"),
                Arguments.of(11000L, 11000.0, "11K"),
                Arguments.of(11L, 11.0, "11"),
                Arguments.of(11L, 11.0, "11.0"),
                Arguments.of(null, null, DASH)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidValuesToMemoryParsing")
    public void shouldReturnValidMemoryInGbytes(final double expectedMemoryValue, final String memoryString) {
        assertEquals(expectedMemoryValue, convertHumanReadableMemoryToGbytes(memoryString));
    }

    static Stream<Arguments> provideValidValuesToMemoryParsing() {
        return Stream.of(
                Arguments.of(1.5, "1.5GB"),
                Arguments.of(1.5e-3, "1.5MB"),
                Arguments.of(1.5e-6, "1.5KB"),
                Arguments.of(1.0e-9, "1B"),
                Arguments.of(1.5e-6, "1500B"),
                Arguments.of(1.5e-5, "15000B")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1.5K", "2x2", "someString"})
    public void shouldThrowExceptionWhenParseInvalidMemoryString(final String memoryString) {
        assertThrows(GridEngineException.class, () -> convertHumanReadableMemoryToGbytes(memoryString));
    }

    @ParameterizedTest
    @MethodSource("provideValuesToTruncate")
    public void shouldReturnTruncatedDoubleValue(final double expectedValue, final double value, final int precision) {
        assertEquals(expectedValue, NumberParseUtils.truncateDoubleValue(value, precision));
    }

    static Stream<Arguments> provideValuesToTruncate() {
        final double testValue1 = 1.11111;
        final double testValue2 = 1.55555;
        return Stream.of(
                Arguments.of(1.0, testValue1, 0),
                Arguments.of(1.110, testValue1, 2),
                Arguments.of(testValue1, testValue1, 5),
                Arguments.of(2.0, testValue2, 0),
                Arguments.of(1.60, testValue2, 1),
                Arguments.of(1.560, testValue2, 2),
                Arguments.of(2.0, 1.99999, 4)
        );
    }
}
