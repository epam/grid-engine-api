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

package com.epam.grid.engine.service;

import com.epam.grid.engine.entity.usage.UsageReport;
import com.epam.grid.engine.entity.usage.UsageReportFilter;
import com.epam.grid.engine.provider.usage.sge.SgeUsageProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

@SpringBootTest
public class UsageOperationProviderServiceTest {

    @Autowired
    UsageOperationProviderService usageOperationProviderService;

    @SpyBean
    SgeUsageProvider usageProvider;

    @Test
    public void shouldReturnCorrectResponse() {

        final UsageReport correctReport = reportFill(usageParser());

        doReturn(correctReport).when(usageProvider).getUsageReport(new UsageReportFilter());
        Assertions.assertEquals(correctReport, usageOperationProviderService.getUsageReport(new UsageReportFilter()));
        Mockito.verify(usageProvider, times(1)).getUsageReport(new UsageReportFilter());
    }

    private UsageReport reportFill(final UsageReport usageReport) {
        return UsageReport.builder()
                .wallClock(usageReport.getWallClock())
                .userTime(usageReport.getUserTime())
                .systemTime(usageReport.getSystemTime())
                .cpuTime(usageReport.getCpuTime())
                .memory(usageReport.getMemory())
                .ioData(usageReport.getIoData())
                .ioWaiting(usageReport.getIoWaiting())
                .build();
    }

    private UsageReport usageParser() {
        return UsageReport.builder()
                .wallClock(1)
                .cpuTime(2.0)
                .ioData(3.00)
                .ioWaiting(4.0)
                .memory(5.0)
                .systemTime(6.0)
                .userTime(7.0)
                .build();
    }
}
