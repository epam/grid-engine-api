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

package com.epam.grid.engine.provider.queue.slurm;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.QueueFilter;
import com.epam.grid.engine.entity.host.slurm.SlurmHost;
import com.epam.grid.engine.entity.queue.slurm.SlurmQueue;
import com.epam.grid.engine.entity.queue.Queue;
import com.epam.grid.engine.entity.queue.QueueVO;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.mapper.queue.slurm.SlurmQueueMapper;
import com.epam.grid.engine.provider.queue.QueueProvider;
import com.epam.grid.engine.provider.utils.CommandsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.epam.grid.engine.utils.TextConstants.COMMA;


/**
 * A service class, that incorporates business logic, connected with Slurm Grid Engine queue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SLURM")
public class SlurmQueueProvider implements QueueProvider {
    private static final String SCONTROL_COMMAND = "scontrol";
    private static final String SINFO_COMMAND = "sinfo";
    private static final String SCONTROL_CREATE_COMMAND = "create";
    private static final String SCONTROL_UPDATE_COMMAND = "update";
    private static final String SCONTROL_DELETE_COMMAND = "delete";

    private static final String SCONTROL_ACTION = "command";
    private static final String SCONTROL_USER_GROUPS = "allowedUserGroups";
    private static final String SCONTROL_HOSTS = "hostList";
    private static final String SCONTROL_PARTITION_NAME = "partitionName";
    private static final String STANDARD_SLURM_DELIMETER = "\\|";

    private static final Pattern NODES_RANGE_REGEX = Pattern.compile("([a-zA-Z]+)\\[(\\d+)-(\\d+)]");
    private static final int SCONTROL_OUPUT_SIZE = 4;
    private static final int SCONTROL_OUPUT_PARTNAME_INDEX = 0;
    private static final int SCONTROL_OUPUT_NODES_INDEX = 1;
    private static final int SCONTROL_OUPUT_CPUS_INDEX = 2;
    private static final int SCONTROL_OUPUT_USERGROUPS_INDEX = 3;

    private final SimpleCmdExecutor simpleCmdExecutor;
    private final SlurmQueueMapper queueMapper;

    /**
     * An object that forms the structure of an executable command according to a template.
     */
    private final GridEngineCommandCompiler commandCompiler;

    /**
     * Returns Slurm Grid Engine.
     *
     * @return current engine type - Slurm Grid Engine
     */
    @Override
    public CommandType getProviderType() {
        return CommandType.SLURM;
    }

    @Override
    public Queue registerQueue(final QueueVO registrationRequest) {
        checkRegistrationRequest(registrationRequest);
        final Context context = prepareContext(registrationRequest);
        final CommandResult result = simpleCmdExecutor.execute(commandCompiler.compileCommand(getProviderType(),
                SCONTROL_COMMAND, context));
        checkIsResultIsCorrect(result);
        return listQueues(
                QueueFilter.builder().queues(Collections.singletonList(registrationRequest.getName())).build()
        ).stream()
        .findFirst()
        .orElseThrow(() -> new GridEngineException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Can't load a queue that was just registered"));
    }

    @Override
    public Queue updateQueue(final QueueVO updateRequest) {
        checkRegistrationRequest(updateRequest);

        final String updateName = updateRequest.getName();
        final List<String> updateHostList = updateRequest.getHostList();
        final List<String> updateUserGroups = ListUtils.emptyIfNull(updateRequest.getAllowedUserGroups()).stream()
                .sorted()
                .collect(Collectors.toList());
        if (!StringUtils.hasText(updateName) || CollectionUtils.isEmpty(updateHostList)
                || CollectionUtils.isEmpty(updateUserGroups)) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Name, hostList and allowedUserGroups should be "
                    + "specified for successful partition update");
        }

        final Context context = createContextWithUpdatePartitionName(updateName);
        final CommandResult sinfoResult = simpleCmdExecutor.execute(commandCompiler.compileCommand(getProviderType(),
                SINFO_COMMAND, context));
        checkIfExecutionResultIsEmpty(sinfoResult);

        final SlurmQueue partitionData = parseResultToSlurmQueues(sinfoResult.getStdOut())
                .stream()
                .findFirst()
                .orElseThrow(
                        () -> new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't load slurm queue.")
                );

        final List<String> currentNodesParsed = partitionData.getNodelist().stream()
                .map(SlurmHost::getNodeName).collect(Collectors.toList());
        final List<String> userGroups = partitionData.getGroups();

        final List<String> updateHostListParsed = parseGroupOfNodes(updateHostList);

        if (updateHostListParsed.equals(currentNodesParsed) && updateUserGroups.equals(userGroups)) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "New partition properties and the current one are "
                    + "equal");
        }
        fillContextWithDataToUpdate(context, updateUserGroups, updateHostListParsed);

        final CommandResult result = simpleCmdExecutor.execute(commandCompiler.compileCommand(getProviderType(),
                SCONTROL_COMMAND, context));
        checkIsResultIsCorrect(result);

        return listQueues(
                QueueFilter.builder().queues(Collections.singletonList(updateName)).build()
        ).stream()
        .findFirst()
        .orElseThrow(() -> new GridEngineException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Can't load a queue that was just registered"));
    }

    @Override
    public List<Queue> listQueues() {
        final CommandResult result = simpleCmdExecutor.execute(commandCompiler.compileCommand(getProviderType(),
                SINFO_COMMAND, new Context()));
        checkIsResultIsCorrect(result);

        return fillQueueNameFromOutput(result.getStdOut());
    }

    @Override
    public List<Queue> listQueues(final QueueFilter queueFilter) {
        final Context context = new Context();
        if (queueFilter.getQueues() != null) {
            context.setVariable(SCONTROL_PARTITION_NAME, queueFilter.getQueues());
        }
        final CommandResult result = simpleCmdExecutor.execute(commandCompiler.compileCommand(getProviderType(),
                SINFO_COMMAND, context));
        checkIsResultIsCorrect(result);

        return fillQueueData(result.getStdOut());
    }

    @Override
    public Queue deleteQueues(final String queueName) {
        if (queueName == null) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Partition name for deletion should be specified");
        }
        final Context context = new Context();
        context.setVariable(SCONTROL_ACTION, SCONTROL_DELETE_COMMAND);
        context.setVariable(SCONTROL_PARTITION_NAME, queueName);
        final CommandResult result = simpleCmdExecutor.execute(commandCompiler.compileCommand(getProviderType(),
                SCONTROL_COMMAND, context));
        checkIsResultIsCorrect(result);
        return Queue.builder()
                .name(queueName)
                .build();
    }

    private List<String> parseGroupOfNodes(final List<String> hostList) {
        return hostList.stream()
                .flatMap(this::mapHostRangeToHosts)
                .collect(Collectors.toList());
    }

    private void checkIsResultIsCorrect(final CommandResult result) {
        if (result.getExitCode() != 0 || !result.getStdErr().isEmpty()) {
            CommandsUtils.throwExecutionDetails(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void fillContextWithDataToUpdate(final Context context, final List<String> updateUserGroups,
                                             final List<String> updateHostList) {
        context.setVariable(SCONTROL_ACTION, SCONTROL_UPDATE_COMMAND);
        context.setVariable(SCONTROL_USER_GROUPS, updateUserGroups);
        context.setVariable(SCONTROL_HOSTS, updateHostList);
    }

    private Context createContextWithUpdatePartitionName(final String updateName) {
        final Context context = new Context();
        context.setVariable(SCONTROL_PARTITION_NAME, updateName);
        return context;
    }

    private void checkIfExecutionResultIsEmpty(final CommandResult executionResult) {
        if (executionResult.getExitCode() != 0 || !executionResult.getStdErr().isEmpty()
                || executionResult.getStdOut().isEmpty()) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Failed to fetch partition data. Check partition "
                    + "name. " + executionResult);
        }
    }

    private SlurmQueue getPartitionData(final String resultOutputString) {
        final String[] nodeDataArray = resultOutputString.split(STANDARD_SLURM_DELIMETER);
        if (nodeDataArray.length != SCONTROL_OUPUT_SIZE) {
            throw new GridEngineException(HttpStatus.NOT_FOUND, "Node data is inconsistent: waiting for "
                    + SCONTROL_OUPUT_SIZE + " fields, but " + nodeDataArray.length + " were fetched.");
        }

        final String nodeName = nodeDataArray[SCONTROL_OUPUT_NODES_INDEX];

        final int cpusPerNode = Integer.parseInt(nodeDataArray[SCONTROL_OUPUT_CPUS_INDEX]);

        final List<String> userGroups = Arrays.stream(nodeDataArray[SCONTROL_OUPUT_USERGROUPS_INDEX].split(COMMA))
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        return SlurmQueue.builder()
                .partition(nodeDataArray[SCONTROL_OUPUT_PARTNAME_INDEX])
                .nodelist(
                        Collections.singletonList(SlurmHost.builder()
                                .nodeName(nodeName).cpuTotal(cpusPerNode).build())
                ).groups(userGroups)
                .build();
    }

    private List<Queue> fillQueueNameFromOutput(final List<String> resultOutput) {
        return ListUtils.emptyIfNull(resultOutput).stream()
                .map(partitionData -> {
                    final String[] partitionDataArray = partitionData.split(STANDARD_SLURM_DELIMETER);
                    if (partitionDataArray.length != SCONTROL_OUPUT_SIZE) {
                        throw new GridEngineException(HttpStatus.NOT_FOUND, "Partition output data is incorrect");
                    }
                    return partitionDataArray[SCONTROL_OUPUT_PARTNAME_INDEX].trim();
                })
                .distinct()
                .map(partitionName -> Queue.builder()
                        .name(partitionName)
                        .build())
                .collect(Collectors.toList());
    }

    private List<Queue> fillQueueData(final List<String> resultOutput) {
        return parseResultToSlurmQueues(resultOutput)
                .stream()
                .map(queueMapper::slurmQueueToQueue)
                .collect(Collectors.toList());
    }

    private List<SlurmQueue> parseResultToSlurmQueues(final List<String> resultOutput) {
        return ListUtils.emptyIfNull(resultOutput).stream()
                .map(this::getPartitionData)
                .collect(Collectors.groupingBy(
                        SlurmQueue::getPartition,
                        Collectors.reducing((q1, q2) -> {
                            q1.setNodelist(ListUtils.union(q1.getNodelist(), q2.getNodelist()));
                            return q1;
                        }))
                ).values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Context prepareContext(final QueueVO registrationRequest) {
        final Context context = new Context();
        context.setVariable(SCONTROL_ACTION, SCONTROL_CREATE_COMMAND);
        if (registrationRequest.getAllowedUserGroups() != null) {
            context.setVariable(SCONTROL_USER_GROUPS, registrationRequest.getAllowedUserGroups());
        }
        if (registrationRequest.getHostList() != null) {
            context.setVariable(SCONTROL_HOSTS, registrationRequest.getHostList());
        }
        context.setVariable(SCONTROL_PARTITION_NAME, registrationRequest.getName());
        return context;
    }

    private Stream<String> mapHostRangeToHosts(final String hosts) {
        final Matcher matcher = NODES_RANGE_REGEX.matcher(hosts);
        if (matcher.find()) {
            final String hostName = matcher.group(1);
            final int fromNodeIndex = Integer.parseInt(matcher.group(2));
            final int toNodeIndex = Integer.parseInt(matcher.group(3));
            return IntStream.rangeClosed(fromNodeIndex, toNodeIndex)
                    .mapToObj(hostNumber -> hostName.concat(String.valueOf(hostNumber)));
        } else {
            return Stream.of(hosts.trim());
        }

    }

    private void checkRegistrationRequest(final QueueVO registrationRequest) {
        if (registrationRequest.getName() == null) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Partition name option is obligatory");
        }
        if (registrationRequest.getParallelEnvironmentNames() != null) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Parallel environment variables cannot be used in "
                    + "Slurm!");
        }
        if (registrationRequest.getOwnerList() != null) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "Owners cannot be set for partitions in slurm");
        }
    }
}
