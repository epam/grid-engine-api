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

package com.epam.grid.engine.provider.parallelenv;

import com.epam.grid.engine.entity.ParallelEnvFilter;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.PeRegistrationVO;
import com.epam.grid.engine.provider.GridProviderAware;

import java.util.List;

/**
 * This is an interface that provides requirements to different grid engine PE providers.
 *
 * @see GridProviderAware
 */
public interface ParallelEnvProvider extends GridProviderAware {

    /**
     * Lists all available PE considering filtering parameters.
     *
     * @param parallelEnvFilter PE names
     * @return List of {@link ParallelEnv}
     * @see ParallelEnvFilter
     */
    List<ParallelEnv> listParallelEnv(ParallelEnvFilter parallelEnvFilter);

    /**
     * Provides PE object that was requested.
     *
     * @param peName PE name
     * @return List of {@link ParallelEnv}
     */
    ParallelEnv getParallelEnv(String peName);

    /**
     * Deletes specified parallel environment.
     * @param parallelEnvName the name of the deleting parallel environment
     * @return {@link ParallelEnv} which was deleted
     */
    ParallelEnv deleteParallelEnv(String parallelEnvName);

    /**
     * Registers a {@link ParallelEnv} with specified properties in grid engine system.
     *
     * @param registrationRequest the properties of the parallel environment to be registered
     * @return the registered {@link ParallelEnv}
     */
    ParallelEnv registerParallelEnv(PeRegistrationVO registrationRequest);
}
