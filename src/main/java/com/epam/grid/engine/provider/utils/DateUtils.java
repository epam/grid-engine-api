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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

    public static LocalDateTime tryParseStringToLocalDateTime(final String dateString,
                                                              final DateTimeFormatter formatter) {
        try {
            return LocalDateTime.parse(dateString, formatter);
        } catch (final DateTimeParseException dateTimeParseException) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during date parsing",
                    dateTimeParseException);
        }
    }

    public static LocalDateTime tryParseStringToLocalDateTime(final String dateString,
                                                              final String timePattern) {
        return tryParseStringToLocalDateTime(dateString, DateTimeFormatter.ofPattern(timePattern));

    }
}
