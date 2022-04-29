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
import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.entity.healthcheck.StatusInfo;
import com.epam.grid.engine.entity.healthcheck.GridEngineStatus;
import com.epam.grid.engine.exception.GridEngineException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;
import static com.epam.grid.engine.utils.TextConstants.SPACE;

import static com.epam.grid.engine.provider.utils.CommandsUtils.mergeOutputLines;

/**
 * Parser containing static methods for interacting
 * with SGE using qping command for check SGE status
 * and parsing the responses received from SGE.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QpingCommandParser {

    private static final String NOT_PROVIDED_STATUS_PREFIX = "9";
    private static final String STATUS = "status";
    private static final String SGE_INFO = "info:";
    private static final String START_TIME = "start time: ";
    private static final String INSIDE_BRACKETS_REGEX = "\\(.*\\)";
    private static final String CANT_FIND_CONNECTION = "can't find connection";
    private static final String CANT_FIND_CONNECTION_MESSAGE = "Can`t find connection via specified port";
    private static final String DATE_FORMAT_CHECK_TIME = "MM/dd/yyyy HH:mm:ss:";
    private static final String DATE_FORMAT_START_TIME = "MM/dd/yyyy HH:mm:ss";
    private static final String DOUBLED_SPACES_REGEX = "\\s+";
    private static final Long NOT_PROVIDED = 99999L;

    /**
     * This method parses result of execution CAT command.
     * If the file is not found method throws {@link GridEngineException}.
     *
     * @param result {@link CommandResult} result of execution CAT command
     * @return name of qmaster host
     */
    public static String parseQmasterHostName(final CommandResult result) {
        if (result.getExitCode() != 0) {
            throw new GridEngineException(HttpStatus.NOT_FOUND,
                    "Can`t find file contains qmaster host name via specified path");
        }
        return mergeOutputLines(result.getStdOut());
    }

    /**
     * This method parses result of execution qping command to {@link HealthCheckInfo}.
     *
     * @param commandResult {@link CommandResult} result of execution qping command
     * @return {@link HealthCheckInfo}
     */
    public static HealthCheckInfo parseQpingResult(final CommandResult commandResult) {
        validateQpingResponse(commandResult);
        final List<String> stdOut = commandResult.getStdOut();
        final StatusInfo statusInfo = parseStatusInfo(stdOut);
        return HealthCheckInfo.builder()
                .statusInfo(statusInfo)
                .checkTime(parseCheckTime(stdOut))
                .startTime(parseStartTime(stdOut))
                .build();
    }

    private static void validateQpingResponse(final CommandResult commandResult) {
        if (CollectionUtils.isEmpty(commandResult.getStdOut())) {
            throw new GridEngineException(HttpStatus.NOT_FOUND,
                    String.format("SGE error during health check. %nexitCode = %d %nstdOut: %s %nstdErr: %s",
                            commandResult.getExitCode(), commandResult.getStdOut(), commandResult.getStdErr())
            );
        }
        if (checkCantFindConnectionCase(commandResult)) {
            throw new GridEngineException(HttpStatus.NOT_FOUND, CANT_FIND_CONNECTION_MESSAGE);
        }
    }

    private static boolean checkCantFindConnectionCase(final CommandResult commandResult) {
        return commandResult.getExitCode() != 0
                && commandResult.getStdOut().get(0).contains(CANT_FIND_CONNECTION)
                && !commandResult.getStdErr().isEmpty();
    }

    private static StatusInfo parseStatusInfo(final List<String> stdOut) {
        final long statusCode = parseStatusCode(stdOut);
        return StatusInfo.builder()
                .code(statusCode)
                .status(parseStatus(statusCode))
                .info(parseInfo(stdOut))
                .build();
    }

    private static long parseStatusCode(final List<String> stdOut) {
        return stdOut.stream().filter(out -> out.contains(STATUS)).findAny()
                .map(statusString -> statusString.substring(statusString.length() - 1))
                .map(statusString -> NOT_PROVIDED_STATUS_PREFIX.equals(statusString)
                        ? NOT_PROVIDED : Long.parseLong(statusString))
                .orElseThrow(() -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "stdOut does not contains status information, stdOt: " + stdOut));
    }

    private static GridEngineStatus parseStatus(final long status) {
        return GridEngineStatus.getById(status)
                .orElseThrow(() -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Not valid status value provided: " + status));
    }

    private static String parseInfo(final List<String> stdOut) {
        return stdOut.stream().filter(out -> out.contains(SGE_INFO)).findAny()
                .map(infoString -> infoString.replaceAll(DOUBLED_SPACES_REGEX, SPACE))
                .map(infoString -> infoString.replace(SGE_INFO, EMPTY_STRING))
                .map(String::trim)
                .orElseThrow(() -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "stdOut does not contains info, stdOut: " + stdOut));
    }

    private static LocalDateTime parseCheckTime(final List<String> stdOut) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_CHECK_TIME);
        return tryParseStringToLocalDateTime(stdOut.get(0), formatter);
    }

    private static LocalDateTime parseStartTime(final List<String> stdOut) {
        return stdOut.stream().filter(out -> out.contains(START_TIME)).findAny()
                .map(start -> start.replaceAll(DOUBLED_SPACES_REGEX, SPACE))
                .map(start -> start.replace(START_TIME, EMPTY_STRING))
                .map(start -> start.replaceAll(INSIDE_BRACKETS_REGEX, EMPTY_STRING))
                .map(String::trim)
                .map(start -> tryParseStringToLocalDateTime(start, DateTimeFormatter.ofPattern(DATE_FORMAT_START_TIME)))
                .orElseThrow(() -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "stdOut does not contains start time, stdOut: " + stdOut));
    }

    private static LocalDateTime tryParseStringToLocalDateTime(
            final String dateString,
            final DateTimeFormatter formatter
    ) {
        try {
            return LocalDateTime.parse(dateString, formatter);
        } catch (final DateTimeParseException dateTimeParseException) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during date parsing",
                    dateTimeParseException);
        }
    }
}
