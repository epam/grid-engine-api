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

package com.epam.grid.engine.provider.utils.slurm.job;

import com.epam.grid.engine.entity.job.slurm.SlurmJob;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.utils.slurm.DateUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.grid.engine.utils.TextConstants.COMMA;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SacctCommandParser {

    private static final String STANDARD_SLURM_DELIMETER = "\\|";
    private static final String FAILED_TO_PARSE_JOB_DATA = "failed to parse job data";
    private static final String SLURM_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static List<String> parseSlurmJob(final String jobString) {
        final String[] jobArray = jobString.split(STANDARD_SLURM_DELIMETER);
        if (jobArray.length != 52) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, "An error occurred during job data parsing");

        }
        return Arrays.stream(jobArray).collect(Collectors.toList());
    }

    public static SlurmJob mapJobDataToSlurmJob(final List<String> jobData) {
        try {
            return SlurmJob.builder()
                    .account(jobData.get(0))
                    .tresPerNode(jobData.get(1))
                    .minCpus(Integer.parseInt(jobData.get(2)))
                    .minTmpDisk(Integer.parseInt(jobData.get(3)))
                    .endTime(!jobData.get(4).equals("N/A")
                            ? DateUtils.tryParseStringToLocalDateTime(jobData.get(4),
                            DateTimeFormatter.ofPattern(SLURM_DATE_FORMAT))
                            : null)
                    .features(jobData.get(5))
                    .groupName(jobData.get(6))
                    .overSubscribe(jobData.get(7))
                    .jobId(Integer.parseInt(jobData.get(8)))
                    .name(jobData.get(9))
                    .comment(jobData.get(10))
                    .timelimit(jobData.get(11))
                    .minMemory(jobData.get(12))
                    .reqNodes(jobData.get(13))
                    .command(jobData.get(14))
                    .priority(Double.parseDouble(jobData.get(15)))
                    .qos(jobData.get(16))
                    .reason(jobData.get(17))
                    .stateCompact(jobData.get(19))
                    .userName(jobData.get(20))
                    .reservation(jobData.get(21))
                    .wckey(jobData.get(22))
                    .excNodes(jobData.get(23))
                    .nice(jobData.get(24))
                    .sct(jobData.get(25))
                    .execHost(jobData.get(26))
                    .account(jobData.get(27))
                    .cpus(Integer.parseInt(jobData.get(28)))
                    .nodes(Integer.parseInt(jobData.get(29)))
                    .dependency(jobData.get(30))
                    .arrayJobId(Integer.parseInt(jobData.get(31)))
                    .groupId(Integer.parseInt(jobData.get(32)))
                    .socketsPerNode(jobData.get(33))
                    .coresPerNode(jobData.get(34))
                    .threadsPerSocket(jobData.get(35))
                    .arrayTaskId(jobData.get(36))
                    .timeLeft(jobData.get(37))
                    .timeUsed(jobData.get(38))
                    .nodeList(parseListFromString(jobData.get(39)))
                    .contiguous(Integer.parseInt(jobData.get(40)))
                    .partition(jobData.get(41))
                    .priorityLong(Long.parseLong(jobData.get(42)))
                    .nodeListReason(parseListFromString(jobData.get(43)))
                    .startTime(!jobData.get(44).equals("N/A")
                            ? DateUtils.tryParseStringToLocalDateTime(jobData.get(44),
                            DateTimeFormatter.ofPattern(SLURM_DATE_FORMAT))
                            : null)
                    .state(jobData.get(45))
                    .uid(Integer.parseInt(jobData.get(46)))
                    .submissionTime(!jobData.get(47).equals("N/A")
                            ? DateUtils.tryParseStringToLocalDateTime(jobData.get(47),
                            DateTimeFormatter.ofPattern(SLURM_DATE_FORMAT))
                            : null)
                    .licenses(jobData.get(48))
                    .coreSpec(jobData.get(49))
                    .schedNodes(jobData.get(50))
                    .workDir(jobData.get(51))
                    .build();
        } catch (final NumberFormatException ex) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR,
                    FAILED_TO_PARSE_JOB_DATA, ex);
        }
    }

    private static List<String> parseListFromString(final String stringList) {
        return List.of(stringList.split(COMMA));
    }

}
