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

package com.epam.grid.engine.mapper.parallelenv.sge;

import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.AllocationRuleType;
import com.epam.grid.engine.entity.parallelenv.RuleState;
import com.epam.grid.engine.entity.parallelenv.UrgencyState;
import com.epam.grid.engine.entity.parallelenv.UrgencyStateType;
import com.epam.grid.engine.entity.parallelenv.sge.SgeParallelEnv;
import com.epam.grid.engine.provider.utils.NumberParseUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.grid.engine.utils.TextConstants.SPACE;

/**
 * This interface consists of the configuration of a mapper for implementation by the MapStruct processor
 * for creation a {@link ParallelEnv} object out of a {@link SgeParallelEnv} object.
 */
@Mapper(componentModel = "spring")
public interface SgeParallelEnvMapper {

    /**
     * The actual mapping method expects the source object as parameter and returns the target object.
     *
     * @param sgeParallelEnv the mapping object
     * @return The mapped object
     */
    @Mapping(target = "allocationRule", ignore = true)
    @Mapping(target = "urgencySlots", ignore = true)
    ParallelEnv mapSgePeToPe(SgeParallelEnv sgeParallelEnv);

    /**
     * Maps raw output to {@link SgeParallelEnv} object.
     * To get more information about field formats of parallel environment configuration report see
     * <a href="http://gridscheduler.sourceforge.net/htmlman/htmlman5/sge_pe.html">
     * Sun Grid Engine parallel environment configuration file format</a>.
     * <p>
     * Note: there are inaccuracies in the description of the "user_lists", "xuser_lists" fields
     * in the manual - SGE in parallel environment configuration report in these fields returns
     * lists separated by spaces.
     *
     * @param peInfo map of PE attributes and their states
     * @return {@link SgeParallelEnv}
     */
    @Mapping(target = "name", source = "pe_name")
    @Mapping(target = "allowedUserGroups",
            expression = "java(parsePeDescriptionValuesWithSpace(peInfo.get(\"user_lists\")))")
    @Mapping(target = "forbiddenUserGroups",
            expression = "java(parsePeDescriptionValuesWithSpace(peInfo.get(\"xuser_lists\")))")
    @Mapping(target = "startProcArgs",
            expression = "java(parsePeDescriptionValuesWithSpace(peInfo.get(\"start_proc_args\")))")
    @Mapping(target = "stopProcArgs",
            expression = "java(parsePeDescriptionValuesWithSpace(peInfo.get(\"stop_proc_args\")))")
    @Mapping(target = "allocationRule", source = "allocation_rule")
    @Mapping(target = "controlSlaves", source = "control_slaves")
    @Mapping(target = "jobIsFirstTask", source = "job_is_first_task")
    @Mapping(target = "urgencySlots", source = "urgency_slots")
    @Mapping(target = "accountingSummary", source = "accounting_summary")
    @Mapping(target = "qsortArgs", expression = "java(parsePeDescriptionValuesWithSpace(peInfo.get(\"qsort_args\")))")
    SgeParallelEnv mapRawOutputToSgePe(final Map<String, String> peInfo);

    /**
     * The method maps the state attribute of the source object to the target object.
     *
     * @param sgeParallelEnv the mapping object
     * @param parallelEnv    the target object
     */
    @AfterMapping
    default void fillStates(final SgeParallelEnv sgeParallelEnv, final @MappingTarget ParallelEnv parallelEnv) {
        parallelEnv.setAllocationRule(mapRuleState(sgeParallelEnv.getAllocationRule()));
        parallelEnv.setUrgencySlots(mapUrgencyState(sgeParallelEnv.getUrgencySlots()));
    }

    /**
     * This method builds UrgencyState object.
     *
     * @param urgencySlot urgency
     * @return {@link UrgencyState}
     */
    default UrgencyState mapUrgencyState(final String urgencySlot) {
        if (NumberParseUtils.isNumber(urgencySlot)) {
            return UrgencyState.builder()
                    .urgencyStateType(UrgencyStateType.NUMBER)
                    .state(Integer.parseInt(urgencySlot))
                    .build();
        }
        return UrgencyState.builder()
                .urgencyStateType(UrgencyStateType.getSlot(urgencySlot))
                .build();
    }

    /**
     * This method builds RuleState object.
     *
     * @param state allocation rule state
     * @return {@link RuleState}
     */
    default RuleState mapRuleState(final String state) {
        if (NumberParseUtils.isNumber(state)) {
            return RuleState.builder()
                    .allocationRule(AllocationRuleType.SLOTS_ON_ASSIGNED_HOST)
                    .originalState(state)
                    .stateNumber(Integer.parseInt(state))
                    .build();
        }
        return RuleState.builder()
                .allocationRule(AllocationRuleType.getRule(state))
                .build();
    }

    default List<String> parsePeDescriptionValuesWithSpace(final String values) {
        if (!StringUtils.hasText(values) || values.equals("NONE")) {
            return Collections.emptyList();
        }
        return List.of(values.split(SPACE));
    }
}
