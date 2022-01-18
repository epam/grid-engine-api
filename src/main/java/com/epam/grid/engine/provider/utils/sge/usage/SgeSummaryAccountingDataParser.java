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
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;
import static com.epam.grid.engine.utils.TextConstants.SPACE;

/**
 * This class provides methods for handling SGE usage summary information when filters are not used.
 */
@NoArgsConstructor
public final class SgeSummaryAccountingDataParser extends SgeAccountingDataParser {

    private static final Double DEFAULT_VALUE = 0.0;

    /**
     * This method parses SGE usage summary information and converts it into a {@link UsageReport} object.
     *
     * @param stdOut a List containing stdOut of CommandResult
     * @return the report object containing completely parsed usage summary information
     */
    @Override
    public UsageReport parseAccountingDataFromStdOut(final List<String> stdOut) {
        final SgeUsageRawOutput sgeUsageRawOutput = parseSgeAccountingOutput(stdOut);

        final EnumMap<SgeAccountingHeaders, Double> accountingMap = new EnumMap<>(SgeAccountingHeaders.class);
        final String valuesLine = Optional.ofNullable(sgeUsageRawOutput.getAccountingData()).orElse(EMPTY_STRING);

        final List<String> headers = getHeaders(sgeUsageRawOutput);
        try {
            final Double[] acctValues = parseResultValues(headers, valuesLine);
            for (int i = 0; i < headers.size(); i++) {
                accountingMap.put(SgeAccountingHeaders.valueOfName(headers.get(i))
                        .orElseThrow(IllegalArgumentException::new), acctValues[i]
                );
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new GridEngineException(HttpStatus.NOT_FOUND, "Cannot parse SGE output", e);
        }

        return UsageReport.builder()
                .wallClock(accountingMap.get(SgeAccountingHeaders.WALL_CLOCK).intValue())
                .userTime(accountingMap.get(SgeAccountingHeaders.USER_TIME))
                .systemTime(accountingMap.get(SgeAccountingHeaders.SYSTEM_TIME))
                .cpuTime(accountingMap.get(SgeAccountingHeaders.CPU_TIME))
                .memory(accountingMap.get(SgeAccountingHeaders.MEMORY))
                .ioData(accountingMap.get(SgeAccountingHeaders.IO_DATA))
                .ioWaiting(accountingMap.get(SgeAccountingHeaders.IO_WAITING))
                .build();
    }

    private Double[] parseResultValues(final List<String> headers, final String values) {
        Double[] acctValues = Arrays.stream(values.split(SPACE))
                .filter(StringUtils::hasText)
                .map(Double::parseDouble)
                .toArray(Double[]::new);
        if (acctValues.length == 0) {
            acctValues = new Double[headers.size()];
            Arrays.fill(acctValues, DEFAULT_VALUE);
        }
        if (acctValues.length == headers.size()) {
            return acctValues;
        }
        throw new IllegalArgumentException();
    }
}
