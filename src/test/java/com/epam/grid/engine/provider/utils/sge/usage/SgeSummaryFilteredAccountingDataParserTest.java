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

package com.epam.grid.engine.provider.utils.sge.usage;

import com.epam.grid.engine.entity.usage.FilteredUsageReport;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class SgeSummaryFilteredAccountingDataParserTest {

    private static final String DELIMITER_LINE = "==================================================================="
            + "=============================================";

    private static final String VALID_HEADER = "    WALLCLOCK         UTIME         STIME           CPU"
            + "             MEMORY                 IO                IOW";
    private static final String VALID_DATA = "            5         0.628         0.377         1.005              "
            + "0.000              0.000              0.000";

    private static final String FILTER_BY_OWNER_HEADER = "OWNER       WALLCLOCK         UTIME         STIME          "
            + " CPU             MEMORY                 IO                IOW";
    private static final String FILTER_BY_OWNER_DATA = "sgeuser            11         0.409         0.260"
            + "         0.669              0.002              0.000              0.000";

    private static final String FILTER_BY_QUEUE_HEADER = "HOST         CLUSTER QUEUE     WALLCLOCK         UTIME     "
            + "    STIME           CPU             MEMORY                 IO                IOW";
    private static final String FILTER_BY_QUEUE_DATA = "ee5be618a23e all.q                    11         0.409       "
            + "  0.260         0.669              0.002              0.000              0.000";

    private static final String FILTER_BY_QUEUE_OWNER_HEADER = "HOST         CLUSTER QUEUE OWNER       WALLCLOCK     "
            + "    UTIME         STIME           CPU             MEMORY                 IO                IOW";
    private static final String FILTER_BY_QUEUE_OWNER_DATA = "ee5be618a23e all.q         sgeuser            11       "
            + "  0.409         0.260         0.669              0.002              0.000              0.000";

    private static final String SGEUSER = "sgeuser";

    @Test
    public void shouldReturnValidReportWhenParseValidData() {
        Assertions.assertEquals(getFilteredUsageReport(5, 0.628, 0.377, 1.005, 0, 0, 0),
                new SgeSummaryFilteredAccountingDataParser().parseAccountingDataFromStdOut(
                        createStdOutRawData(VALID_HEADER, VALID_DATA))
        );
    }

    @Test
    public void shouldReturnReportWithOwnerWhenParseDataWithFilterByOwner() {
        final FilteredUsageReport report = getFilteredUsageReport(11, 0.409, 0.260, 0.669, 0.002, 0.000, 0.000);
        report.setOwners(Set.of(SGEUSER));
        Assertions.assertEquals(report, new SgeSummaryFilteredAccountingDataParser().parseAccountingDataFromStdOut(
                createStdOutRawData(FILTER_BY_OWNER_HEADER, FILTER_BY_OWNER_DATA))
        );
    }

    @Test
    public void shouldReturnReportWithQueueHostClusterFieldsWhenParseDataWithFilterByQueue() {
        final FilteredUsageReport report = getFilteredUsageReport(11, 0.409, 0.260, 0.669, 0.002, 0.000, 0.000);
        report.setQueues(Set.of("all.q"));
        report.setHosts(Set.of("ee5be618a23e"));
        Assertions.assertEquals(report, new SgeSummaryFilteredAccountingDataParser().parseAccountingDataFromStdOut(
                createStdOutRawData(FILTER_BY_QUEUE_HEADER, FILTER_BY_QUEUE_DATA))
        );
    }

    @Test
    public void shouldReturnReportWithQueueHostClusterOwnerFieldsWhenParseDataWithFilterByQueueAndOwner() {
        final FilteredUsageReport report = getFilteredUsageReport(11, 0.409, 0.260, 0.669, 0.002, 0.000, 0.000);
        report.setOwners(Set.of(SGEUSER));
        report.setQueues(Set.of("all.q"));
        report.setHosts(Set.of("ee5be618a23e"));
        Assertions.assertEquals(report, new SgeSummaryFilteredAccountingDataParser().parseAccountingDataFromStdOut(
                createStdOutRawData(FILTER_BY_QUEUE_OWNER_HEADER, FILTER_BY_QUEUE_OWNER_DATA))
        );
    }

    @Test
    public void shouldThrownExceptionWhenParseDataWithFilterByWrongOwner() {
        Assertions.assertThrows(GridEngineException.class, () -> new SgeSummaryFilteredAccountingDataParser()
                .parseSgeAccountingOutput(List.of(FILTER_BY_OWNER_HEADER, DELIMITER_LINE)));
    }

    @Test
    public void shouldThrownExceptionWhenParseDataWithFilterByWrongQueue() {
        Assertions.assertThrows(GridEngineException.class, () -> new SgeSummaryFilteredAccountingDataParser()
                .parseSgeAccountingOutput(List.of(FILTER_BY_QUEUE_HEADER, DELIMITER_LINE)));
    }

    private FilteredUsageReport getFilteredUsageReport(final int wallClock,
                                                       final double userTime,
                                                       final double systemTime,
                                                       final double cpuTime,
                                                       final double memory,
                                                       final double ioData,
                                                       final double ioWaiting) {
        return FilteredUsageReport.builder()
                .wallClock(wallClock)
                .userTime(userTime)
                .systemTime(systemTime)
                .cpuTime(cpuTime)
                .memory(memory)
                .ioData(ioData)
                .ioWaiting(ioWaiting)
                .build();
    }

    private List<String> createStdOutRawData(final String header, final String values) {
        return List.of(header, DELIMITER_LINE, values);
    }

}
