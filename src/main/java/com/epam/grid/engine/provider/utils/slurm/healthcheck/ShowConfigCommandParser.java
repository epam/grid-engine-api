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

package com.epam.grid.engine.provider.utils.slurm.healthcheck;

import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.healthcheck.GridEngineStatus;
import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.entity.healthcheck.StatusInfo;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.utils.slurm.DateUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.epam.grid.engine.utils.TextConstants.SPACE;
import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;
import static com.epam.grid.engine.utils.TextConstants.EQUAL_SIGN;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShowConfigCommandParser {

    private static final String UP_STATE = "UP";
    private static final String DOWN_STATE = "DOWN";
    private static final String SLURM_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String BOOT_TIME = "BOOT_TIME";
    private static final String CONFIGURATION_STRING = "Configuration data as of";
    private static final String SPACES_REGEX = "\\s+";
    private static final Long NOT_PROVIDED = 99999L;

    public static HealthCheckInfo parseShowConfigResult(final CommandResult commandResult) {
        validateShowConfigResponse(commandResult);
        final List<String> stdOut = commandResult.getStdOut();
        final StatusInfo statusInfo = parseStatusInfo(stdOut);
        return HealthCheckInfo.builder()
                .statusInfo(statusInfo)
                .startTime(getStartTime(stdOut))
                .checkTime(getCheckTime(stdOut))
                .build();
    }

    private static void validateShowConfigResponse(final CommandResult commandResult) {
        if (CollectionUtils.isEmpty(commandResult.getStdOut())) {
            throw new GridEngineException(HttpStatus.NOT_FOUND,
                    String.format("Slurm error during health check. %nexitCode = %d %nstdOut: %s %nstdErr: %s",
                            commandResult.getExitCode(), commandResult.getStdOut(), commandResult.getStdErr())
            );
        }
        if (checkUnexpectedError(commandResult)) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Unexcpected error occurred during health check. "
                                    + "%nexitCode = %d %nstdOut: %s %nstdErr: %s",
                            commandResult.getExitCode(), commandResult.getStdOut(), commandResult.getStdErr())
            );
        }
    }

    private static boolean checkUnexpectedError(final CommandResult commandResult) {
        return commandResult.getExitCode() != 0 && !commandResult.getStdErr().isEmpty();
    }

    private static StatusInfo parseStatusInfo(final List<String> stdOut) {
        final long statusCode = parseStatusCode(stdOut);
        return StatusInfo.builder()
                .code(statusCode)
                .status(getStatus(statusCode))
                .info(parseInfo(stdOut))
                .build();
    }

    private static long parseStatusCode(final List<String> stdOut) {
        final List<String> statusString = List.of(stdOut.get(stdOut.size() - 1)
                .replaceAll(SPACES_REGEX, SPACE)
                .split(SPACE));
        final String status = statusString.get(statusString.size() - 1);
        switch (status) {
            case UP_STATE:
                return 0L;
            case DOWN_STATE:
                return 2L;
            default:
                return NOT_PROVIDED;
        }
    }

    private static GridEngineStatus getStatus(final long status) {
        return GridEngineStatus.getById(status)
                .orElseThrow(() -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Not valid status value provided: " + status));
    }

    private static String parseInfo(final List<String> stdOut) {
        return stdOut.get(stdOut.size() - 1).trim();
    }

    private static LocalDateTime getCheckTime(final List<String> stdOut) {
        return stdOut.stream().filter(out -> out.contains(CONFIGURATION_STRING)).findAny()
                .map(start -> start.replaceAll(SPACES_REGEX, SPACE))
                .map(start -> start.replace(CONFIGURATION_STRING, EMPTY_STRING))
                .map(String::trim)
                .map(start -> DateUtils.tryParseStringToLocalDateTime(start,
                        DateTimeFormatter.ofPattern(SLURM_DATE_FORMAT)))
                .orElseThrow(() -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "check time wasn't found in stdOut, stdOut: " + stdOut));
    }

    private static LocalDateTime getStartTime(final List<String> stdOut) {
        return stdOut.stream().filter(out -> out.contains(BOOT_TIME)).findAny()
                .map(bootTimeStr -> bootTimeStr.split(EQUAL_SIGN))
                .map(splitStr -> splitStr.length == 2 ? splitStr[1].trim() : "")
                .map(start -> DateUtils.tryParseStringToLocalDateTime(start,
                        DateTimeFormatter.ofPattern(SLURM_DATE_FORMAT)))
                .orElseThrow(() -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "boot time wasn't found in stdOut, stdOut: " + stdOut));
    }


}
