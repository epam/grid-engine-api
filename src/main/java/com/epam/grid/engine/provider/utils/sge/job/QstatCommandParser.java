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

package com.epam.grid.engine.provider.utils.sge.job;

import com.epam.grid.engine.exception.GridEngineException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static com.epam.grid.engine.utils.TextConstants.NEW_LINE_DELIMITER;
import static java.util.Map.entry;

import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QstatCommandParser {

    private static final Map<String, String> JOB_STATE_MAPPING =
            Map.ofEntries(
                    entry("pending", "p"),
                    entry("running", "r"),
                    entry("suspended", "s"),
                    entry("zombie", "z"),
                    entry("hu", "hu"),
                    entry("ho", "ho"),
                    entry("hs", "hs"),
                    entry("hd", "hd"),
                    entry("ha", "ha"),
                    entry("h", "h"),
                    entry("a", "a")
            );
    private static final String NOT_CORRECT_STATE_MESSAGE = String.format("Specified state not correct! Please use "
            + "one of the follow states: %s", String.join(NEW_LINE_DELIMITER, JOB_STATE_MAPPING.keySet()));

    public static String getStateFromStateMap(final String key) {
        return Optional.ofNullable(JOB_STATE_MAPPING.get(key)).orElseThrow(() -> {
            throw new GridEngineException(HttpStatus.NOT_FOUND, NOT_CORRECT_STATE_MESSAGE);
        });
    }
}
