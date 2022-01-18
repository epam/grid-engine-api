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

package com.epam.grid.engine.entity.host.sge;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlEnumValue;

/**
 * This enum contains enumeration values which match the values of the host object.
 * To get more information about load parameters see <a href="http://manpages.ubuntu.com/manpages/bionic/man5/load_parameters.5.html">
 * Grid Engine default load parameters</a>.
 *
 * @see com.epam.grid.engine.entity.host.Host
 */
@XmlEnum
@XmlAccessorType(XmlAccessType.FIELD)
public enum SgeHostProperty {

    /**
     * The operating system architecture.
     */
    @XmlEnumValue("arch_string")
    TYPE_OF_ARCHITECT,

    /**
     * The number of processors provided by the execution host.
     */
    @XmlEnumValue("num_proc")
    NUM_OF_PROCESSORS,

    /**
     * Number of sockets available on the reporting host.
     */
    @XmlEnumValue("m_socket")
    NUM_OF_SOCKET,

    /**
     * Number of cores reported for all sockets on a host.
     */
    @XmlEnumValue("m_core")
    NUM_OF_CORE,

    /**
     * Number of hardware threads reported for all cores on a host.
     */
    @XmlEnumValue("m_thread")
    NUM_OF_THREAD,

    /**
     * The medium time average OS run queue length.
     */
    @XmlEnumValue("load_avg")
    LOAD,

    /**
     * The total amount of memory.
     */
    @XmlEnumValue("mem_total")
    MEM_TOTAL,

    /**
     * The amount of memory used.
     */
    @XmlEnumValue("mem_used")
    MEM_USED,

    /**
     * The total amount of swap space
     * (memory in form of a partition or a file).
     */
    @XmlEnumValue("swap_total")
    TOTAL_SWAP_SPACE,

    /**
     * The amount of swap space used.
     */
    @XmlEnumValue("swap_used")
    USED_SWAP_SPACE
}
