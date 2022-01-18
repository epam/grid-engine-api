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

import com.epam.grid.engine.entity.parallelenv.AllocationRuleType;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.RuleState;
import com.epam.grid.engine.entity.parallelenv.UrgencyState;
import com.epam.grid.engine.entity.parallelenv.UrgencyStateType;
import com.epam.grid.engine.entity.parallelenv.sge.SgeParallelEnv;
import com.epam.grid.engine.provider.utils.sge.common.SgeOutputParsingUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SgeParallelEnvMapperTest {

    private static final int SOME_SLOTS_NUMBER1 = 100;
    private static final int SOME_SLOTS_NUMBER2 = 999;
    private static final int SOME_NUMBER_RULE = 5;
    private static final int SOME_NUMBER_URGENCY_STATE = 15;
    private static final String SOME_NUMBER_RULE_STATE_STRING = String.valueOf(SOME_NUMBER_RULE);
    private static final String SOME_NUMBER_URGENCY_STATE_STRING = String.valueOf(SOME_NUMBER_URGENCY_STATE);
    private static final String SOME_PE_NAME = "make";
    private static final String FILL_UP = "$fill_up";
    private static final String ROUND_ROBIN = "$round_robin";
    private static final String PE_SLOTS = "$pe_slots";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String AVG = "avg";

    private static final List<String> testSgeParallelEnvRawOutput = List.of(
            "pe_name             make",
            "slots               " + SOME_SLOTS_NUMBER1,
            "user_lists          admins sgeusers",
            "xuser_lists         xusers guests",
            "start_proc_args     /opt/sge/examples/jobs/pascal.sh -catch_rsh $pe_hostfile",
            "stop_proc_args      /opt/sge/examples/jobs/simple.sh",
            "allocation_rule     $round_robin",
            "control_slaves      TRUE",
            "job_is_first_task   FALSE",
            "urgency_slots       min",
            "accounting_summary  FALSE",
            "qsort_args          libmyqsort.so myqsort 5");

    private static final List<String> USER_GROUPS = List.of("admins", "sgeusers");
    private static final List<String> XUSER_GROUPS = List.of("xusers", "guests");
    private static final List<String> START_PROC_ARGS = List.of("/opt/sge/examples/jobs/pascal.sh",
            "-catch_rsh", "$pe_hostfile");
    private static final List<String> STOP_PROC_ARGS = List.of("/opt/sge/examples/jobs/simple.sh");
    private static final List<String> QSORT_ARGS = List.of("libmyqsort.so", "myqsort", "5");

    private static final SgeParallelEnvMapper parallelEnvMapper = Mappers.getMapper(SgeParallelEnvMapper.class);

    @ParameterizedTest
    @MethodSource({"provideParametersForSgeParallelEnvCreating"})
    public void shouldReturnCorrectParallelEnv(final int slots,
                                               final String sourceAllocationRule,
                                               final boolean controlSlaves,
                                               final boolean jobIsFirstTask,
                                               final String sourceUrgency,
                                               final boolean accountingSummary,
                                               final RuleState expectedRuleState,
                                               final UrgencyState expectedUrgencyState) {
        final SgeParallelEnv sgeParallelEnv = SgeParallelEnv.builder()
                .name(SOME_PE_NAME)
                .slots(slots)
                .allowedUserGroups(EMPTY_LIST)
                .forbiddenUserGroups(EMPTY_LIST)
                .startProcArgs(EMPTY_LIST)
                .stopProcArgs(EMPTY_LIST)
                .allocationRule(sourceAllocationRule)
                .controlSlaves(controlSlaves)
                .jobIsFirstTask(jobIsFirstTask)
                .urgencySlots(sourceUrgency)
                .accountingSummary(accountingSummary)
                .qsortArgs(EMPTY_LIST)
                .build();

        final ParallelEnv expectedParallelEnv = ParallelEnv.builder()
                .name(SOME_PE_NAME)
                .slots(slots)
                .allowedUserGroups(EMPTY_LIST)
                .forbiddenUserGroups(EMPTY_LIST)
                .startProcArgs(EMPTY_LIST)
                .stopProcArgs(EMPTY_LIST)
                .allocationRule(expectedRuleState)
                .controlSlaves(controlSlaves)
                .jobIsFirstTask(jobIsFirstTask)
                .urgencySlots(expectedUrgencyState)
                .accountingSummary(accountingSummary)
                .build();

        assertEquals(expectedParallelEnv, parallelEnvMapper.mapSgePeToPe(sgeParallelEnv));
    }

    static Stream<Arguments> provideParametersForSgeParallelEnvCreating() {
        return Stream.of(
                Arguments.of(SOME_SLOTS_NUMBER1, SOME_NUMBER_RULE_STATE_STRING, false, false,
                        SOME_NUMBER_URGENCY_STATE_STRING, true,
                        buildNumberRuleState(SOME_NUMBER_RULE),
                        buildNumberUrgencyState(SOME_NUMBER_URGENCY_STATE)),
                Arguments.of(SOME_SLOTS_NUMBER1, FILL_UP, false, true, MIN, true,
                        buildTypeRuleState(AllocationRuleType.FILL_UP),
                        buildTypeUrgencyState(UrgencyStateType.MIN)),
                Arguments.of(SOME_SLOTS_NUMBER2, ROUND_ROBIN, true, false, MAX, false,
                        buildTypeRuleState(AllocationRuleType.ROUND_ROBIN),
                        buildTypeUrgencyState(UrgencyStateType.MAX)),
                Arguments.of(SOME_SLOTS_NUMBER2, PE_SLOTS, true, true, AVG, false,
                        buildTypeRuleState(AllocationRuleType.PE_SLOTS),
                        buildTypeUrgencyState(UrgencyStateType.AVG))
        );
    }

    @Test
    public void shouldMapRawOutputToSgeParallelEnv() {
        final SgeParallelEnv expectedSgeParallelEnv = SgeParallelEnv.builder()
                .name(SOME_PE_NAME)
                .slots(SOME_SLOTS_NUMBER1)
                .allowedUserGroups(USER_GROUPS)
                .forbiddenUserGroups(XUSER_GROUPS)
                .startProcArgs(START_PROC_ARGS)
                .stopProcArgs(STOP_PROC_ARGS)
                .allocationRule(ROUND_ROBIN)
                .controlSlaves(true)
                .jobIsFirstTask(false)
                .urgencySlots(MIN)
                .accountingSummary(false)
                .qsortArgs(QSORT_ARGS)
                .build();

        final Map<String, String> testSgeParallelEnvDescription = SgeOutputParsingUtils
                .parseEntitiesToMap(testSgeParallelEnvRawOutput);
        assertEquals(expectedSgeParallelEnv, parallelEnvMapper.mapRawOutputToSgePe(testSgeParallelEnvDescription));
    }

    private static UrgencyState buildNumberUrgencyState(final int slots) {
        return UrgencyState.builder()
                .urgencyStateType(UrgencyStateType.NUMBER)
                .state(slots)
                .build();
    }

    private static UrgencyState buildTypeUrgencyState(final UrgencyStateType type) {
        return UrgencyState.builder()
                .urgencyStateType(type)
                .build();
    }

    private static RuleState buildNumberRuleState(final int stateNumber) {
        return RuleState.builder()
                .allocationRule(AllocationRuleType.SLOTS_ON_ASSIGNED_HOST)
                .originalState(String.valueOf(stateNumber))
                .stateNumber(stateNumber)
                .build();
    }

    private static RuleState buildTypeRuleState(final AllocationRuleType type) {
        return RuleState.builder()
                .allocationRule(type)
                .build();
    }
}
