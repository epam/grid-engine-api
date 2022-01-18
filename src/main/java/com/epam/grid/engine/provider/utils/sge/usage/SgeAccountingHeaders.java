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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This enumeration contains a list of fields used when creating a usage report.
 */
public enum SgeAccountingHeaders {

    OWNER("OWNER", "owner"),
    HOST("HOST", "hostname"),
    CLUSTER("CLUSTER", null),
    QUEUE("QUEUE", "qname"),
    PARALLEL_ENV("PARALLELENV", "granted_pe"),
    WALL_CLOCK("WALLCLOCK", "ru_wallclock"),
    USER_TIME("UTIME", "ru_utime"),
    SYSTEM_TIME("STIME", "ru_stime"),
    CPU_TIME("CPU", "cpu"),
    MEMORY("MEMORY", "mem"),
    IO_DATA("IO", "io"),
    IO_WAITING("IOW", "iow"),
    JOB_ID(null, "jobnumber");

    private static final Map<String, SgeAccountingHeaders> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(value -> value.name, Function.identity()));

    private static final Map<String, SgeAccountingHeaders> BY_FILTERED_JOB_REPORT = Arrays.stream(values())
            .collect(Collectors.toMap(value -> value.filteredJobReportField, Function.identity()));

    /**
     * The field name of the usage report received from the response of qacct command without j-key.
     */
    private final String name;

    /**
     * The field name of the usage report received from the response of qacct command with j-key.
     * To get more information see <a href="http://gridscheduler.sourceforge.net/htmlman/htmlman5/accounting.html">
     * Sun Grid Engine accounting file format</a>.
     */
    private final String filteredJobReportField;

    SgeAccountingHeaders(final String name, final String filteredJobReportField) {
        this.name = name;
        this.filteredJobReportField = filteredJobReportField;
    }

    /**
     * The method returns the enumeration value by the field name of the usage report.
     *
     * @param name The field name of the usage report.
     * @return The enumeration value.
     */
    public static Optional<SgeAccountingHeaders> valueOfName(final String name) {
        return Optional.ofNullable(BY_NAME.get(name));
    }

    /**
     * The method returns the enumeration value by the field name of usage response
     * received from qacct command with j-key.
     *
     * @param  name The field name of the usage report.
     * @return The enumeration value.
     */
    public static Optional<SgeAccountingHeaders> valueOfFilteredJobReportField(final String name) {
        return Optional.ofNullable(BY_FILTERED_JOB_REPORT.get(name));
    }
}
