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

import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.SINGLETON_LIST_WITH_STANDARD_WARN;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.TYPE_XML;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
public class SgeHostProviderTest {

    private static final String CORRECT_XML = "<?xml version='1.0'?>"
            + "<qhost xmlns:xsd=\"http://arc.liv.ac.uk/repos/darcs/sge/source/dist/util/resources/schemas/qhost/qhost.xsd\">\n"
            + "<host name='test-ip'>\n"
            + "<hostvalue name='arch_string'>lx-amd64</hostvalue>\n"
            + "<hostvalue name='num_proc'>2</hostvalue>\n"
            + "<hostvalue name='m_socket'>1</hostvalue>\n"
            + "<hostvalue name='m_core'>1</hostvalue>\n"
            + "<hostvalue name='m_thread'>2</hostvalue>\n"
            + "<hostvalue name='load_avg'>0.00</hostvalue>\n"
            + "<hostvalue name='mem_total'>3.6G</hostvalue>\n"
            + "<hostvalue name='mem_used'>311.6M</hostvalue>\n"
            + "<hostvalue name='swap_total'>0.0</hostvalue>\n"
            + "<hostvalue name='swap_used'>0.0</hostvalue>\n"
            + "</host>\n"
            + "</qhost>";

    private static final String EMPTY_XML = "<?xml version='1.0'?>"
            + "<qhost xmlns:xsd=\"http://arc.liv.ac.uk/repos/darcs/sge/source/dist/util/resources/schemas/qhost/qhost.xsd\">\n"
            + "<host name=''>\n"
            + "<hostvalue/>\n"
            + " <hostvalue/>\n"
            + "<hostvalue/>\n"
            + "<hostvalue/>\n"
            + "<hostvalue/>\n"
            + "<hostvalue/>\n"
            + "<hostvalue/>\n"
            + "<hostvalue/>\n"
            + "<hostvalue/>\n"
            + "<hostvalue/>\n"
            + "</host>\n"
            + "</qhost>";

    private static final String INCORRECT_XML = "<?xml version='1.0'?>"
            + "<qhost xmlns:xsd=\"http://arc.liv.ac.uk/repos/darcs/sge/source/dist/util/resources/schemas/qhost/qhost.xsd\">\n"
            + "<host name='test'>\n" + "<hostvalue name='arch_string'>dv</hostvalue>\n"
            + "<hostvalue name='num_proc'>sff </hostvalue>\n"
            + "<hostvalue name='m_socket'>dv bg </hostvalue>\n"
            + "<hostvalue name='m_core'>kk</hostvalue>\n"
            + "<hostvalue name='m_thread'>iuy</hostvalue>\n"
            + "<hostvalue name='load_avg'>jk,o</hostvalue>\n"
            + "<hostvalue name='mem_total'>j,olo</hostvalue>\n"
            + "<hostvalue name='mem_used'>yui</hostvalue>\n"
            + "<hostvalue name='swap_total'>jmnb</hostvalue>\n"
            + "<hostvalue name='swap_used'>olo,</hostvalue>\n"
            + "</host>\n"
            + "</qhost>";

    private static final String QHOST_COMMAND = "qhost";
    private final CommandResult commandResult = new CommandResult();

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @Autowired
    private SgeHostProvider sgeHostProvider;

    @Test
    public void shouldReturnCorrectXml() {
        final Host expectedHost = Host.builder()
                .hostname("test-ip")
                .typeOfArchitect("lx-amd64")
                .numOfProcessors(2)
                .numOfSocket(1)
                .numOfCore(1)
                .numOfThread(2)
                .load(0.0)
                .memTotal(3600000000L)
                .memUsed(311600000L)
                .totalSwapSpace(0.0)
                .usedSwapSpace(0.0)
                .build();
        final List<String> hostList = Collections.singletonList(CORRECT_XML);
        commandResult.setStdOut(hostList);
        commandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);
        doReturn(commandResult).when(mockCmdExecutor).execute(QHOST_COMMAND, TYPE_XML);
        final Listing<Host> result = sgeHostProvider.listHosts(new HostFilter());
        Assertions.assertEquals(expectedHost, result.getElements().get(0));
    }

    @Test
    public void shouldReturnEmptyInput() {
        final List<String> hostList = Collections.singletonList(EMPTY_XML);
        commandResult.setStdOut(hostList);
        commandResult.setStdErr(EMPTY_LIST);
        doReturn(commandResult).when(mockCmdExecutor).execute(QHOST_COMMAND, TYPE_XML);
        final HostFilter hostFilter = new HostFilter();
        Assertions.assertThrows(IllegalStateException.class,
                () -> sgeHostProvider.listHosts(hostFilter));
    }

    @Test
    public void shouldThrowsExceptionWithIncorrectInput() {
        final List<String> hostList = Collections.singletonList(INCORRECT_XML);
        commandResult.setStdOut(hostList);
        commandResult.setStdErr(EMPTY_LIST);
        doReturn(commandResult).when(mockCmdExecutor).execute(QHOST_COMMAND, TYPE_XML);
        final HostFilter hostFilter = new HostFilter();
        Assertions.assertThrows(GridEngineException.class,
                () -> sgeHostProvider.listHosts(hostFilter));
    }

    @Test
    public void shouldFailWithException() {
        final HostFilter hostFilter = new HostFilter();
        final CommandResult commandResult = CommandResult.builder()
                .stdOut(EMPTY_LIST)
                .stdErr(EMPTY_LIST)
                .exitCode(1)
                .build();
        doReturn(commandResult).when(mockCmdExecutor).execute(QHOST_COMMAND, TYPE_XML);
        final Throwable thrown = Assertions.assertThrows(GridEngineException.class,
                () -> sgeHostProvider.listHosts(hostFilter));
        Assertions.assertNotNull(thrown.getMessage());
    }
}
