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

package com.epam.grid.engine.mapper.host.sge;

import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.entity.host.sge.SgeHost;
import com.epam.grid.engine.entity.host.sge.SgeHostValue;
import com.epam.grid.engine.provider.utils.NumberParseUtils;
import com.epam.grid.engine.utils.TextConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SgeHostMapper {

    @Mapping(target = "typeOfArchitect",
            expression = "java(trimEmptyToNull(attributes.get(\"TYPE_OF_ARCHITECT\")))")
    @Mapping(target = "numOfProcessors",
            expression = "java(toInt(attributes.get(\"NUM_OF_PROCESSORS\")))")
    @Mapping(target = "numOfSocket", expression = "java(toInt(attributes.get(\"NUM_OF_SOCKET\")))")
    @Mapping(target = "numOfCore", expression = "java(toInt(attributes.get(\"NUM_OF_CORE\")))")
    @Mapping(target = "numOfThread", expression = "java(toInt(attributes.get(\"NUM_OF_THREAD\")))")
    @Mapping(target = "load", expression = "java(toDouble(attributes.get(\"LOAD\")))")
    @Mapping(target = "memTotal", expression = "java(toLong(attributes.get(\"MEM_TOTAL\")))")
    @Mapping(target = "memUsed", expression = "java(toLong(attributes.get(\"MEM_USED\")))")
    @Mapping(target = "totalSwapSpace",
            expression = "java(toDouble(attributes.get(\"TOTAL_SWAP_SPACE\")))")
    @Mapping(target = "usedSwapSpace",
            expression = "java(toDouble(attributes.get(\"USED_SWAP_SPACE\")))")
    Host attributesToHost(final Map<String, String> attributes);

    default Host mapToHost(final SgeHost sgeHost) {
        final Host host = attributesToHost(extractHostAttributesMapping(sgeHost));
        host.setHostname(sgeHost.getHostname());
        return host;
    }

    default Map<String, String> extractHostAttributesMapping(final SgeHost sgeHost) {
        return sgeHost.getHostAttributes().stream()
                .collect(Collectors.toMap(name -> Optional.of(name)
                                .map(SgeHostValue::getName)
                                .map(Enum::name)
                                .orElse(TextConstants.EMPTY_STRING),
                        value -> Optional.of(value)
                                .map(SgeHostValue::getValue)
                                .orElse(TextConstants.EMPTY_STRING)));
    }

    default String trimEmptyToNull(final String value) {
        return NumberParseUtils.trimEmptyToNull(value);
    }

    default Integer toInt(final String value) {
        return NumberParseUtils.toInt(value);
    }

    default Double toDouble(final String value) {
        return NumberParseUtils.toDouble(value);
    }

    default Long toLong(final String value) {
        return NumberParseUtils.toLong(value);
    }
}
