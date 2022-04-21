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

package com.epam.grid.engine.provider.host.slurm;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.entity.host.slurm.SlurmHost;
import com.epam.grid.engine.mapper.host.slurm.SlurmHostMapper;
import com.epam.grid.engine.provider.host.HostProvider;
import com.epam.grid.engine.provider.utils.CommandsUtils;
import com.epam.grid.engine.provider.utils.slurm.host.ScontrolShowNodeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the implementation of the host provider for SLURM.
 *
 * @see com.epam.grid.engine.provider.host.HostProvider
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SLURM")
public class SlurmHostProvider implements HostProvider {

    private static final String FILTER = "filter";
    private static final String SCONTROL_COMMAND = "scontrol";

    private final SimpleCmdExecutor simpleCmdExecutor;
    private final GridEngineCommandCompiler commandCompiler;
    private final ScontrolShowNodeParser slurmHostParser;
    private final SlurmHostMapper slurmHostMapper;

    @Override
    public EngineType getProviderType() {
        return EngineType.SLURM;
    }

    @Override
    public Listing<Host> listHosts(final HostFilter hostFilter) {
        final Context context = new Context();
        context.setVariable(FILTER, hostFilter);
        final String[] hostCommand = commandCompiler.compileCommand(getProviderType(), SCONTROL_COMMAND, context);
        final CommandResult commandResult = simpleCmdExecutor.execute(hostCommand);
        if (commandResult.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(commandResult);
        } else if (!commandResult.getStdErr().isEmpty()) {
            log.warn(commandResult.getStdErr().toString());
        }
        return mapToHosts(commandResult.getStdOut().stream()
                .map(slurmHostParser::mapHostDataToSlurmHost)
                .collect(Collectors.toList()));
    }

    private Listing<Host> mapToHosts(final List<SlurmHost> hostList) {
        return new Listing<>(
                hostList.stream()
                        .map(slurmHostMapper::mapToHost)
                        .collect(Collectors.toList())
        );
    }
}
