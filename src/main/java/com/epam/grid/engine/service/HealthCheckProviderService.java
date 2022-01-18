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
import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.provider.healthcheck.HealthCheckProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provider determines which of the grid engines shall be used
 * and calls appropriate methods.
 */
@Service
public class HealthCheckProviderService {

    private final EngineType engineType;

    private Map<EngineType, HealthCheckProvider> providers;

    /**
     * This method sets grid engine type in the context.
     * @param engineType type of grid engine
     * @see EngineType
     */
    public HealthCheckProviderService(@Value("${grid.engine.type}") final EngineType engineType) {
        this.engineType = engineType;
    }

    /**
     * This method passes the request on to {@link HealthCheckProvider}
     * and returns working grid engine status information.
     * @return {@link HealthCheckInfo}
     */
    public HealthCheckInfo checkHealth() {
        return getProvider().checkHealth();
    }

    /**
     * This method injects all created {@link HealthCheckProvider} beans.
     * @param providers list of existing HealthCheckProviders
     * @see HealthCheckProvider
     */
    @Autowired
    public void setProviders(final List<HealthCheckProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(HealthCheckProvider::getProviderType, Function.identity()));
    }

    private HealthCheckProvider getProvider() {
        final HealthCheckProvider healthCheckProvider = providers.get(engineType);
        Assert.notNull(healthCheckProvider, String.format("Provides for type '%s' is not "
                + "supported", engineType));
        return healthCheckProvider;
    }
}
