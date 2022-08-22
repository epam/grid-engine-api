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

package com.epam.grid.engine.service;

import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.provider.healthcheck.HealthCheckProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provider determines which of the grid engines shall be used
 * and calls appropriate methods.
 */
@Service
@RequiredArgsConstructor
public class HealthCheckProviderService {

    private final HealthCheckProvider healthCheckProvider;

    /**
     * This method passes the request on to {@link HealthCheckProvider}
     * and returns working grid engine status information.
     *
     * @return {@link HealthCheckInfo}
     */
    public HealthCheckInfo checkHealth() {
        return healthCheckProvider.checkHealth();
    }

}
