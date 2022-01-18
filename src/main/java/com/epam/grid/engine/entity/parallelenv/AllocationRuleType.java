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
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;

/**
 * This enum contains states of allocationRule setting of PE.
 * To get more information about allocationRule see <a href= "http://gridscheduler.sourceforge.net/htmlman/htmlman5/sge_pe.html">
 * Grid Engine default load parameters</a>.
 *
 * @see SgeParallelEnv
 */
@Getter
@AllArgsConstructor
public enum AllocationRuleType {

    /**
     * Use all the job slots on a given host before moving to the next host.
     */
    FILL_UP("$fill_up"),

    /**
     * Select one slot from each host in a round-robin fashion until all job slots are assigned.
     */
    ROUND_ROBIN("$round_robin"),

    /**
     * Place all the job slots on a single machine.
     */
    PE_SLOTS("$pe_slots"),

    /**
     * Grid Engine will assign that many slots to the parallel job
     * on each host until the assigned number of job slots is met.
     */
    SLOTS_ON_ASSIGNED_HOST(EMPTY_STRING);

    private static final Map<String, AllocationRuleType> mapAllocationType =
            Stream.of(AllocationRuleType.values())
                    .collect(Collectors.toMap(AllocationRuleType::getStateCode, Function.identity()));

    private final String stateCode;

    /**
     * This method juxtaposes slot value and enum.
     *
     * @param state allocation setting
     * @return enum value
     */
    public static AllocationRuleType getRule(final String state) {
        return Optional.ofNullable(state)
                        .filter(StringUtils::hasText)
                        .map(mapAllocationType::get)
                        .orElseThrow(NoSuchElementException::new);

    }
}
