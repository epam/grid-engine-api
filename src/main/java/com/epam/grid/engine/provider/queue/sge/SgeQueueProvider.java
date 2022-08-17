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

package com.epam.grid.engine.provider.queue.sge;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.EntitiesRawOutput;
import com.epam.grid.engine.entity.QueueFilter;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.QueueVO;
import com.epam.grid.engine.entity.queue.sge.SgeQueue;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.mapper.queue.sge.SgeQueueMapper;
import com.epam.grid.engine.provider.queue.QueueProvider;
import com.epam.grid.engine.provider.utils.CommandsUtils;
import com.epam.grid.engine.provider.utils.sge.common.SgeOutputParsingUtils;
import com.epam.grid.engine.provider.utils.sge.queue.SgeDeleteQueueCommandUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.grid.engine.provider.utils.CommandsUtils.determineStatus;
import static com.epam.grid.engine.provider.utils.CommandsUtils.mergeOutputLines;
import static com.epam.grid.engine.provider.utils.sge.common.SgeEntitiesRegistrationUtils.normalizePathToUnixFormat;
import static com.epam.grid.engine.provider.utils.sge.common.SgeEntitiesRegistrationUtils.deleteTemporaryDescriptionFile;
import static com.epam.grid.engine.utils.TextConstants.SPACE;

