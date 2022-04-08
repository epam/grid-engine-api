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

import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.provider.host.HostProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class determines which of the grid engines shall be used and calls appropriate methods.
 */
@Service
public class HostOperationProviderService {

    private final HostProvider hostProvider;

    /**
     * Constructor, sets created hostProvider bean to the class field.
     *
     * @param hostProvider created HostGroupProvider
     * @see HostProvider
     */
    @Autowired
    public HostOperationProviderService(final HostProvider hostProvider) {
        this.hostProvider = hostProvider;
    }

    /**
     * This method processes the request to provider and returns listing of hosts,
     * if request is empty it will return all the hosts.
     *
     * @param filter names of hosts needed
     * @return {@link Listing} of {@link Host}
     * @see HostFilter
     */
    public Listing<Host> filter(final HostFilter filter) {
        return hostProvider.listHosts(filter);
    }

}
