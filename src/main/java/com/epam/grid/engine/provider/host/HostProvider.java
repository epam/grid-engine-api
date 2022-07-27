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

package com.epam.grid.engine.provider.host;

import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.provider.CommandTypeAware;

/**
 * This is an interface that provides requirements to different grid engine host providers.
 *
 * @see CommandTypeAware
 */
public interface HostProvider extends CommandTypeAware {

    /**
     * Lists all available hosts considering filtering parameters.
     *
     * @param hostNames host names
     * @return {@link Listing} of {@link Host}
     * @see HostFilter
     */
    Listing<Host> listHosts(HostFilter hostNames);
}
