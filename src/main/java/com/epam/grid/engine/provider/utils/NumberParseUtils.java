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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberParseUtils {

    private static final int THOUSAND = 1000;
    private static final String NUMERIC_STRING_REGEX = "\\d+";
    private static final String HUMAN_READABLE_NUMERIC_STRING_REGEX = "(\\d+\\.?\\d*)(([KMG])?)";
    private static final String HUMAN_READABLE_MEMORY_STRING_REGEX = "(\\d+\\.?\\d*)(([KMG])?B)";
    private static final String EMPTY_VALUE = "-";

    public static Integer toInt(final String value) {
        final String trimValue = value.trim();

        if (trimValue.equals(EMPTY_VALUE)) {
            return null;
        }

        if (trimValue.matches(NUMERIC_STRING_REGEX)) {
            return Integer.parseInt(trimValue);
        }

        throw new GridEngineException(HttpStatus.NOT_FOUND,
                String.format("Cannot be converted to Integer: %s", value));
    }

    public static Double toDouble(final String value) {
        if (value.trim().equals(EMPTY_VALUE)) {
            return null;
        }
        return convertHumanReadableStringToDigits(value);
    }

    public static Long toLong(final String value) {
        if (value.trim().equals(EMPTY_VALUE)) {
            return null;
        }
        return (long) convertHumanReadableStringToDigits(value);
    }

    public static String trimEmptyToNull(final String value) {
        if (value.trim().equals(EMPTY_VALUE)) {
            return null;
        }
        return value;
    }

    public static boolean isNumber(final String state) {
        try {
            Double.parseDouble(state);
            return true;
        } catch (final NumberFormatException exception) {
            return false;
        }
    }

    public static double truncateDoubleValue(final double value, final int precision) {
        return new BigDecimal(Double.toString(value))
                .setScale(precision, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static double convertHumanReadableMemoryToGbytes(final String memoryString) {
        final Pattern pattern = Pattern.compile(HUMAN_READABLE_MEMORY_STRING_REGEX);
        final Matcher match = pattern.matcher(memoryString);

        if (!match.matches() || match.groupCount() != 3) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Something went wrong while parsing memory string value: %s", memoryString));
        }
        final int factor = "GMKB".indexOf(match.group(2).charAt(0));
        return Double.parseDouble(match.group(1)) / Math.pow(THOUSAND, factor);
    }

    private static double convertHumanReadableStringToDigits(final String value) {
        final Pattern pattern = Pattern.compile(HUMAN_READABLE_NUMERIC_STRING_REGEX);
        final Matcher match = pattern.matcher(value.trim());

        if (!match.matches() || match.groupCount() != 3) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Cannot parse value: %s", value));
        }
        final int factor = match.group(2).isEmpty() ? 0 : "KMG".indexOf(match.group(2).charAt(0)) + 1;
        return Double.parseDouble(match.group(1)) * Math.pow(THOUSAND, factor);
    }
}
