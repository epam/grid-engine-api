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
import java.util.Set;

import static com.epam.grid.engine.utils.TextConstants.EMPTY_STRING;
import static com.epam.grid.engine.utils.TextConstants.SPACE;

/**
 * This class provides methods for handling SGE usage summary information when using filters.
 */
@NoArgsConstructor
public final class SgeSummaryFilteredAccountingDataParser extends SgeAccountingDataParser {

    /**
     * This method parses SGE usage summary information and converts it into a {@link FilteredUsageReport} object.
     *
     * @param stdOut a List containing stdOut of CommandResult
     * @return the report object containing completely parsed usage summary information.
     */
    @Override
    public UsageReport parseAccountingDataFromStdOut(final List<String> stdOut) {
        final SgeUsageRawOutput sgeUsageRawOutput = parseSgeAccountingOutput(stdOut);

        final EnumMap<SgeAccountingHeaders, String> accountingMap = new EnumMap<>(SgeAccountingHeaders.class);
        final String valuesLine = Optional.ofNullable(sgeUsageRawOutput.getAccountingData()).orElse(EMPTY_STRING);

        final List<String> headers = getHeaders(sgeUsageRawOutput);
        try {
            final String[] acctValues = parseResultValues(headers, valuesLine);
            for (int i = 0; i < headers.size(); i++) {
                accountingMap.put(SgeAccountingHeaders.valueOfName(headers.get(i))
                        .orElseThrow(IllegalArgumentException::new), acctValues[i]
                );
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new GridEngineException(HttpStatus.NOT_FOUND, "Cannot parse SGE output", e);
        }
        return buildFilteredUsageReportFromMap(headers, accountingMap);
    }

    private String[] parseResultValues(final List<String> headers, final String values) {
        final String[] acctValues = Arrays.stream(values.split(SPACE))
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
        if (acctValues.length == 0) {
            return fillEmptyValues(headers.size());
        }
        if (acctValues.length == headers.size()) {
            return acctValues;
        }
        if (headers.contains(SgeAccountingHeaders.QUEUE.name())) {
            return addEmptyClusterField(acctValues, headers.size());
        }
        throw new IllegalArgumentException();
    }

    private String[] addEmptyClusterField(final String[] values, final int headersSize) {
        final String[] newValues = new String[headersSize];
        System.arraycopy(values, 1, newValues, 2, values.length - 1);
        newValues[0] = values[0];
        return newValues;
    }

    private String[] fillEmptyValues(final int headersSize) {
        final String[] values = new String[headersSize];
        Arrays.fill(values, EMPTY_STRING);
        return values;
    }

    private FilteredUsageReport buildFilteredUsageReportFromMap(final List<String> headers,
                                                                final EnumMap<SgeAccountingHeaders, String> enumMap) {
        final FilteredUsageReport report = FilteredUsageReport.builder()
                .wallClock(Integer.parseInt(enumMap.get(SgeAccountingHeaders.WALL_CLOCK)))
                .userTime(Double.parseDouble(enumMap.get(SgeAccountingHeaders.USER_TIME)))
                .systemTime(Double.parseDouble(enumMap.get(SgeAccountingHeaders.SYSTEM_TIME)))
                .cpuTime(Double.parseDouble(enumMap.get(SgeAccountingHeaders.CPU_TIME)))
                .memory(Double.parseDouble(enumMap.get(SgeAccountingHeaders.MEMORY)))
                .ioData(Double.parseDouble(enumMap.get(SgeAccountingHeaders.IO_DATA)))
                .ioWaiting(Double.parseDouble(enumMap.get(SgeAccountingHeaders.IO_WAITING)))
                .build();
        if (headers.contains(SgeAccountingHeaders.OWNER.name())) {
            report.setOwners(Set.of(enumMap.get(SgeAccountingHeaders.OWNER)));
        }
        if (headers.contains(SgeAccountingHeaders.QUEUE.name())) {
            report.setHosts(Set.of(enumMap.get(SgeAccountingHeaders.HOST)));
            report.setCluster(enumMap.get(SgeAccountingHeaders.CLUSTER));
            report.setQueues(Set.of(enumMap.get(SgeAccountingHeaders.QUEUE)));
        }
        if (headers.contains(SgeAccountingHeaders.PARALLEL_ENV.name())) {
            report.setQueues(Set.of(enumMap.get(SgeAccountingHeaders.PARALLEL_ENV)));
        }
        return report;
    }
}
