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

package com.epam.grid.engine.provider.healthcheck.sge;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.healthcheck.HealthCheckInfo;
import com.epam.grid.engine.provider.healthcheck.HealthCheckProvider;
import com.epam.grid.engine.provider.utils.sge.healthcheck.QpingCommandParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

/**
 * Provider class that addressing a request
 * to the Sun Grid Engine and processes the response received.
 */
@Service
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SGE")
public class SgeHealthCheckProvider implements HealthCheckProvider {

    private static final String QPING_COMMAND = "qping";
    private static final String DEFINE_QMASTER_COMMAND = "define_qmaster";

    private final String qmasterPort;
    private final String qmasterHostPath;
    private final SimpleCmdExecutor simpleCmdExecutor;
    private final GridEngineCommandCompiler commandCompiler;

    public SgeHealthCheckProvider(
            @Value("${sge.qmaster.port}") final String qmasterPort,
            @Value("${sge.qmaster.host.path}") final String qmasterHostPath,
            final SimpleCmdExecutor simpleCmdExecutor,
            final GridEngineCommandCompiler commandCompiler
    ) {
        this.qmasterPort = qmasterPort;
        this.qmasterHostPath = qmasterHostPath;
        this.simpleCmdExecutor = simpleCmdExecutor;
        this.commandCompiler = commandCompiler;
    }

    /**
     * This method tells which grid engine is used.
     *
     * @return Type of grid engine
     * @see CommandType
     */
    @Override
    public CommandType getProviderType() {
        return CommandType.SGE;
    }

    /**
     * This method accesses the grid engine after receiving health check request
     * and returns working grid engine status information.
     *
     * @return {@link HealthCheckInfo}
     */
    @Override
    public HealthCheckInfo checkHealth() {
        return executeQpingCommand();
    }

    private HealthCheckInfo executeQpingCommand() {
        final CommandResult result = simpleCmdExecutor.execute(getQpingCommand());
        return QpingCommandParser.parseQpingResult(result);
    }

    private String[] getQpingCommand() {
        final Context context = new Context();
        context.setVariable("qmasterHost", getNameQmasterHost());
        context.setVariable("qmasterPort", qmasterPort);
        return commandCompiler.compileCommand(getProviderType(), QPING_COMMAND, context);
    }

    private String getNameQmasterHost() {
        final Context context = new Context();
        context.setVariable("qmasterHostPath", qmasterHostPath);
        final String[] defineQmasterCommand = commandCompiler.compileCommand(getProviderType(),
                DEFINE_QMASTER_COMMAND, context);

        final CommandResult result = simpleCmdExecutor.execute(defineQmasterCommand);
        return QpingCommandParser.parseQmasterHostName(result);
    }
}
