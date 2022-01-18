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

package com.epam.grid.engine.entity.queue.sge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * A simple object that represents a SgeQueue entity.<p>
 * To get more information about SgeQueue parameters see
 * <a href="http://gridscheduler.sourceforge.net/htmlman/htmlman5/queue_conf.html?pathrev=V62u5_TAG">
 *     queue configuration information</a>
 */
@Builder
@Data
@AllArgsConstructor
public class SgeQueue {

    /**
     * The name of the cluster queue.
     */
    private String name;

    /**
     * The list of host identifiers.
     */
    private List<String> hostList;

    /**
     * The position for this queue in the scheduling order
     * withing the suitable queues for a job to be dispatched.
     */
    private Integer numberInSchedulingOrder;

    /**
     * The list of load thresholds.
     */
    private List<String> loadThresholds;

    /**
     * Load thresholds with the same semantics as that of
     * the {@link SgeQueue#loadThresholds} parameter except that
     * exceeding one of the denoted thresholds initiates suspension
     * of one of multiple jobs in the queue.
     */
    private List<String> suspendThresholds;

    /**
     * The number of jobs which are suspended/enabled per time
     * interval.
     */
    private Integer numOfSuspendedJobs;

    /**
     * The time interval in which further suspended jobs are
     * suspended.
     */
    private String interval;

    /**
     * The value at which jobs in this queue will be run.
     */
    private Integer jobPriority;

    /**
     * The time between two automatic checkpoints in case of
     * transparently checkpointing jobs.
     */
    private String minCpuInterval;

    /**
     * The list of processors in case of a multiprocessor execution
     * host.
     */
    private List<String> processorNumber;

    /**
     * The type of queue.
     */
    private String qtype;

    /**
     * The list of  administrator-defined  checkpointing  interface
     * names.
     */
    private List<String> checkpointNames;

    /**
     * The list of administrator-defined parallel environment
     * names  to  be  associated  with  the  queue.
     */
    private List<String> parallelEnvironmentNames;

    /**
     * A default behavior for jobs which are aborted by
     * system crashes or manual "violent" shutdown of the complete
     * Sun Grid Engine system on the queue host.
     */
    private Boolean rerun;

    /**
     * The maximum number of concurrently executing jobs allowed in
     * the queue.
     */
    private String slots;

    /**
     * The absolute path to the base of the temporary directory filesystem.
     */
    private String tmpDir;

    /**
     * The executable path of the command interpreter (e.g. sh or csh)
     * to be used to process the job scripts executed in the queue.
     */
    private String shell;

    /**
     * The executable path of a shell script that is started before
     * execution of Sun Grid Engine jobs with the same environment
     * setting as that for the Sun Grid Engine jobs to be started
     * afterwards.
     */
    private String prolog;

    /**
     * The executable path of a shell script that is started  after
     * execution  of Sun Grid Engine jobs with the same environment
     * setting as that for the Sun Grid Engine jobs that has just
     * completed.
     */
    private String epilog;

    /**
     * The mechanisms which are used to actually invoke
     * the job scripts on the execution hosts.
     */
    private String shellStartMode;

    /**
     * The specified executable path to be used as a job starter
     * facility responsible for starting batch jobs.
     */
    private String starterMethod;

    /**
     * This parameter can be used for overwriting the default
     * method used by Sun Grid Engine for suspension.
     */
    private String suspendMethod;

    /**
     * This parameter can be used for overwriting the default
     * method used by Sun Grid Engine for release of suspension.
     */
    private String resumeMethod;

    /**
     * This parameter can be used for overwriting the default
     * method used by Sun Grid Engine for termination of a job.
     */
    private String terminateMethod;

    /**
     * The time waited between delivery notification signals and
     * suspend/kill signals if job was submitted with
     * the qsub -notify option.
     */
    private String notify;

    /**
     * The list of user names who are authorized to disable and
     * suspend this queue through qmod SGE command.
     */
    private List<String> ownerList;

    /**
     * The list of user groups with access permissions to queues.
     */
    private List<String> allowedUserGroups;

    /**
     * The list of user groups who are not allowed to use parallel environments.
     */
    private List<String> forbiddenUserGroups;

    /**
     * The list of queue names that defines priority between queues.
     */
    private List<String> subordinateList;

    /**
     * The quotas for resource attributes managed via this queue.
     */
    private List<String> complexValues;

    /**
     * A list of SGE projects that have access to the queue.
     */
    private List<String> allowedProjects;

    /**
     * A list of SGE projects that are denied access to the queue.
     */
    private List<String> forbiddenProjects;

    /**
     * A calendar defines the availability of a queue depending
     * on time of day, week and year.
     */
    private String calendar;

    /**
     * An initial state for the queue (default, enabled, disabled).
     */
    private String initialState;

    /**
     * The time having passed since the start of the job.
     */
    private String secRealTime;

    /**
     * The time having passed since the start of the job.
     */
    private String hourRealTime;

    /**
     The per-process CPU time limit in seconds.
     */
    private String secCpu;

    /**
     * The per-job CPU time limit in seconds.
     */
    private String hourCpu;

    /**
     * File size limit.
     */
    private String secFSize;

    /**
     * The total number of disk blocks that this job can create.
     */
    private String hourFSize;

    /**
     * The per-process maximum memory limit in bytes.
     */
    private String secData;

    /**
     * The per-job maximum memory limit in bytes.
     */
    private String hourData;

    /**
     * The per-process stack limit in bytes.
     */
    private String secStack;

    /**
     * The per-job stack limit in bytes.
     */
    private String hourStack;

    /**
     * The per-process maximum core file size in bytes.
     */
    private String secCore;

    /**
     * The per-job maximum core file size in bytes.
     */
    private String hourCore;

    /**
     * RSS memory.
     */
    private String secRss;

    /**
     * RSS memory.
     */
    private String hourRss;

    /**
     * The same as {@link SgeQueue#secData}
     * (if both are set, the minimum is used).
     */
    private String secVmem;

    /**
     * The same as {@link SgeQueue#hourData}
     * (if both are set, the minimum is used).
     */
    private String hourVmem;
}
