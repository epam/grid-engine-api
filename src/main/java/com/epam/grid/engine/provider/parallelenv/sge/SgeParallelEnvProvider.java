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

package com.epam.grid.engine.provider.parallelenv.sge;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EntitiesRawOutput;
import com.epam.grid.engine.entity.ParallelEnvFilter;
import com.epam.grid.engine.entity.parallelenv.AllocationRuleType;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.PeRegistrationVO;
import com.epam.grid.engine.entity.parallelenv.sge.SgeParallelEnv;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.mapper.parallelenv.sge.SgeParallelEnvMapper;
import com.epam.grid.engine.provider.parallelenv.ParallelEnvProvider;
import com.epam.grid.engine.provider.utils.CommandsUtils;
import com.epam.grid.engine.provider.utils.sge.common.SgeOutputParsingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.CommandsUtils.determineStatus;
import static com.epam.grid.engine.provider.utils.sge.common.SgeEntitiesRegistrationUtils.normalizePathToUnixFormat;
import static com.epam.grid.engine.provider.utils.sge.common.SgeEntitiesRegistrationUtils.deleteTemporaryDescriptionFile;

/**
 * This is the implementation of the PE provider for Sun Grid Engine.
 *
 * @see ParallelEnvProvider
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SGE")
public class SgeParallelEnvProvider implements ParallelEnvProvider {

    private static final String SPECIFY_REQUEST = "Name of PE should be specified!";
    private static final String WRONG_SIZE = "We expect to list one PE but get ";
    private static final String PE_NAME = "pe_name";
    private static final String QCONF_SPL = "qconf_spl";
    private static final String QCONF_SP = "qconf_sp";
    private static final String FILTER = "filter";
    private static final String QCONF_PE_DELETION = "qconf_PE_deletion";
    private static final String WRONG_PE_NAME_MESSAGE = "Invalid parallel environment deletion request:"
            + " Parallel environment name should be specified!";
    private static final String WRONG_PE_REG_MESSAGE =
            "Parallel environment registration properties should be specified correctly!";
    private static final String PE_NAME_FIELD = "parallelEnvName";
    private static final String SLOTS_FIELD = "slots";
    private static final String ALLOCATION_RULE_FIELD = "allocationRule";
    private static final String PE_REG_TEMPLATE_FILE = "pe_registration_template";
    private static final String PE_REG_COMMAND_FILE = "pe_registration_command";
    private static final String TEMPORARY_FILE_WAS_NOT_FOUND =
            "Temporary file with parallel environment description was not found";

    /**
     * The MapStruct mapping mechanism used.
     */
    private final SgeParallelEnvMapper parallelEnvMapper;

    /**
     * The executor that provide the ability to call any command available in the current environment.
     */
    private final SimpleCmdExecutor simpleCmdExecutor;

    /**
     * An object that forms the structure of an executable command according to a template.
     */
    private final GridEngineCommandCompiler commandCompiler;

    private final String peRegistrationDefaultSlots;

    private final String peRegistrationDefaultAllocationRule;

    public SgeParallelEnvProvider(final SgeParallelEnvMapper parallelEnvMapper,
                                  final SimpleCmdExecutor simpleCmdExecutor,
                                  final GridEngineCommandCompiler commandCompiler,
                                  @Value("${sge.parallel.environment.registration.default.slots:999}")
                                  final String peRegistrationDefaultSlots,
                                  @Value("${sge.parallel.environment.registration.default.allocation.rule:$fill_up}")
                                  final String peRegistrationDefaultAllocationRule) {
        this.parallelEnvMapper = parallelEnvMapper;
        this.simpleCmdExecutor = simpleCmdExecutor;
        this.commandCompiler = commandCompiler;
        this.peRegistrationDefaultSlots = peRegistrationDefaultSlots;
        this.peRegistrationDefaultAllocationRule = peRegistrationDefaultAllocationRule;
    }

    /**
     * This method tells what grid engine is used.
     *
     * @return Type of grid engine
     * @see EngineType
     */
    @Override
    public EngineType getProviderType() {
        return EngineType.SGE;
    }

    /**
     * Lists PE available in SGE according to limitations from {@link ParallelEnvFilter},
     * if filter is empty it will return all the pe.
     *
     * @param parallelEnvFilter names of PE needed
     * @return List of {@link ParallelEnv}
     */
    @Override
    public List<ParallelEnv> listParallelEnv(final ParallelEnvFilter parallelEnvFilter) {
        final ParallelEnvFilter filter = validateAndFormPeFilter(parallelEnvFilter);
        final Context context = new Context();
        context.setVariable(FILTER, filter);
        final String[] command = commandCompiler.compileCommand(getProviderType(), QCONF_SP, context);
        final List<EntitiesRawOutput> rawPeList = SgeOutputParsingUtils
                .splitOutputToEntities(executeCommandAndGetOutput(command), PE_NAME);

        return rawPeList.stream()
                .map(EntitiesRawOutput::getRawEntitiesList)
                .map(SgeOutputParsingUtils::parseEntitiesToMap)
                .map(parallelEnvMapper::mapRawOutputToSgePe)
                .map(parallelEnvMapper::mapSgePeToPe)
                .collect(Collectors.toList());
    }

    /**
     * Provides PE object that was requested.
     *
     * @param peName PE name
     * @return {@link ParallelEnv}
     */
    @Override
    public ParallelEnv getParallelEnv(final String peName) {
        final List<ParallelEnv> peListingResult = listParallelEnv(mapToEnvsFilter(peName));
        final int peListingSize = peListingResult.size();

        if (peListingSize != 1) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, WRONG_SIZE + peListingSize);
        }
        return peListingResult.get(0);
    }

    /**
     * Deletes specified parallel environment.
     *
     * @param parallelEnvName the name of the deleting parallel environment
     * @return {@link ParallelEnv} which was deleted
     */
    @Override
    public ParallelEnv deleteParallelEnv(final String parallelEnvName) {
        validateParallelEnvName(parallelEnvName);
        return ParallelEnv.builder()
                .name(executeDeleteCommand(parallelEnvName))
                .build();
    }

    /**
     * Registers a {@link ParallelEnv} matching the requested description in the Sun Grid Engine.
     *
     * @param registrationRequest the description of the parallel environment to be registered
     * @return the registered {@link ParallelEnv}
     */
    @Override
    public ParallelEnv registerParallelEnv(final PeRegistrationVO registrationRequest) {
        validateRegistrationRequest(registrationRequest);
        final Path pathToTemporaryPeDescription = createPeRegistrationTmpFile(registrationRequest);
        final CommandResult commandResult = simpleCmdExecutor
                .execute(normalizePathToUnixFormat(pathToTemporaryPeDescription, PE_REG_COMMAND_FILE, commandCompiler));
        verifyProcessStatus(commandResult, determineStatus(commandResult.getStdErr()));
        return createPeFromTemporaryFile(pathToTemporaryPeDescription);
    }

    private void validateRegistrationRequest(final PeRegistrationVO registrationRequest) {
        if (!StringUtils.hasText(registrationRequest.getName())
                || isInvalidSlotsParameter(registrationRequest.getSlots())
                || isInvalidAllocationRuleTypeParameter(registrationRequest.getAllocationRule())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST,
                    WRONG_PE_REG_MESSAGE);
        }
    }

    private boolean isInvalidAllocationRuleTypeParameter(final String allocationRuleType) {
        return Optional.ofNullable(allocationRuleType).isPresent()
                && Arrays.stream(AllocationRuleType.values())
                .map(AllocationRuleType::getStateCode)
                .noneMatch(allocationRuleType::equals);
    }

    private boolean isInvalidSlotsParameter(final Integer slots) {
        return Optional.ofNullable(slots)
                .filter(s -> s < 0)
                .isPresent();
    }

    private Path createPeRegistrationTmpFile(final PeRegistrationVO peRegistrationVO) {
        final Context context = new Context();
        context.setVariable(PE_NAME_FIELD, peRegistrationVO.getName());
        final String slots = peRegistrationVO.getSlots() != null
                ? String.valueOf(peRegistrationVO.getSlots())
                : peRegistrationDefaultSlots;
        context.setVariable(SLOTS_FIELD, slots);
        final String allocationRule = peRegistrationVO.getAllocationRule() != null
                ? peRegistrationVO.getAllocationRule()
                : peRegistrationDefaultAllocationRule;
        context.setVariable(ALLOCATION_RULE_FIELD, allocationRule);
        return commandCompiler
                .compileEntityConfigFile(getProviderType(), PE_REG_TEMPLATE_FILE, context);
    }

    private void verifyProcessStatus(final CommandResult commandResult, final HttpStatus status) {
        if (commandResult.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(commandResult, status);
        } else if (!commandResult.getStdErr().isEmpty()) {
            log.warn(commandResult.getStdErr().toString());
        }
    }

    private ParallelEnv createPeFromTemporaryFile(final Path pathToTemporaryPeDescription) {
        try (Stream<String> lines = Files.lines(pathToTemporaryPeDescription)) {
            final List<String> rawEntities = lines.collect(Collectors.toList());
            final SgeParallelEnv sgeParallelEnv = parallelEnvMapper.mapRawOutputToSgePe(SgeOutputParsingUtils
                    .parseEntitiesToMap(rawEntities));
            return parallelEnvMapper.mapSgePeToPe(sgeParallelEnv);
        } catch (final IOException e) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, TEMPORARY_FILE_WAS_NOT_FOUND, e);
        } finally {
            deleteTemporaryDescriptionFile(pathToTemporaryPeDescription);
        }
    }

    private ParallelEnvFilter validateAndFormPeFilter(final ParallelEnvFilter parallelEnvFilter) {
        if (isParallelEnvFilterNotProvided(parallelEnvFilter)) {
            final String[] command = commandCompiler.compileCommand(getProviderType(), QCONF_SPL, new Context());
            final List<String> peList = executeCommandAndGetOutput(command);
            return new ParallelEnvFilter(peList);
        }
        return parallelEnvFilter;
    }

    private static boolean isParallelEnvFilterNotProvided(final ParallelEnvFilter peFilter) {
        return Optional.ofNullable(peFilter)
                .map(ParallelEnvFilter::getParallelEnvs)
                .filter(Predicate.not(List::isEmpty))
                .isEmpty();
    }

    private ParallelEnvFilter mapToEnvsFilter(final String peName) {
        return Optional.ofNullable(peName)
                .filter(StringUtils::hasText)
                .map(name -> new ParallelEnvFilter(List.of(name)))
                .orElseThrow(() -> new GridEngineException(HttpStatus.BAD_REQUEST, SPECIFY_REQUEST));
    }

    private String executeDeleteCommand(final String parallelEnvName) {
        final String[] deleteCommand = buildDeleteCommand(parallelEnvName);
        final CommandResult commandResult = simpleCmdExecutor.execute(deleteCommand);

        if (commandResult.getExitCode() == 0) {
            return parallelEnvName;
        }

        throw new GridEngineException(determineStatus(commandResult.getStdErr()),
                CommandsUtils.mergeOutputLines(commandResult.getStdErr()));
    }

    private String[] buildDeleteCommand(final String parallelEnv) {
        final Context context = new Context();
        context.setVariable(PE_NAME_FIELD, parallelEnv);
        return commandCompiler.compileCommand(getProviderType(), QCONF_PE_DELETION, context);
    }

    private void validateParallelEnvName(final String parallelEnvName) {
        if (!StringUtils.hasText(parallelEnvName)) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, WRONG_PE_NAME_MESSAGE);
        }
    }

    private List<String> executeCommandAndGetOutput(final String[] envCommand) {
        final CommandResult commandResult = simpleCmdExecutor.execute(envCommand);

        if (commandResult.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(commandResult);
        } else if (!commandResult.getStdErr().isEmpty()) {
            log.warn(commandResult.getStdErr().toString());
        }
        return commandResult.getStdOut();
    }
}