/**
 * A service class, that incorporates business logic, connected with Sun Grid Engine queue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SGE")
public class SgeQueueProvider implements QueueProvider {

    private static final String FILTER_PARAMETER = "filter";
    private static final String QUEUE_ENTITY = "queue";
    private static final String QCONF_SQL = "qconf_sql";
    private static final String QCONF_AQ = "qconf_aq";
    private static final String QCONF_MQ = "qconf_Mq";
    private static final String PREFIX = "qname";

    private static final String QNAME_FIELD = "qname";
    private static final String HOST_LIST_FIELD = "hostList";
    private static final String PE_LIST_FIELD = "peList";
    private static final String OWNER_LIST_FIELD = "ownerList";
    private static final String USER_LIST_FIELD = "userList";

    private static final String HOST_LIST_DEFAULT = "@allhosts";
    private static final String PE_LIST_DEFAULT = "make smp mpi";
    private static final String OWNER_LIST_DEFAULT = "NONE";
    private static final String USER_LIST_DEFAULT = "arusers";

    private static final String TEMPORARY_FILE_WAS_NOT_FOUND =
            "Temporary file with queue description was not found";
    private static final String WRONG_QUEUE_REGISTRATION_REQUEST_MESSAGE =
            "Queue registration request should exist, queue name should be specified!";
    private static final String WRONG_QUEUE_UPDATE_REQUEST_MESSAGE =
            "Queue update request should exist, queue name and at least one more property should be specified!";
    private static final String INVALID_QUEUE_UPDATE_REQUEST_INFO =
            "Invalid queue update request: {}";

    /**
     * The MapStruct mapping mechanism used.
     */
    private final SgeQueueMapper queueMapper;

    private final SimpleCmdExecutor simpleCmdExecutor;

    /**
     * An object that forms the structure of an executable command according to a template.
     */
    private final GridEngineCommandCompiler commandCompiler;

    /**
     * Returns the type of used grid engine. In this case - Sun Grid Engine.
     *
     * @return the engine type - Sun Grid Engine
     */
    @Override
    public CommandType getProviderType() {
        return CommandType.SGE;
    }

    /**
     * Returns a List containing the existing Queues. Each Queue entity of
     * this List has only one variable representing Queue's name.
     *
     * @return a List containing the existing Queues with their names
     */
    @Override
    public List<Queue> listQueues() {
        final CommandResult commandResult = simpleCmdExecutor.execute(buildQueueListingCommand(null));
        verifyProcessStatus(commandResult, HttpStatus.NOT_FOUND);

        return commandResult.getStdOut().stream()
                .map(s -> Queue.builder()
                        .name(s)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Returns a List containing specified Queues. Each element of the List
     * includes full information about certain Queue entity.
     *
     * @param queueFilter a List containing names of the certain Queues
     * @return a list containing specified Queues
     */
    @Override
    public List<Queue> listQueues(final QueueFilter queueFilter) {
        final CommandResult commandResult = simpleCmdExecutor.execute(buildQueueListingCommand(queueFilter));
        verifyProcessStatus(commandResult, HttpStatus.NOT_FOUND);

        final List<EntitiesRawOutput> queuesDescription = SgeOutputParsingUtils
                .splitOutputToEntities(commandResult.getStdOut(), PREFIX);

        final List<SgeQueue> sgeQueues = queuesDescription.stream()
                .map(EntitiesRawOutput::getRawEntitiesList)
                .map(SgeOutputParsingUtils::parseEntitiesToMap)
                .map(queueMapper::mapRawOutputToSgeQueue)
                .collect(Collectors.toList());

        return sgeQueues.stream()
                .map(queueMapper::sgeQueueToQueue)
                .collect(Collectors.toList());
    }

    /**
     * Registers a queue matching the requested description in the Sun Grid Engine queuing system.
     *
     * @param registrationRequest the description of the queue to be registered
     * @return the registered {@link Queue}
     */
    @Override
    public Queue registerQueue(final QueueVO registrationRequest) {
        validateRegistrationRequest(registrationRequest);

        final Context context = createQueueRegistrationContext(registrationRequest);
        final Path pathToTemporaryQueueDescription = commandCompiler
                .compileEntityConfigFile(getProviderType(), QUEUE_ENTITY, context);

        final CommandResult commandResult = simpleCmdExecutor
                .execute(normalizePathToUnixFormat(pathToTemporaryQueueDescription, QCONF_AQ, commandCompiler));
        verifyProcessStatus(commandResult, determineStatus(commandResult.getStdErr()));

        return createQueueFromTemporaryFile(pathToTemporaryQueueDescription);
    }

    /**
     * Deletes the queue in accordance with the specified parameter.
     *
     * @param queueName Search parameters for queue to delete.
     * @return Which queues were deleted.
     */
    @Override
    public Queue deleteQueues(final String queueName) {
        SgeDeleteQueueCommandUtils.validateDeletionRequest(queueName);

        return Queue.builder()
                .name(parseAndExecuteDeleteCommand(queueName))
                .build();
    }

    /**
     * Updates a queue matching the requested description in the Sun Grid Engine queuing system.
     *
     * @param updateRequest the description of the queue to be updated
     * @return the updated {@link Queue}
     */
    @Override
    public Queue updateQueue(final QueueVO updateRequest) {
        validateUpdateRequest(updateRequest);

        final List<Queue> queues = listQueues(QueueFilter.builder().queues(List.of(updateRequest.getName())).build());
        if (queues.size() != 1) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Can't find exact one queue by name: " + updateRequest.getName());
        }
        final Queue queue = queues.get(0);
        final Context context = createQueueUpdateContext(updateRequest, queue);
        final Path pathToTemporaryQueueDescription = commandCompiler
                .compileEntityConfigFile(getProviderType(), QUEUE_ENTITY, context);

        final CommandResult commandResult = simpleCmdExecutor
                .execute(normalizePathToUnixFormat(pathToTemporaryQueueDescription, QCONF_MQ, commandCompiler));
        verifyProcessStatus(commandResult, determineStatus(commandResult.getStdErr()));

        return createQueueFromTemporaryFile(pathToTemporaryQueueDescription);
    }

    private Context createQueueUpdateContext(final QueueVO queueVO, final Queue queue) {
        final Context context = new Context();
        final String hostList = isParameterExists(queueVO.getHostList())
                ? String.join(SPACE, queueVO.getHostList())
                : String.join(SPACE, queue.getHostList());
        final String peList = isParameterExists(queueVO.getParallelEnvironmentNames())
                ? String.join(SPACE, queueVO.getParallelEnvironmentNames())
                : String.join(SPACE, queue.getParallelEnvironmentNames());
        final String ownerList = isParameterExists(queueVO.getOwnerList())
                ? String.join(SPACE, queueVO.getOwnerList())
                : String.join(SPACE, queue.getOwnerList());
        final String userList = isParameterExists(queueVO.getAllowedUserGroups())
                ? String.join(SPACE, queueVO.getAllowedUserGroups())
                : String.join(SPACE, queue.getAllowedUserGroups());

        context.setVariable(QNAME_FIELD, queueVO.getName());
        context.setVariable(HOST_LIST_FIELD, hostList);
        context.setVariable(PE_LIST_FIELD, peList);
        context.setVariable(OWNER_LIST_FIELD, ownerList);
        context.setVariable(USER_LIST_FIELD, userList);
        return context;
    }

    private Context createQueueRegistrationContext(final QueueVO queueVO) {
        final Context context = new Context();
        context.setVariable(QNAME_FIELD, queueVO.getName());

        final String hostList = isParameterExists(queueVO.getHostList())
                ? String.join(SPACE, queueVO.getHostList())
                : HOST_LIST_DEFAULT;

        final String peList = isParameterExists(queueVO.getParallelEnvironmentNames())
                ? String.join(SPACE, queueVO.getParallelEnvironmentNames())
                : PE_LIST_DEFAULT;

        final String ownerList = isParameterExists(queueVO.getOwnerList())
                ? String.join(SPACE, queueVO.getOwnerList())
                : OWNER_LIST_DEFAULT;

        final String userList = isParameterExists(queueVO.getAllowedUserGroups())
                ? String.join(SPACE, queueVO.getAllowedUserGroups())
                : USER_LIST_DEFAULT;

        context.setVariable(PE_LIST_FIELD, peList);
        context.setVariable(HOST_LIST_FIELD, hostList);
        context.setVariable(OWNER_LIST_FIELD, ownerList);
        context.setVariable(USER_LIST_FIELD, userList);
        return context;
    }

    private boolean isParameterExists(final List<String> parameter) {
        return parameter != null && !parameter.isEmpty();
    }

    private void verifyProcessStatus(final CommandResult commandResult, final HttpStatus status) {
        if (commandResult.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(commandResult, status);
        } else if (!commandResult.getStdErr().isEmpty()) {
            log.warn(commandResult.getStdErr().toString());
        }
    }

    private Queue createQueueFromTemporaryFile(final Path pathToTemporaryQueueDescription) {
        try (Stream<String> lines = Files.lines(pathToTemporaryQueueDescription)) {
            final List<String> rawEntity = lines.collect(Collectors.toList());
            final SgeQueue sgeQueue = queueMapper.mapRawOutputToSgeQueue(SgeOutputParsingUtils
                    .parseEntitiesToMap(rawEntity));
            return queueMapper.sgeQueueToQueue(sgeQueue);
        } catch (final IOException e) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, TEMPORARY_FILE_WAS_NOT_FOUND, e);
        } finally {
            deleteTemporaryDescriptionFile(pathToTemporaryQueueDescription);
        }
    }

    private String parseAndExecuteDeleteCommand(final String queueName) {
        final CommandResult commandResult = simpleCmdExecutor
                .execute(SgeDeleteQueueCommandUtils.buildDeleteQueueCommand(queueName,
                        commandCompiler, getProviderType()));

        if (commandResult.getExitCode() != 0) {
            throw new GridEngineException(determineStatus(commandResult.getStdErr()),
                    mergeOutputLines(commandResult.getStdErr()));
        }
        return queueName;
    }

    private String[] buildQueueListingCommand(final QueueFilter queueFilter) {
        final Context context = new Context();
        context.setVariable(FILTER_PARAMETER, queueFilter);
        return commandCompiler.compileCommand(getProviderType(), QCONF_SQL, context);
    }

    private void validateRegistrationRequest(final QueueVO request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, WRONG_QUEUE_REGISTRATION_REQUEST_MESSAGE);
        }
    }

    private void validateUpdateRequest(final QueueVO request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, WRONG_QUEUE_UPDATE_REQUEST_MESSAGE);
        }

        if (!isParameterExists(request.getHostList())
                && !isParameterExists(request.getParallelEnvironmentNames())
                && !isParameterExists(request.getOwnerList())
                && !isParameterExists(request.getAllowedUserGroups())) {
            log.info(INVALID_QUEUE_UPDATE_REQUEST_INFO, request);
            throw new GridEngineException(HttpStatus.BAD_REQUEST, WRONG_QUEUE_UPDATE_REQUEST_MESSAGE);
        }
    }
}
