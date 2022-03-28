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

package com.epam.grid.engine.provider.hostgroup.sge;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.HostGroupFilter;
import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.entity.hostgroup.sge.SgeHostGroup;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.mapper.hostgroup.sge.SgeHostGroupMapper;
import com.epam.grid.engine.provider.hostgroup.HostGroupProvider;
import com.epam.grid.engine.provider.utils.CommandsUtils;
import com.epam.grid.engine.provider.utils.sge.common.SgeOutputParsingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provider class that addressing a request
 * to the Sun Grid Engine and processes the response received.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SgeHostGroupProvider implements HostGroupProvider {

    private static final String AT_SYMBOL = "@";
    private static final String FILTER = "filter";
    private static final String QCONF_COMMAND = "qconf";

    private final SimpleCmdExecutor simpleCmdExecutor;
    private final SgeHostGroupMapper hostGroupMapper;

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
    public EngineType getProviderType() {
        return EngineType.SGE;
    }

    /**
     * Returns a List containing specified HostGroups. Each element of the List
     * includes full information about certain {@link HostGroup} entity.
     *
     * @param hostGroupFilter a List containing names of the certain HostGroups
     * @return a list containing specified HostGroups
     */
    @Override
    public List<HostGroup> listHostGroups(final HostGroupFilter hostGroupFilter) {
        final List<String> currentHostGroupNames = validateFilter(hostGroupFilter);
        final List<SgeHostGroup> fetchedSgeHostGroups = getHostGroupsByName(currentHostGroupNames);
        return mapSgeHostGroupToHostGroup(fetchedSgeHostGroups);
    }

    /**
     * Returns {@link HostGroup} by specified group name.
     *
     * @param groupName The specified group name.
     * @return Information about the group.
     */
    @Override
    public HostGroup getHostGroup(final String groupName) {
        verifyGroupName(groupName);
        final HostGroupFilter filter = new HostGroupFilter(Collections.singletonList(groupName));
        return listHostGroups(filter).get(0);
    }

    private List<HostGroup> mapSgeHostGroupToHostGroup(final List<SgeHostGroup> sgeHostGroups) {
        return sgeHostGroups.stream()
                .map(hostGroupMapper::sgeHostGroupToHostGroup)
                .collect(Collectors.toList());
    }

    private List<String> buildRequest(final List<String> hostGroupNames) {
        final Context context = new Context();
        context.setVariable(FILTER, new HostGroupFilter(hostGroupNames));
        final CommandResult commandResult = simpleCmdExecutor.execute(commandCompiler
                .compileCommand(getProviderType(), QCONF_COMMAND, context));
        validateCommandResult(commandResult);

        return commandResult.getStdOut();
    }

    private List<SgeHostGroup> getHostGroupsByName(final List<String> hostGroupNames) {
        return hostGroupNames.stream()
                .map(Collections::singletonList)
                .map(this::buildRequest)
                .map(SgeOutputParsingUtils::parseEntitiesToMap)
                .map(hostGroupMapper::mapToSgeHostGroup)
                .collect(Collectors.toList());
    }

    private List<String> validateFilter(final HostGroupFilter hostGroupFilter) {
        if (CollectionUtils.isEmpty(hostGroupFilter.getHostGroupNames())) {
            return buildRequest(null);
        }
        return hostGroupFilter.getHostGroupNames().stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private void verifyGroupName(final String groupName) {
        if (!groupName.startsWith(AT_SYMBOL)) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST,
                    "Incorrect host group name: should start with @ symbol!");
        }
    }

    private void validateCommandResult(final CommandResult commandResult) {
        if (commandResult.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(commandResult);
        } else if (!commandResult.getStdErr().isEmpty()) {
            log.warn(commandResult.getStdErr().toString());
        }
    }
}
