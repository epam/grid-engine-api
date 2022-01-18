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

package com.epam.grid.engine.entity.host;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class provides an object that contains full description of the host state.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Host {

    /**
     * The name of host.
     */
    private String hostname;

    /**
     * The operating system architecture.
     */
    private String typeOfArchitect;

    /**
     * The number of processors provided by the execution host.
     */
    private Integer numOfProcessors;

    /**
     * Number of sockets available on the reporting host.
     */
    private Integer numOfSocket;

    /**
     * Number of cores reported for all sockets on a host.
     */
    private Integer numOfCore;

    /**
     * Number of hardware threads reported for all cores on a host.
     */
    private Integer numOfThread;

    /**
     * The medium time average OS run queue length.
     */
    private Double load;

    /**
     * The total amount of memory.
     */
    private Long memTotal;

    /**
     * The amount of memory used.
     */
    private Long memUsed;

    /**
     * The total amount of swap space
     * (memory in form of a partition or a file).
     */
    private Double totalSwapSpace;

    /**
     * The amount of swap space used.
     */
    private Double usedSwapSpace;
}
