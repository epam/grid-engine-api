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

package com.epam.grid.engine.provider.pe.sge;

import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.ParallelEnvFilter;
import com.epam.grid.engine.entity.parallelenv.AllocationRuleType;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.PeRegistrationVO;
import com.epam.grid.engine.entity.parallelenv.RuleState;
import com.epam.grid.engine.entity.parallelenv.UrgencyState;
import com.epam.grid.engine.entity.parallelenv.UrgencyStateType;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.parallelenv.sge.SgeParallelEnvProvider;
import org.apache.commons.collections4.ListUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_STRING;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
public class SgePeProviderTest {

    private static final List<String> VALID_PE = List.of(
            "pe_name            make",
            "slots              999",
            "user_lists         NONE",
            "xuser_lists        NONE",
            "start_proc_args    NONE",
            "stop_proc_args     NONE",
            "control_slaves     TRUE",
            "job_is_first_task  FALSE",
            "accounting_summary TRUE",
            "qsort_args         NONE");
    private static final List<String> WARNING = List.of("standard warning");
    private static final String FILL_UP = "$fill_up";
    private static final String ROUND_ROBIN = "$round_robin";
    private static final String PE_SLOTS = "$pe_slots";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String AVG = "avg";
    private static final String MAKE = "make";
    private static final String QCONF = "qconf";
    private static final String SP = "-sp";
    private static final String SPL = "-spl";
    private static final String AP = "-Ap";
    private static final List<String> DELETED_PE = Collections
            .singletonList("user deleted \"cre\" parallel environment");
    private static final List<String> DELETION_DENIED = Collections
            .singletonList("denied: parallel environment \"rmi\" does not exist");
    private static final String PE_EXPECTED = "cre";
    private static final String PE_EXPECTED_ERROR = "rmi";

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @Autowired
    private SgeParallelEnvProvider sgePeProvider;

