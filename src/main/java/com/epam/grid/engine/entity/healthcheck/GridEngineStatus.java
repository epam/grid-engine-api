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

package com.epam.grid.engine.entity.healthcheck;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Representation of possible statuses of working
 * grid engine as enumeration.
 */
@RequiredArgsConstructor
public enum GridEngineStatus {
    /**
     * Grid engine is ok.
     */
    OK(0L),
    /**
     * One or more grid engine threads  has  reached  warning  timeout.
     */
    WARNING(1L),
    /**
     * One or more grid engine threads has reached error timeout.
     */
    ERROR(2L),
    /**
     * The grid engine`s time measurement is not initialized.
     */
    NOT_INITIALIZED(3L),
    /**
     * Information about grid engine status not provided.
     */
    NOT_PROVIDED(99999L);

    private final long id;

    /**
     * This method provides the status corresponding to the received id.
     * @param id status of grid engine in long format
     * @return {@link GridEngineStatus}
     */
    public static Optional<GridEngineStatus> getById(final long id) {
        return Arrays.stream(values())
                .filter(status -> status.id == id)
                .findFirst();
    }
}
