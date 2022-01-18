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

package com.epam.grid.engine.mapper.queue.sge;

import com.epam.grid.engine.entity.queue.sge.SgeQueue;
import com.epam.grid.engine.provider.utils.sge.common.SgeOutputParsingUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_LIST;
import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SgeQueueMapperTest {
    private static final int SEQNO = 0;
    private static final int NSUSPEND = 1;
    private static final int PRIORITY = 0;
    private static final String QNAME = "all.q";
    private static final String SUSPENDINTERVAL = "00:05:00";
    private static final String MINCPUINTERVAL = "00:05:00";
    private static final String QTYPE = "BATCH INTERACTIVE";
    private static final String SLOTS = "1,[863431bb452c=1]";
    private static final String TMPDIR = "/tmp";
    private static final String SHELL = "/bin/sh";
    private static final String SHELLSTARTMODE = "posix_compliant";
    private static final String NOTIFY = "00:00:60";
    private static final String INITIALSTATE = "default";
    private static final String INFINITY_VALUE = "INFINITY";
    private static final List<String> HOST_LIST = List.of("@allhosts");
    private static final List<String> LOADTHRESHOLDS = List.of("np_load_avg=1.75");
    private static final List<String> PE_LIST = List.of("make", "smp", "mpi");

    private static final List<String> testSgeQueueRawOutput = List.of(
            "qname                 all.q",
            "hostlist              @allhosts",
            "seq_no                0",
            "load_thresholds       np_load_avg=1.75",
            "suspend_thresholds    NONE",
            "nsuspend              1",
            "suspend_interval      00:05:00",
            "priority              0",
            "min_cpu_interval      00:05:00",
            "processors            UNDEFINED",
            "qtype                 BATCH INTERACTIVE",
            "ckpt_list             NONE",
            "pe_list               make smp mpi",
            "rerun                 FALSE",
            "slots                 1,[863431bb452c=1]",
            "tmpdir                /tmp",
            "shell                 /bin/sh",
            "prolog                NONE",
            "epilog                NONE",
            "shell_start_mode      posix_compliant",
            "starter_method        NONE",
            "suspend_method        NONE",
            "resume_method         NONE",
            "terminate_method      NONE",
            "notify                00:00:60",
            "owner_list            NONE",
            "user_lists            NONE",
            "xuser_lists           NONE",
            "subordinate_list      NONE",
            "complex_values        NONE",
            "projects              NONE",
            "xprojects             NONE",
            "calendar              NONE",
            "initial_state         default",
            "s_rt                  INFINITY",
            "h_rt                  INFINITY",
            "s_cpu                 INFINITY",
            "h_cpu                 INFINITY",
            "s_fsize               INFINITY",
            "h_fsize               INFINITY",
            "s_data                INFINITY",
            "h_data                INFINITY",
            "s_stack               INFINITY",
            "h_stack               INFINITY",
            "s_core                INFINITY",
            "h_core                INFINITY",
            "s_rss                 INFINITY",
            "h_rss                 INFINITY",
            "s_vmem                INFINITY",
            "h_vmem                INFINITY");

    private static final SgeQueueMapper queueMapper = Mappers.getMapper(SgeQueueMapper.class);

    @Test
    public void shouldMapRawOutputToSgeQueue() {
        final SgeQueue expectedSgeQueue = SgeQueue.builder()
                .name(QNAME)
                .hostList(HOST_LIST)
                .numberInSchedulingOrder(SEQNO)
                .loadThresholds(LOADTHRESHOLDS)
                .suspendThresholds(EMPTY_LIST)
                .numOfSuspendedJobs(NSUSPEND)
                .interval(SUSPENDINTERVAL)
                .jobPriority(PRIORITY)
                .minCpuInterval(MINCPUINTERVAL)
                .processorNumber(EMPTY_LIST)
                .qtype(QTYPE)
                .checkpointNames(EMPTY_LIST)
                .parallelEnvironmentNames(PE_LIST)
                .rerun(false)
                .slots(SLOTS)
                .tmpDir(TMPDIR)
                .shell(SHELL)
                .prolog(EMPTY_STRING)
                .epilog(EMPTY_STRING)
                .shellStartMode(SHELLSTARTMODE)
                .starterMethod(EMPTY_STRING)
                .suspendMethod(EMPTY_STRING)
                .resumeMethod(EMPTY_STRING)
                .terminateMethod(EMPTY_STRING)
                .notify(NOTIFY)
                .ownerList(EMPTY_LIST)
                .allowedUserGroups(EMPTY_LIST)
                .forbiddenUserGroups(EMPTY_LIST)
                .subordinateList(EMPTY_LIST)
                .complexValues(EMPTY_LIST)
                .allowedProjects(EMPTY_LIST)
                .forbiddenProjects(EMPTY_LIST)
                .calendar(EMPTY_STRING)
                .initialState(INITIALSTATE)
                .secRealTime(INFINITY_VALUE)
                .hourRealTime(INFINITY_VALUE)
                .secCpu(INFINITY_VALUE)
                .hourCpu(INFINITY_VALUE)
                .secFSize(INFINITY_VALUE)
                .hourFSize(INFINITY_VALUE)
                .secData(INFINITY_VALUE)
                .hourData(INFINITY_VALUE)
                .secStack(INFINITY_VALUE)
                .hourStack(INFINITY_VALUE)
                .secCore(INFINITY_VALUE)
                .hourCore(INFINITY_VALUE)
                .secRss(INFINITY_VALUE)
                .hourRss(INFINITY_VALUE)
                .secVmem(INFINITY_VALUE)
                .hourVmem(INFINITY_VALUE)
                .build();
        final Map<String, String> testSgeQueueDescription = SgeOutputParsingUtils
                .parseEntitiesToMap(testSgeQueueRawOutput);

        assertEquals(expectedSgeQueue, queueMapper.mapRawOutputToSgeQueue(testSgeQueueDescription));
    }
}
