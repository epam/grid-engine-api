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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representation of information about status of grid engine cluster.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HealthCheckInfo {
    /**
     * Information about grid engine status.
     * @see StatusInfo
     */
    private StatusInfo statusInfo;
    /**
     * Start time of the grid engine.
     */
    private LocalDateTime startTime;
    /**
     * Status request time.
     */
    private LocalDateTime checkTime;
}
