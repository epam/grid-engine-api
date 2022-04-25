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

import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(properties = {"grid.engine.type=SLURM"})
public class SlurmHostProviderTest {

    private static final String WORKER1_OUT = "NodeName=worker1 Arch=x86_64 CoresPerSocket=1  CPUAlloc=0 CPUTot=1 "
            + "CPULoad=0.01 AvailableFeatures=(null) ActiveFeatures=(null) Gres=(null) NodeAddr=worker1 "
            + "NodeHostName=worker1  OS=Linux 5.10.16.3-microsoft-standard-WSL2 #1 SMP Fri Apr 2 22:23:49 UTC 2021  "
            + "RealMemory=1000 AllocMem=0 FreeMem=9936 Sockets=1 Boards=1 State=IDLE ThreadsPerCore=1 TmpDisk=0 "
            + "Weight=1 Owner=N/A MCS_label=N/A Partitions=normal  BootTime=2022-04-16T01:15:56 "
            + "SlurmdStartTime=2022-04-17T19:53:28 CfgTRES=cpu=1,mem=1000M,billing=1 AllocTRES= CapWatts=n/a "
            + "CurrentWatts=0 AveWatts=0 ExtSensorsJoules=n/s ExtSensorsWatts=0 ExtSensorsTemp=n/s";

    private static final String EMPTY_OUT = "NodeName= Arch= CoresPerSocket=  CPUAlloc= CPUTot= "
            + "CPULoad=0.01 AvailableFeatures=(null) ActiveFeatures=(null) Gres=(null) NodeAddr= "
            + "NodeHostName=  OS= "
            + "RealMemory= AllocMem= FreeMem= Sockets= Boards= State= ThreadsPerCore= TmpDisk= "
            + "Weight= Owner= MCS_label= Partitions=  BootTime= "
            + "SlurmdStartTime= CfgTRES=cpu=,mem=,billing= AllocTRES= CapWatts= "
            + "CurrentWatts= AveWatts= ExtSensorsJoules= ExtSensorsWatts= ExtSensorsTemp=";

    private static final String NOT_FOUND = "Node nodeName not found";
    private static final String NOT_FOUND_NEW_LINE = "Node nodeName not found\n";

    private static final String[] SCONTROL_COMMAND = {"scontrol", "-o", "show", "node"};

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @Autowired
    private SlurmHostProvider slurmHostProvider;

    @Test
    public void shouldReturnCorrectAnswer() {
        final Host slurmHost1 = Host.builder()
                .hostname("worker1")
                .typeOfArchitect("x86_64")
                .numOfProcessors(1)
                .numOfSocket(1)
                .numOfCore(1)
                .numOfThread(1)
                .memTotal(1000L)
                .memUsed(0L)
                .build();
        final List<String> worker1List = Collections.singletonList(WORKER1_OUT);
        final HostFilter hostFilter = new HostFilter();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(worker1List)
                .stdErr(EMPTY_LIST)
                .exitCode(0)
                .build();
        doReturn(commandResult).when(mockCmdExecutor).execute(SCONTROL_COMMAND);
        final Listing<Host> result = slurmHostProvider.listHosts(hostFilter);
        Assertions.assertEquals(slurmHost1, result.getElements().get(0));
    }

    @Test
    public void shouldReturnEmptyAnswer() {
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(EMPTY_LIST)
                .exitCode(1)
                .build();

        doReturn(commandResult).when(mockCmdExecutor).execute(SCONTROL_COMMAND);
        final HostFilter hostFilter = new HostFilter();
        final GridEngineException gridEngineException = Assertions.assertThrows(GridEngineException.class,
                () -> slurmHostProvider.listHosts(hostFilter));
        Assertions.assertNotNull(gridEngineException.getMessage());
    }

    @Test
    public void shouldReturnNotFound() {
        final HostFilter hostFilter = new HostFilter();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(Collections.singletonList(NOT_FOUND))
                .stdErr(EMPTY_LIST)
                .exitCode(1)
                .build();
        doReturn(commandResult).when(mockCmdExecutor).execute(SCONTROL_COMMAND);
        final GridEngineException gridEngineException = Assertions.assertThrows(GridEngineException.class,
                () -> slurmHostProvider.listHosts(hostFilter));
        Assertions.assertEquals(NOT_FOUND_NEW_LINE, gridEngineException.getMessage());
    }

    @Test
    public void shouldReturnNotFoundFromEmptyOutput() {
        final List<String> worker1List = Collections.singletonList(EMPTY_OUT);
        final HostFilter hostFilter = new HostFilter();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(worker1List)
                .stdErr(EMPTY_LIST)
                .exitCode(0)
                .build();
        doReturn(commandResult).when(mockCmdExecutor).execute(SCONTROL_COMMAND);
        final GridEngineException gridEngineException = Assertions.assertThrows(GridEngineException.class,
                () -> slurmHostProvider.listHosts(hostFilter));
        Assertions.assertNotNull(gridEngineException.getMessage());
    }

}
