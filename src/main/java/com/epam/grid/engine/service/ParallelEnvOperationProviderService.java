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

import com.epam.grid.engine.entity.ParallelEnvFilter;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.PeRegistrationVO;
import com.epam.grid.engine.provider.parallelenv.ParallelEnvProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class determines which of the grid engines shall be used and calls appropriate methods.
 */
@Service
@RequiredArgsConstructor
public class ParallelEnvOperationProviderService {

    private final ParallelEnvProvider parallelEnvProvider;

    /**
     * This method processes the request to provider and returns listing of pe according to filter.
     *
     * @param parallelEnvFilter names of PE needed
     * @return List of {@link ParallelEnv}
     * @see ParallelEnvFilter
     */
    public List<ParallelEnv> filterParallelEnvs(final ParallelEnvFilter parallelEnvFilter) {
        return parallelEnvProvider.listParallelEnv(parallelEnvFilter);
    }

    /**
     * This method processes the request to provider and returns deleted PE.
     *
     * @param parallelEnvName name of PE to be deleted
     * @return {@link ParallelEnv} that was deleted
     */
    public ParallelEnv deleteParallelEnv(final String parallelEnvName) {
        return parallelEnvProvider.deleteParallelEnv(parallelEnvName);
    }

    /**
     * This method processes the request to provider and returns PE object required.
     *
     * @param peName name of PE to retrieve
     * @return object of {@link ParallelEnv}
     */
    public ParallelEnv getParallelEnv(final String peName) {
        return parallelEnvProvider.getParallelEnv(peName);
    }

    /**
     * Registers a {@link ParallelEnv} with specified properties in preassigned grid engine system.
     *
     * @param registrationRequest the properties of the parallel environment to be registered
     * @return the registered {@link ParallelEnv}
     */
    public ParallelEnv registerParallelEnv(final PeRegistrationVO registrationRequest) {
        return parallelEnvProvider.registerParallelEnv(registrationRequest);
    }

}