    @Test
    public void shouldFailIfExitCodeNotNull() {
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(EMPTY_LIST)
                .exitCode(1)
                .build();

        doReturn(commandResult).when(mockCmdExecutor).execute(QCONF, SP, MAKE);
        final ParallelEnvFilter parallelEnvFilter = new ParallelEnvFilter();
        parallelEnvFilter.setParallelEnvs(List.of(MAKE));

        final Throwable emptyListThrown = Assertions.assertThrows(GridEngineException.class,
                () -> sgePeProvider.listParallelEnv(parallelEnvFilter));
        Assertions.assertNotNull(emptyListThrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource({"filterStates"})
    public void shouldReturnAllThePe(final List<String> filterState) {
        final ParallelEnvFilter peFilter = ParallelEnvFilter.builder().parallelEnvs(filterState).build();
        final ParallelEnv makePe = buildPe(new String[]{MIN, FILL_UP});
        makePe.setName(MAKE);

        final CommandResult splCommandResult = CommandResult.builder()
                .stdOut(List.of(MAKE))
                .stdErr(EMPTY_LIST)
                .exitCode(0)
                .build();

        final CommandResult spCommandResult = CommandResult.builder()
                .stdOut(buildTemplate(new String[]{MIN, FILL_UP}))
                .stdErr(EMPTY_LIST)
                .exitCode(0)
                .build();

        doReturn(splCommandResult).when(mockCmdExecutor).execute(QCONF, SPL);
        doReturn(spCommandResult).when(mockCmdExecutor).execute(QCONF, SP, MAKE);

        final List<ParallelEnv> resultPE = sgePeProvider.listParallelEnv(peFilter);

        Assertions.assertEquals(EngineType.SGE, sgePeProvider.getProviderType());
        Assertions.assertEquals(resultPE.get(0), makePe);
    }

    static Stream<Arguments> filterStates() {
        return mapObjectsToArgumentsStream(
                null,
                EMPTY_LIST
        );
    }

    @ParameterizedTest
    @MethodSource({"buildStates"})
    public void shouldReturnCorrectMapStatesPe(final String[] states) {
        final ParallelEnv makePe = buildPe(states);
        makePe.setName(MAKE);

        final CommandResult commandResult = CommandResult.builder()
                .stdOut(buildTemplate(states))
                .stdErr(WARNING)
                .build();

        doReturn(commandResult).when(mockCmdExecutor).execute(QCONF, SP, MAKE);
        final ParallelEnvFilter parallelEnvFilter = new ParallelEnvFilter();
        parallelEnvFilter.setParallelEnvs(List.of(MAKE));
        final List<ParallelEnv> resultPE = sgePeProvider.listParallelEnv(parallelEnvFilter);

        Assertions.assertEquals(EngineType.SGE, sgePeProvider.getProviderType());
        Assertions.assertEquals(resultPE.get(0), makePe);
    }

    static Stream<Arguments> buildStates() {
        return mapObjectsToArgumentsStream(
                new String[]{MIN, FILL_UP},
                new String[]{MAX, ROUND_ROBIN},
                new String[]{AVG, PE_SLOTS}
        );
    }

    @Test
    public void shouldFailWithEmptyRequest() {
        final Throwable emptyListThrown = Assertions.assertThrows(GridEngineException.class,
                () -> sgePeProvider.getParallelEnv(null));
        Assertions.assertNotNull(emptyListThrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource({"buildStates"})
    public void shouldReturnCorrectPe(final String[] states) {
        final ParallelEnv expectedPe = buildPe(states);
        expectedPe.setName(MAKE);

        final CommandResult commandResult = CommandResult.builder()
                .stdOut(buildTemplate(states))
                .stdErr(WARNING)
                .build();

        doReturn(commandResult).when(mockCmdExecutor).execute(QCONF, SP, MAKE);

        final ParallelEnv resultPE = sgePeProvider.getParallelEnv(MAKE);
        Assertions.assertEquals(EngineType.SGE, sgePeProvider.getProviderType());
        Assertions.assertEquals(resultPE, expectedPe);
    }

    @Test
    public void shouldReturnCorrectDeleteResponse() {
        final ParallelEnv expectedParallelEnv = ParallelEnv.builder()
                .name(PE_EXPECTED)
                .build();
        final CommandResult expectedCommandResult = CommandResult.builder()
                .stdOut(DELETED_PE)
                .stdErr(EMPTY_LIST)
                .exitCode(0)
                .build();
        doReturn(expectedCommandResult).when(mockCmdExecutor).execute(Mockito.any());
        final ParallelEnv resultParallelEnv = sgePeProvider.deleteParallelEnv(PE_EXPECTED);
        Assertions.assertEquals(expectedParallelEnv, resultParallelEnv);
    }

    @Test
    public void shouldReturnErrorDeleteResponse() {
        final CommandResult expectedErrorCommandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(DELETION_DENIED)
                .exitCode(1)
                .build();
        doReturn(expectedErrorCommandResult).when(mockCmdExecutor).execute(Mockito.any());
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                sgePeProvider.deleteParallelEnv(PE_EXPECTED_ERROR));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    public void shouldThrowsExceptionBecauseIncorrectCommand() {
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class, () ->
                sgePeProvider.deleteParallelEnv(null));
        Assertions.assertNotNull(thrown.getMessage());
    }

    @Test
    public void registerPeShouldInvokeRegisterCommandExecution() {
        final ParallelEnv expectedPe = buildPe(new String[]{MIN, FILL_UP});
        expectedPe.setName(MAKE);
        final PeRegistrationVO registrationRequest = PeRegistrationVO.builder()
                .name(expectedPe.getName())
                .slots(expectedPe.getSlots())
                .allocationRule(FILL_UP)
                .build();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(buildTemplate(new String[]{MIN, FILL_UP}))
                .stdErr(EMPTY_LIST)
                .exitCode(0)
                .build();

        doReturn(commandResult).when(mockCmdExecutor)
                .execute(Mockito.matches(QCONF), Mockito.matches(AP), Mockito.anyString());
        final ParallelEnv actualPe = sgePeProvider.registerParallelEnv(registrationRequest);

        Assertions.assertEquals(EngineType.SGE, sgePeProvider.getProviderType());
        Assertions.assertEquals(expectedPe.getName(), actualPe.getName());
        Assertions.assertEquals(expectedPe.getSlots(), actualPe.getSlots());
        Assertions.assertEquals(expectedPe.getAllocationRule(), actualPe.getAllocationRule());
    }

    @Test
    public void shouldFailIfExitCodeNotNullOnRegisterCommand() {
        final PeRegistrationVO registrationRequest
                = PeRegistrationVO.builder()
                .name(EMPTY_STRING)
                .build();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(EMPTY_LIST)
                .exitCode(1)
                .build();

        doReturn(commandResult).when(mockCmdExecutor)
                .execute(Mockito.matches(QCONF), Mockito.matches(AP), Mockito.anyString());

        final Throwable emptyPeNameException = Assertions.assertThrows(GridEngineException.class,
                () -> sgePeProvider.registerParallelEnv(registrationRequest));

        Assertions.assertNotNull(emptyPeNameException.getMessage());
    }

    private ParallelEnv buildPe(final String[] states) {
        return ParallelEnv.builder()
                .slots(999)
                .allowedUserGroups(EMPTY_LIST)
                .forbiddenUserGroups(EMPTY_LIST)
                .startProcArgs(EMPTY_LIST)
                .stopProcArgs(EMPTY_LIST)
                .controlSlaves(true)
                .urgencySlots(buildUrgencyState(states[0]))
                .allocationRule(buildRuleState(states[1]))
                .jobIsFirstTask(false)
                .accountingSummary(true)
                .build();
    }

    private static List<String> buildTemplate(final String[] strings) {
        final List<String> urgency = new ArrayList<>(List.of("urgency_slots    " + strings[0]));
        urgency.addAll(List.of("allocation_rule    " + strings[1]));

        return ListUtils.union(VALID_PE, urgency);
    }

    private static UrgencyState buildUrgencyState(final String urgencySlot) {
        return UrgencyState.builder()
                .urgencyStateType(UrgencyStateType.getSlot(urgencySlot))
                .build();
    }

    private static RuleState buildRuleState(final String state) {
        return RuleState.builder()
                .allocationRule(AllocationRuleType.getRule(state))
                .build();
    }

    private static Stream<Arguments> mapObjectsToArgumentsStream(final Object... args) {
        return Stream.of(args).map(Arguments::of);
    }
}
