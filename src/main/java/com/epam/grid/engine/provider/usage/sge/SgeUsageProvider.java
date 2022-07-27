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

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.usage.UsageReport;
import com.epam.grid.engine.entity.usage.UsageReportFilter;
import com.epam.grid.engine.provider.usage.UsageProvider;
import com.epam.grid.engine.provider.utils.sge.usage.SgeAccountingDataParser;
import com.epam.grid.engine.provider.utils.sge.usage.SgeJobAccountingDataParser;
import com.epam.grid.engine.provider.utils.sge.usage.SgeSummaryAccountingDataParser;
import com.epam.grid.engine.provider.utils.CommandsUtils;
import com.epam.grid.engine.provider.utils.sge.usage.SgeSummaryFilteredAccountingDataParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A {@link UsageProvider} implementation for Sun Grid Engine queuing system.
 * More information about SGE qacct command:
 * <p><a href="http://gridscheduler.sourceforge.net/htmlman/htmlman1/qacct.html">
 * http://gridscheduler.sourceforge.net/htmlman/htmlman1/qacct.html</a>
 */
@Service
@AllArgsConstructor
@Slf4j
public class SgeUsageProvider implements UsageProvider {

    private static final String QACCT_COMMAND = "qacct";
    private static final String DATE_TIME_PATTERN = "yyyyMMddHHmm.ss";
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";

    /**
     * The executor that provide the ability to call any command available in the current environment.
     */
    private final SimpleCmdExecutor simpleCmdExecutor;

    /**
     * An object that forms the structure of an executable command according to a template.
     */
    private final GridEngineCommandCompiler commandCompiler;

    /**
     * This method gets SGE usage summary information as an {@link UsageReport} object from SGE
     * with filters applied.
     *
     * @param filter List of keys for setting filters.
     * @return the report object containing usage summary information.
     */
    @Override
    public UsageReport getUsageReport(final UsageReportFilter filter) {
        final Context context = new Context();
        context.setVariable("filter", filter);

        Optional.ofNullable(filter.getStartTime())
                .ifPresent(startTime -> context.setVariable(START_TIME, parseTime(startTime)));

        Optional.ofNullable(filter.getEndTime())
                .ifPresent(endTime -> context.setVariable(END_TIME, parseTime(endTime)));

        final CommandResult commandResult = simpleCmdExecutor.execute(commandCompiler.compileCommand(
                getProviderType(), QACCT_COMMAND, context));

        if (commandResult.getExitCode() != 0) {
            CommandsUtils.throwExecutionDetails(commandResult);
        } else if (!commandResult.getStdErr().isEmpty()) {
            log.warn("Standard error while sge qacct command was executed: {}", commandResult.getStdErr());
        }
        final SgeAccountingDataParser parser = selectSgeAccountingDataParser(filter);
        return parser.parseAccountingDataFromStdOut(commandResult.getStdOut());
    }

    /**
     * This method provides information about the current provider engine type.
     *
     * @return the engine type
     */
    @Override
    public CommandType getProviderType() {
        return CommandType.SGE;
    }

    private String parseTime(final LocalDateTime dateTime) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        return formatter.format(dateTime);
    }

    private SgeAccountingDataParser selectSgeAccountingDataParser(final UsageReportFilter options) {
        if (options.getJobIdOrName() != null) {
            return new SgeJobAccountingDataParser();
        }
        return requestIsEmpty(options)
                ? new SgeSummaryAccountingDataParser()
                : new SgeSummaryFilteredAccountingDataParser();
    }

    /**
     * This method checks whether the user conditions are set.
     *
     * @param options An object that stores a list of user conditions.
     * @return The result of checking the user's conditions.
     */
    private boolean requestIsEmpty(final UsageReportFilter options) {
        return options.getEndTime() == null
                && options.getStartTime() == null
                && options.getDays() == null
                && Stream.of(options.getOwner(),
                        options.getParallelEnv(),
                        options.getQueue(),
                        options.getJobIdOrName())
                .noneMatch(StringUtils::hasText);
    }
}
