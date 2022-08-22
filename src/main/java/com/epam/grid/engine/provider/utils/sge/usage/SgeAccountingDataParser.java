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
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.epam.grid.engine.utils.TextConstants.SPACE;

/**
 * This class provides methods for handling SGE usage summary information.
 */
public abstract class SgeAccountingDataParser {

    private static final String HORIZONTAL_DELIMITER_PATTERN = "=+";
    private static final Integer SGE_OUTPUTS_MIN_LENGTH = 4;
    private static final Integer SGE_OUTPUTS_MIN_LENGTH_WITH_FILTER = 3;
    private static final String SGE_OUTPUT_HEADER = "Total System Usage";

    /**
     * This method parses SGE usage summary information and converts it into a {@link UsageReport} object.
     *
     * @param stdOut - a List containing stdOut of SGE CommandResult
     * @return the report object containing completely parsed usage summary information
     */
    public abstract UsageReport parseAccountingDataFromStdOut(final List<String> stdOut);

    /**
     * This method gets the headers from SGE output.
     *
     * @param sgeUsageRawOutput An object containing summary information about usage.
     * @return A list of headers from the SGE output.
     */
    public List<String> getHeaders(final SgeUsageRawOutput sgeUsageRawOutput) {
        final String headerLine = Optional.ofNullable(sgeUsageRawOutput.getHeader())
                .orElseThrow(() -> new GridEngineException(HttpStatus.NOT_FOUND, "Empty SGE output"));
        return Arrays.stream(headerLine.split(SPACE)).filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * This method parses SGE "qacct" command output containing usage summary information
     * and splits it into a header and a data line.
     * More information about SGE report content:
     * <p><a href="http://gridscheduler.sourceforge.net/htmlman/htmlman5/accounting.html">
     * http://gridscheduler.sourceforge.net/htmlman/htmlman1/qacct.html</a>
     *
     * @param stdOut SGE output as a list of lines
     * @return an object containing a header and a data line
     * @see SgeUsageRawOutput
     */
    public SgeUsageRawOutput parseSgeAccountingOutput(final List<String> stdOut) {
        validateSgeOutputs(stdOut);
        return IntStream
                .range(0, stdOut.size())
                .filter(i -> stdOut.get(i).matches(HORIZONTAL_DELIMITER_PATTERN))
                .boxed()
                .findFirst()
                .map(i -> SgeUsageRawOutput.builder()
                        .header(stdOut.get(i - 1))
                        .accountingData(stdOut.get(i + 1))
                        .build())
                .orElseThrow(() -> new GridEngineException(HttpStatus.NOT_FOUND,
                        "Sge qacct output does not contains delimiter line!"));
    }

    private void validateSgeOutputs(final List<String> stdOut) {
        if (stdOut.size() < SGE_OUTPUTS_MIN_LENGTH
                && stdOut.get(0) != null
                && stdOut.get(0).equals(SGE_OUTPUT_HEADER)
                || stdOut.size() < (SGE_OUTPUTS_MIN_LENGTH_WITH_FILTER)) {
            throw new GridEngineException(HttpStatus.NOT_FOUND, "SGE has not provided usage information yet");
        }
    }
}
