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

import com.epam.grid.engine.entity.usage.JobFilteredUsageReport;
import com.epam.grid.engine.entity.usage.UsageReport;
import com.epam.grid.engine.exception.GridEngineException;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.grid.engine.provider.utils.NumberParseUtils.convertHumanReadableMemoryToGbytes;
import static com.epam.grid.engine.provider.utils.NumberParseUtils.truncateDoubleValue;
import static com.epam.grid.engine.utils.TextConstants.NONE;
import static com.epam.grid.engine.utils.TextConstants.SPACE;

@NoArgsConstructor
public final class SgeJobAccountingDataParser extends SgeAccountingDataParser {

    private static final int FOOTER_LENGTH = 4;
    private static final int JOB_REPORT_LENGTH = 45;
    private static final int VALUES_PRECISION = 5;
    private static final String PARSING_ERROR = "Something went wrong while parsing SGE response";

    /**
     * This method parses SGE usage summary information and converts it into a {@link JobFilteredUsageReport} object.
     *
     * @param stdOut a List containing stdOut of CommandResult
     * @return the report object containing completely parsed usage summary information of matching jobs
     */
    @Override
    public UsageReport parseAccountingDataFromStdOut(final List<String> stdOut) {
        final int numberFooterLines = stdOut.size() % JOB_REPORT_LENGTH;
        if (stdOut.isEmpty() || !(numberFooterLines == 0 || numberFooterLines == FOOTER_LENGTH)) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, PARSING_ERROR);
        }

        final JobFilteredUsageReport report = JobFilteredUsageReport.builder()
                .matchingJobIds(new HashSet<>())
                .owners(new HashSet<>())
                .queues(new HashSet<>())
                .hosts(new HashSet<>())
                .parallelEnvs(new HashSet<>())
                .build();

        int wallClock = 0;
        double userTime = 0;
        double systemTime = 0;
        double cpuTime = 0;
        double memory = 0;
        double ioData = 0;
        double ioWaiting = 0;

        for (int i = 1; i < stdOut.size() - numberFooterLines; i += JOB_REPORT_LENGTH) {

            final EnumMap<SgeAccountingHeaders, String> accountingMap =
                    parseJobFilteredEntitiesToMap(stdOut.subList(i, i + JOB_REPORT_LENGTH - 1));

            report.getMatchingJobIds().add(Long.parseLong(accountingMap.get(SgeAccountingHeaders.JOB_ID)));
            report.getOwners().add(accountingMap.get(SgeAccountingHeaders.OWNER));
            report.getQueues().add(accountingMap.get(SgeAccountingHeaders.QUEUE));
            report.getHosts().add(accountingMap.get(SgeAccountingHeaders.HOST));

            wallClock += Integer.parseInt(removeLastChar(accountingMap.get(SgeAccountingHeaders.WALL_CLOCK)));
            userTime += Double.parseDouble(removeLastChar(accountingMap.get(SgeAccountingHeaders.USER_TIME)));
            systemTime += Double.parseDouble(removeLastChar(accountingMap.get(SgeAccountingHeaders.SYSTEM_TIME)));
            cpuTime += Double.parseDouble(removeLastChar(accountingMap.get(SgeAccountingHeaders.CPU_TIME)));
            memory += convertHumanReadableMemoryToGbytes(
                    removeLastChar(accountingMap.get(SgeAccountingHeaders.MEMORY)));
            ioData += convertHumanReadableMemoryToGbytes(accountingMap.get(SgeAccountingHeaders.IO_DATA));
            ioWaiting += Double.parseDouble(removeLastChar(accountingMap.get(SgeAccountingHeaders.IO_WAITING)));

            Optional.ofNullable(accountingMap.get(SgeAccountingHeaders.PARALLEL_ENV))
                    .filter(parallelEnv -> !parallelEnv.equals(NONE))
                    .ifPresent(report.getParallelEnvs()::add);
        }

        report.setWallClock(wallClock);
        report.setUserTime(truncateDoubleValue(userTime, VALUES_PRECISION));
        report.setSystemTime(truncateDoubleValue(systemTime, VALUES_PRECISION));
        report.setCpuTime(truncateDoubleValue(cpuTime, VALUES_PRECISION));
        report.setMemory(truncateDoubleValue(memory, VALUES_PRECISION));
        report.setIoData(truncateDoubleValue(ioData, VALUES_PRECISION));
        report.setIoWaiting(truncateDoubleValue(ioWaiting, VALUES_PRECISION));

        return report;
    }

    private EnumMap<SgeAccountingHeaders, String> parseJobFilteredEntitiesToMap(final List<String> entitiesTable) {
        return entitiesTable.stream()
                .map(line -> line.split(SPACE, 2))
                .filter(splitLine ->
                        SgeAccountingHeaders.valueOfFilteredJobReportField(splitLine[0].trim()).isPresent())
                .collect(Collectors.toMap(
                        splitLine -> SgeAccountingHeaders.valueOfFilteredJobReportField(splitLine[0].trim()).get(),
                        splitLine -> splitLine[1].trim(),
                        (first, second) -> {
                            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, PARSING_ERROR);
                        },
                        () -> new EnumMap<>(SgeAccountingHeaders.class))
                );
    }

    private String removeLastChar(final String str) {
        return str.substring(0, str.length() - 1);
    }
}
