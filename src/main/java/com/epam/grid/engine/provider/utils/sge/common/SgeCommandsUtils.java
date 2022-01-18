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

package com.epam.grid.engine.provider.utils.sge.common;

import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.exception.GridEngineException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.epam.grid.engine.utils.TextConstants.NEW_LINE_DELIMITER;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SgeCommandsUtils {

    private static final String MUST_BE_MANAGER = "must be manager for this operation";

    public static void throwExecutionDetails(final CommandResult result, final HttpStatus httpStatus) {
        final String message = result.getStdOut().isEmpty() ? mergeOutputLines(result.getStdErr()) :
                mergeOutputLines(result.getStdOut()) + NEW_LINE_DELIMITER + mergeOutputLines(result.getStdErr());
        throw new GridEngineException(httpStatus, message);
    }

    public static void throwExecutionDetails(final CommandResult result) {
        throwExecutionDetails(result, HttpStatus.NOT_FOUND);
    }

    public static String mergeOutputLines(final List<String> stdOut) {
        return String.join(NEW_LINE_DELIMITER, stdOut);
    }

    public static HttpStatus determineStatus(final List<String> result) {
        final boolean requiredRoleMissing = result.stream()
                .anyMatch(stdErr -> stdErr.endsWith(MUST_BE_MANAGER));

        return requiredRoleMissing
                ? HttpStatus.FORBIDDEN
                : HttpStatus.BAD_REQUEST;
    }
}
