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

import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.ParallelEnvFilter;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.PeRegistrationVO;
import com.epam.grid.engine.provider.parallelenv.ParallelEnvProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class determines which of the grid engines shall be used and calls appropriate methods.
 */
@Service
public class ParallelEnvOperationProviderService {

    private final EngineType engineType;

    private Map<EngineType, ParallelEnvProvider> providers;

    /**
     * This method sets grid engine type in the context.
     *
     * @param engineType type of grid engine
     * @see EngineType
     */
    public ParallelEnvOperationProviderService(@Value("${grid.engine.type}") final EngineType engineType) {
        this.engineType = engineType;
    }

    /**
     * This method processes the request to provider and returns listing of pe according to filter.
     *
     * @param parallelEnvFilter names of PE needed
     * @return List of {@link ParallelEnv}
     * @see ParallelEnvFilter
     */
    public List<ParallelEnv> filterParallelEnvs(final ParallelEnvFilter parallelEnvFilter) {
        return getConfigProvider().listParallelEnv(parallelEnvFilter);
    }

    /**
     * This method processes the request to provider and returns deleted PE.
     * @param parallelEnvName name of PE to be deleted
     * @return {@link ParallelEnv} that was deleted
     */
    public ParallelEnv deleteParallelEnv(final String parallelEnvName) {
        return getConfigProvider().deleteParallelEnv(parallelEnvName);
    }

    /**
     * This method processes the request to provider and returns PE object required.
     *
     * @param peName name of PE to retrieve
     * @return object of {@link ParallelEnv}
     */
    public ParallelEnv getParallelEnv(final String peName) {
        return getConfigProvider().getParallelEnv(peName);
    }

    /**
     * Registers a {@link ParallelEnv} with specified properties in preassigned grid engine system.
     *
     * @param registrationRequest the properties of the parallel environment to be registered
     * @return the registered {@link ParallelEnv}
     */
    public ParallelEnv registerParallelEnv(final PeRegistrationVO registrationRequest) {
        return getConfigProvider().registerParallelEnv(registrationRequest);
    }

    /**
     * Injects all available {@link ParallelEnvProvider} implementations.
     *
     * @param providers list of ParallelEnvProvider.
     * @see ParallelEnvProvider
     */
    @Autowired
    public void setProviders(final List<ParallelEnvProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(ParallelEnvProvider::getProviderType, Function.identity()));
    }

    private ParallelEnvProvider getConfigProvider() {
        final ParallelEnvProvider parallelEnv = providers.get(engineType);
        Assert.notNull(parallelEnv, String.format("Provides for type '%s' is not supported", engineType));
        return parallelEnv;
    }
}
