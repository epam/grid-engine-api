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

package com.epam.grid.engine.provider.healthcheck;

import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.provider.CommandTypeAware;

/**
 * The interface that defines an API providing information about the state of the cluster.
 * This interface should be implemented by specific grid engine provider.
 * @see CommandTypeAware
 */
public interface HealthCheckProvider extends CommandTypeAware {

    /**
     * Describes state of the cluster.
     * Each realization under the hood reuses specific build-in engine
     * functionality and maps it on
     * @see HealthCheckInfo
     * @return {@link HealthCheckInfo}
     */
    HealthCheckInfo checkHealth();
}
