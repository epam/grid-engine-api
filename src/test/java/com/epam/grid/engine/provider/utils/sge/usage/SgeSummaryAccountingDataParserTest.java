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

import com.epam.grid.engine.entity.usage.UsageReport;
import com.epam.grid.engine.entity.usage.sge.SgeUsageRawOutput;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SgeSummaryAccountingDataParserTest {

    private static final String DELIMITER_LINE = "==================================================================="
            + "=============================================";

    private static final String VALID_HEADER = "    WALLCLOCK       UTIME       STIME          CPU          MEMORY"
            + "          IO            IOW";
    private static final String VALID_DATA = " 5          0.628       0.377       1.005            0.000          "
            + "0.000        0.000";

    private static final String SGE_REPORT_WITHOUT_CPU_VAL = " 5          0.628       0.377                       "
            + "0.000          0.000        0.000";

    private static final String UNSUPPORTED_HEADER =
            "    WALLCLOCK       UTIME       STIME        SPEED          MEMORY          IO            IOW";
    private static final String UNSUPPORTED_HEADER_DATA =
            " 5          0.628       0.377        1.005          0.000          0.000        0.000";

    private static final List<String> REPORT_WITHOUT_INFORMATION =
            List.of("Total System Usage",
                    "    WALLCLOCK       UTIME       STIME          CPU          MEMORY          IO            IOW",
                    DELIMITER_LINE);

    private static final List<String> REPORT_WITH_INFORMATION =
            List.of("Total System Usage",
                    VALID_HEADER,
                    DELIMITER_LINE,
                    VALID_DATA);

    private static final SgeUsageRawOutput SGE_REPORT_VALID_OUTPUT = SgeUsageRawOutput.builder()
            .header(VALID_HEADER)
            .accountingData(VALID_DATA)
            .build();


    @Test
    public void shouldReturnValidReportWhenParseValidData() {
        Assertions.assertEquals(getUsageReport(), new SgeSummaryAccountingDataParser().parseAccountingDataFromStdOut(
                createStdOutRawData(VALID_HEADER, VALID_DATA))
        );
    }

    @Test
    public void shouldThrowExceptionWhenParseSgeAccountingWithoutCpuVal() {
        Assertions.assertThrows(GridEngineException.class,
                () -> new SgeSummaryAccountingDataParser().parseAccountingDataFromStdOut(
                        createStdOutRawData(VALID_HEADER, SGE_REPORT_WITHOUT_CPU_VAL))
        );
    }

    @Test
    public void shouldThrowExceptionWhenParseSgeAccountingWithUnsupportedHeader() {
        Assertions.assertThrows(GridEngineException.class, () -> new SgeSummaryAccountingDataParser()
                .parseAccountingDataFromStdOut(createStdOutRawData(UNSUPPORTED_HEADER, UNSUPPORTED_HEADER_DATA))
        );
    }

    @Test
    public void sholdThrowExceptionBecauseNoInformation() {
        Assertions.assertThrows(GridEngineException.class,
                () -> new SgeSummaryAccountingDataParser().parseSgeAccountingOutput(REPORT_WITHOUT_INFORMATION));
    }

    @Test
    public void shouldParseToCorrectRawOutput() {
        Assertions.assertEquals(SGE_REPORT_VALID_OUTPUT,
                new SgeSummaryAccountingDataParser().parseSgeAccountingOutput(REPORT_WITH_INFORMATION));
    }

    private UsageReport getUsageReport() {
        return new UsageReport(5, 0.628, 0.377, 1.005, 0, 0, 0);
    }

    private List<String> createStdOutRawData(final String header, final String values) {
        return List.of(header, DELIMITER_LINE, values);
    }

}
