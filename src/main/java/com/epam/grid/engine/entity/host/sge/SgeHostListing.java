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

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This class maps list of sun grid engine hosts when command qhost was used.
 * To get more information see <a href="http://gridscheduler.sourceforge.net/htmlman/htmlman1/qhost.html">
 *  *     qhost command full info</a>
 *
 * @see com.epam.grid.engine.entity.host.Host
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "qhost")
@XmlAccessorType(XmlAccessType.FIELD)
public class SgeHostListing {

    /**
     * Listing of SgeHost values.
     *
     * @see SgeHost
     */
    @XmlElement(name = "host")
    public List<SgeHost> sgeHost;
}
