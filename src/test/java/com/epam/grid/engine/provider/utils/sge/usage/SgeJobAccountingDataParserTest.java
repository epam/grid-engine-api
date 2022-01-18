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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SgeJobAccountingDataParserTest {

    private static final String oneJobAndFooterReport =
            "==============================================================\n"
            + "qname        all.q\n"
            + "hostname     e58e7973af6e\n"
            + "group        sgeuser\n"
            + "owner        sgeuser\n"
            + "project      NONE\n"
            + "department   defaultdepartment\n"
            + "jobname      simple.sh\n"
            + "jobnumber    1\n"
            + "taskid       undefined\n"
            + "account      sge\n"
            + "priority     0\n"
            + "qsub_time    Thu Jan 13 15:19:31 2022\n"
            + "start_time   Thu Jan 13 15:19:39 2022\n"
            + "end_time     Thu Jan 13 15:20:00 2022\n"
            + "granted_pe   NONE\n"
            + "slots        1\n"
            + "failed       0\n"
            + "exit_status  0\n"
            + "ru_wallclock 21s\n"
            + "ru_utime     0.321s\n"
            + "ru_stime     0.208s\n"
            + "ru_maxrss    7.020KB\n"
            + "ru_ixrss     0.000B\n"
            + "ru_ismrss    0.000B\n"
            + "ru_idrss     0.000B\n"
            + "ru_isrss     0.000B\n"
            + "ru_minflt    5859\n"
            + "ru_majflt    0\n"
            + "ru_nswap     0\n"
            + "ru_inblock   0\n"
            + "ru_oublock   16\n"
            + "ru_msgsnd    0\n"
            + "ru_msgrcv    0\n"
            + "ru_nsignals  0\n"
            + "ru_nvcsw     160\n"
            + "ru_nivcsw    8\n"
            + "cpu          0.529s\n"
            + "mem          162.529KBs\n"
            + "io           0.000B\n"
            + "iow          0.000s\n"
            + "maxvmem      3.176MB\n"
            + "arid         undefined\n"
            + "ar_sub_time  undefined\n"
            + "category     -U arusers\n"
            + "Total System Usage\n"
            + "    WALLCLOCK       UTIME       STIME         CPU           MEMORY               IO              IOW\n"
            + "====================================================================================================\n"
            + "           21       0.321       0.208       0.529            0.000            0.000            0.000";

    private static final String twoJobsReport =
            "==============================================================\n"
            + "qname        all.q\n"
            + "hostname     4de74d46ed4b\n"
            + "group        sgeuser\n"
            + "owner        sgeuser\n"
            + "project      NONE\n"
            + "department   defaultdepartment\n"
            + "jobname      simple.sh\n"
            + "jobnumber    1\n"
            + "taskid       undefined\n"
            + "account      sge\n"
            + "priority     0\n"
            + "qsub_time    Thu Jan 13 07:43:12 2022\n"
            + "start_time   Thu Jan 13 07:43:20 2022\n"
            + "end_time     Thu Jan 13 07:43:41 2022\n"
            + "granted_pe   make\n"
            + "slots        1\n"
            + "failed       0\n"
            + "exit_status  0\n"
            + "ru_wallclock 21s\n"
            + "ru_utime     0.329s\n"
            + "ru_stime     0.214s\n"
            + "ru_maxrss    7.023KB\n"
            + "ru_ixrss     0.000B\n"
            + "ru_ismrss    0.000B\n"
            + "ru_idrss     0.000B\n"
            + "ru_isrss     0.000B\n"
            + "ru_minflt    5617\n"
            + "ru_majflt    4\n"
            + "ru_nswap     0\n"
            + "ru_inblock   80\n"
            + "ru_oublock   24\n"
            + "ru_msgsnd    0\n"
            + "ru_msgrcv    0\n"
            + "ru_nsignals  0\n"
            + "ru_nvcsw     161\n"
            + "ru_nivcsw    10\n"
            + "cpu          0.544s\n"
            + "mem          1.629MBs\n"
            + "io           0.000B\n"
            + "iow          0.000s\n"
            + "maxvmem      6.508MB\n"
            + "arid         undefined\n"
            + "ar_sub_time  undefined\n"
            + "category     -U arusers\n"
            + "==============================================================\n"
            + "qname        main\n"
            + "hostname     4de74d46ed4b\n"
            + "group        sgeuser\n"
            + "owner        sgeuser\n"
            + "project      NONE\n"
            + "department   defaultdepartment\n"
            + "jobname      simple.sh\n"
            + "jobnumber    2\n"
            + "taskid       undefined\n"
            + "account      sge\n"
            + "priority     0\n"
            + "qsub_time    Thu Jan 13 07:43:13 2022\n"
            + "start_time   Thu Jan 13 07:43:20 2022\n"
            + "end_time     Thu Jan 13 07:43:41 2022\n"
            + "granted_pe   NONE\n"
            + "slots        1\n"
            + "failed       0\n"
            + "exit_status  0\n"
            + "ru_wallclock 21s\n"
            + "ru_utime     0.285s\n"
            + "ru_stime     0.256s\n"
            + "ru_maxrss    7.023KB\n"
            + "ru_ixrss     0.000B\n"
            + "ru_ismrss    0.000B\n"
            + "ru_idrss     0.000B\n"
            + "ru_isrss     0.000B\n"
            + "ru_minflt    5659\n"
            + "ru_majflt    0\n"
            + "ru_nswap     0\n"
            + "ru_inblock   0\n"
            + "ru_oublock   24\n"
            + "ru_msgsnd    0\n"
            + "ru_msgrcv    0\n"
            + "ru_nsignals  0\n"
            + "ru_nvcsw     158\n"
            + "ru_nivcsw    6\n"
            + "cpu          0.541s\n"
            + "mem          1.645MBs\n"
            + "io           0.000B\n"
            + "iow          0.000s\n"
            + "maxvmem      6.508MB\n"
            + "arid         undefined\n"
            + "ar_sub_time  undefined\n"
            + "category     -U arusers";

    private static final String invalidDataReport =
            "==============================================================\n"
            + "qname        all.q\n"
            + "hostname     4de74d46ed4b\n"
            + "group        sgeuser\n"
            + "owner        sgeuser\n"
            + "project      NONE\n"
            + "department   defaultdepartment\n"
            + "jobname      simple.sh\n"
            + "jobnumber    3\n"
            + "taskid       undefined\n"
            + "account      sge\n"
            + "priority     0\n"
            + "qsub_time    Thu Jan 13 07:43:14 2022\n"
            + "start_time   Thu Jan 13 07:43:50 2022\n"
            + "end_time     Thu Jan 13 07:44:11 2022\n"
            + "granted_pe   NONE\n"
            + "slots        1\n"
            + "failed       0\n"
            + "exit_status  0\n"
            + "ru_wallclock 21s\n"
            + "ru_utime     0.323s\n"
            + "ru_stime     0.264s\n"
            + "ru_maxrss    7.070KB\n"
            + "ru_ixrss     0.000B\n"
            + "ru_ismrss    0.000B\n"
            + "ru_idrss     0.000B\n"
            + "ru_isrss     0.000B\n"
            + "ru_minflt    5633\n"
            + "ru_majflt    0\n"
            + "ru_nswap     0\n"
            + "ru_inblock   0\n"
            + "ru_oublock   24\n"
            + "ru_msgsnd    0\n"
            + "ru_msgrcv    0\n"
            + "ru_nsignals  0\n"
            + "ru_nvcsw     160\n"
            + "ru_nivcsw    7\n"
            + "cpu          0.586s\n"
            + "mem          1.789TBs\n"
            + "io           0.000B\n"
            + "iow          0.000s\n"
            + "maxvmem      6.555zB\n"
            + "arid         undefined\n"
            + "ar_sub_time  undefined\n"
            + "category     -U arusers";

    private static final String dataReportWithInvalidNumberOfLine =
            "==============================================================\n"
            + "qname        all.q\n"
            + "hostname     4de74d46ed4b\n"
            + "group        sgeuser\n"
            + "owner        sgeuser\n"
            + "project      NONE\n"
            + "department   defaultdepartment\n"
            + "jobname      simple.sh\n"
            + "jobnumber    3\n"
            + "taskid       undefined\n"
            + "account      sge\n"
            + "priority     0\n"
            + "qsub_time    Thu Jan 13 07:43:14 2022\n"
            + "start_time   Thu Jan 13 07:43:50 2022\n"
            + "end_time     Thu Jan 13 07:44:11 2022\n"
            + "granted_pe   NONE\n"
            + "slots        1\n";

    private static final String NEW_LINE_DELIMITER = "\n";
    private static final String SGEUSER = "sgeuser";
    private static final double DELTA = 1e-5;
    private static final int THOUSAND_FACTOR = 1000;

    @Test
    public void shouldReturnValidReportWhenParseValidDataWithFooter() {
        final UsageReport usageReport = new SgeJobAccountingDataParser().parseAccountingDataFromStdOut(
                stdOutAsStringToLineList(oneJobAndFooterReport));
        assertTrue(usageReport instanceof JobFilteredUsageReport);

        final JobFilteredUsageReport result = (JobFilteredUsageReport) usageReport;
        assertEquals(21, result.getWallClock());
        assertEquals(0.321, result.getUserTime(), DELTA);
        assertEquals(0.208, result.getSystemTime(), DELTA);
        assertEquals(0.529, result.getCpuTime(), DELTA);
        assertEquals(162.529 / THOUSAND_FACTOR / THOUSAND_FACTOR, result.getMemory(), DELTA);
        assertEquals(0, result.getIoData());
        assertEquals(0, result.getIoWaiting());
        assertEquals(Set.of(SGEUSER), result.getOwners());
        assertEquals(Set.of("all.q"), result.getQueues());
        assertEquals(Set.of("e58e7973af6e"), result.getHosts());
        assertEquals(Collections.emptySet(), result.getParallelEnvs());
        assertEquals(Set.of(1L), result.getMatchingJobIds());
    }

    @Test
    public void shouldReturnValidReportWhenParseValidData() {
        final UsageReport usageReport = new SgeJobAccountingDataParser().parseAccountingDataFromStdOut(
                        stdOutAsStringToLineList(twoJobsReport));
        assertTrue(usageReport instanceof JobFilteredUsageReport);

        final JobFilteredUsageReport result = (JobFilteredUsageReport) usageReport;
        assertEquals(42, result.getWallClock());
        assertEquals(0.614, result.getUserTime(), DELTA);
        assertEquals(0.47, result.getSystemTime(), DELTA);
        assertEquals(1.085, result.getCpuTime(), DELTA);
        assertEquals(3.274 / THOUSAND_FACTOR, result.getMemory(), DELTA);
        assertEquals(0, result.getIoData());
        assertEquals(0, result.getIoWaiting());
        assertEquals(Set.of(SGEUSER), result.getOwners());
        assertEquals(Set.of("all.q", "main"), result.getQueues());
        assertEquals(Set.of("4de74d46ed4b"), result.getHosts());
        assertEquals(Set.of("make"), result.getParallelEnvs());
        assertEquals(Set.of(1L, 2L), result.getMatchingJobIds());
    }

    @ParameterizedTest
    @ValueSource(strings = {invalidDataReport, dataReportWithInvalidNumberOfLine, ""})
    public void shouldThrowExceptionWhenParseInvalidData(final String stdOut) {
        assertThrows(GridEngineException.class, () -> new SgeJobAccountingDataParser().parseAccountingDataFromStdOut(
                stdOutAsStringToLineList(stdOut)));
    }

    private List<String> stdOutAsStringToLineList(final String stdOutString) {
        return List.of(stdOutString.split(NEW_LINE_DELIMITER));
    }
}
