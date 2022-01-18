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

package com.epam.grid.engine.provider.host.sge;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.entity.host.sge.SgeHostListing;
import com.epam.grid.engine.mapper.host.sge.SgeHostMapper;
import com.epam.grid.engine.provider.host.HostProvider;
import com.epam.grid.engine.provider.utils.JaxbUtils;
import com.epam.grid.engine.provider.utils.sge.common.SgeCommandsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.stream.Collectors;

import static com.epam.grid.engine.utils.TextConstants.NEW_LINE_DELIMITER;

/**
 * This is the implementation of the host provider for Sun Grid Engine.
 *
 * @see HostProvider
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SgeHostProvider implements HostProvider {

    private static final String FILTER = "filter";
    private static final String GLOBAL = "global";
    private static final String QHOST_COMMAND = "qhost";

    private final SgeHostMapper sgeHostMapper;

    /**
     * The executor that provide the ability to call any command available in the current environment.
     */
    private final SimpleCmdExecutor simpleCmdExecutor;

    /**
     * An object that forms the structure of an executable command according to a template.
     */
    private final GridEngineCommandCompiler commandCompiler;

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
     * Lists active hosts available in SGE according to limitations from {@link HostFilter}.
     *
     * @param hostFilter names of hosts needed
     * @return {@link Listing} of {@link Host}
     */
    @Override
    public Listing<Host> listHosts(final HostFilter hostFilter) {
        final Context context = new Context();
        context.setVariable(FILTER, hostFilter);
        final String[] hostCommand = commandCompiler.compileCommand(getProviderType(), QHOST_COMMAND, context);
        final CommandResult commandResult = simpleCmdExecutor.execute(hostCommand);
        if (commandResult.getExitCode() != 0) {
            SgeCommandsUtils.throwExecutionDetails(commandResult);
        } else if (!commandResult.getStdErr().isEmpty()) {
            log.warn(commandResult.getStdErr().toString());
        }
        return mapToHosts(JaxbUtils.unmarshall(String.join(NEW_LINE_DELIMITER,
                commandResult.getStdOut()),
                SgeHostListing.class));
    }

    private Listing<Host> mapToHosts(final SgeHostListing sgeHostListing) {
        return new Listing<>(CollectionUtils.emptyIfNull(sgeHostListing.getSgeHost())
                .stream()
                .map(sgeHostMapper::mapToHost)
                .filter(host -> !host.getHostname().equals(GLOBAL))
                .collect(Collectors.toList())
        );
    }
}
