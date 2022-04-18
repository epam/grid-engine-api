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

package com.epam.grid.engine.provider.usage.sge;

import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.usage.UsageReport;
import com.epam.grid.engine.entity.usage.UsageReportFilter;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.SINGLETON_LIST_WITH_STANDARD_WARN;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(properties = {"grid.engine.type=SGE"})
public class SgeUsageProviderTest {

    private static final String QACCT_COMMAND = "qacct";
    private static final List<String> SGE_REPORT_VALID_OUTPUT = List.of("Total System Usage",
            "    WALLCLOCK       UTIME       STIME         CPU           MEMORY               IO              IOW",
            "====================================================================================================",
            "            5       0.628       0.377       1.005            0.000            0.000            0.000");
    private static final UsageReport expectedUsageReport = new UsageReport(5, 0.628, 0.377, 1.005, 0, 0, 0);

    @Autowired
    private SgeUsageProvider sgeUsageProvider;

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @Test
    public void shouldWorkCorrectWithNotEmptyErrorMassage() {
        final CommandResult commandResult = new CommandResult();
        commandResult.setStdOut(SGE_REPORT_VALID_OUTPUT);
        commandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);

        doReturn(commandResult).when(mockCmdExecutor).execute(QACCT_COMMAND);

        final UsageReport result = sgeUsageProvider.getUsageReport(new UsageReportFilter());
        Assertions.assertEquals(expectedUsageReport, result);
    }

    @Test
    public void shouldFailWithException() {
        final CommandResult commandResult = new CommandResult();
        commandResult.setExitCode(1);
        commandResult.setStdOut(EMPTY_LIST);
        commandResult.setStdErr(EMPTY_LIST);

        doReturn(commandResult).when(mockCmdExecutor).execute(QACCT_COMMAND);

        final Throwable thrown = Assertions.assertThrows(GridEngineException.class,
                () -> sgeUsageProvider.getUsageReport(new UsageReportFilter()));
        Assertions.assertNotNull(thrown.getMessage());
    }
}
