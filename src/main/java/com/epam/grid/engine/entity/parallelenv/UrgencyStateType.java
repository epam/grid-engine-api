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

package com.epam.grid.engine.entity.parallelenv;

import com.epam.grid.engine.entity.parallelenv.sge.SgeParallelEnv;
import com.epam.grid.engine.provider.utils.NumberParseUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This enum contains states of urgency state setting of PE.
 * To get more information about allocationRule see <a href="http://gridscheduler.sourceforge.net/htmlman/htmlman5/sge_pe.html">
 * Grid Engine default load parameters</a>.
 *
 * @see SgeParallelEnv
 */
@Getter
@AllArgsConstructor
public enum UrgencyStateType {

    MIN("min"),
    MAX("max"),
    AVG("avg"),
    NUMBER("number");

    private static final Map<String, UrgencyStateType> mapUrgencyType =
            Stream.of(UrgencyStateType.values())
                    .collect(Collectors.toMap(UrgencyStateType::getStateCode, Function.identity()));

    private final String stateCode;

    /**
     * This method juxtaposes slot value and enum.
     *
     * @param slot urgency priority
     * @return enum value
     */
    public static UrgencyStateType getSlot(final String slot) {
        return NumberParseUtils.isNumber(slot)
                ? UrgencyStateType.NUMBER
                : Optional.ofNullable(slot)
                    .filter(StringUtils::hasText)
                    .map(mapUrgencyType::get)
                    .orElseThrow(NoSuchElementException::new);
    }

}
