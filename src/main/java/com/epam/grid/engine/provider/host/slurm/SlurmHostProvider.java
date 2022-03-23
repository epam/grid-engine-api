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
import com.epam.grid.engine.provider.utils.sge.common.SgeCommandsUtils;
import com.epam.grid.engine.provider.utils.slurm.host.ScontrolShowNodeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        final String[] hostCommand = commandCompiler.compileCommand(getProviderType(), SCONTROL_COMMAND,
                context);
        final CommandResult commandResult = simpleCmdExecutor.execute(hostCommand);
        if (commandResult.getExitCode() != 0) {
            SgeCommandsUtils.throwExecutionDetails(commandResult);
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
