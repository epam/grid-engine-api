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

package com.epam.grid.engine.entity.job.slurm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This class represents SLURM job.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlurmJob {
    /**
     * Account the job ran under.
     */
    private String account;
    /**
     * Trackable RESources per node.
     */
    private String tresPerNode;
    /**
     * Minimum number of CPUs (processors) per node requested by the job.
     * This reports the value of the srun --mincpus option with a default value of zero.
     */
    private int minCpus;
    /**
     * Minimum size of temporary disk space (in MB) requested by the job.
     */
    private int minTmpDisk;
    /**
     * The time of job termination, actual or expected.
     */
    private LocalDateTime endTime;
    /**
     * Features required by the job.
     */
    private String features;
    /**
     * Group name of the job. (Valid for jobs only)
     */
    private String groupName;
    /**
     * Can the compute resources allocated to the job be over subscribed by other jobs.
     * The resources to be over subscribed can be nodes, sockets, cores,
     * or hyperthreads depending upon configuration. The value will be "YES" if the job
     * was submitted with the oversubscribe option or the partition is configured with
     * OverSubscribe=Force, "NO" if the job requires exclusive node access, "USER"
     * if the allocated compute nodes are dedicated to a single user, "MCS"
     * if the allocated compute nodes are dedicated to a single security class
     * (See MCSPlugin and MCSParameters configuration parameters for more information),
     * "OK" otherwise (typically allocated dedicated CPUs).
     */
    private String overSubscribe;
    /**
     * Job ID. This will have a unique value for each element of job arrays and each component of heterogeneous jobs.
     */
    private int jobId;
    /**
     * Job or job step name.
     */
    private String name;
    /**
     * Comment associated with the job.
     */
    private String comment;
    /**
     * Timelimit for the job or job step.
     */
    private String timelimit;
    /**
     * Minimum size of memory (in MB) requested by the job.
     */
    private String minMemory;
    /**
     * List of node names explicitly requested by the job.
     */
    private String reqNodes;
    /**
     * The command to be executed.
     */
    private String command;
    /**
     * Priority of the job (converted to a floating point number between 0.0 and 1.0). Also see prioritylong.
     */
    private double priority;
    /**
     * Quality of service associated with the job.
     */
    private String qos;
    /**
     * The reason a job is in its current state. See the <a href="https://slurm.schedmd.com/squeue.html#SECTION_JOB-REASON-CODES">JOB REASON CODES</a>
     * section below for more information.
     */
    private String reason;
    /**
     * Job state in compact form. See the JOB STATE CODES section below for a list of possible states.
     */
    private String stateCompact;
    /**
     * User name for a job or job step.
     */
    private String userName;
    /**
     * Reservation for the job.
     */
    private String reservation;
    /**
     * Workload Characterization Key (wckey).
     */
    private String wckey;

    private String excNodes;
    /**
     * Nice value (adjustment to a job's scheduling priority).
     */
    private String nice;
    /**
     * Number of requested sockets, cores, and threads (S:C:T) per node for the job. When (S:C:T) has not been set,
     * "*" is displayed.
     */
    private String sct;

    private String execHost;

    private int cpus;
    /**
     * List of nodes allocated to the job or job step. In the case of a COMPLETING job, the list of nodes will comprise
     * only those nodes that have not yet been returned to service.
     */
    private int nodes;
    /**
     * Job dependencies remaining. This job will not begin execution until these dependent jobs complete.
     * In the case of a job that can not run due to job dependencies never being satisfied, the full original job
     * dependency specification will be reported. A value of NULL implies this job has no dependencies.
     */
    private String dependency;
    /**
     * Prints the job ID of the job array.
     */
    private int arrayJobId;
    /**
     * Group ID of the job.
     */
    private int groupId;
    /**
     * Number of sockets in a node to dedicate to a job (minimum).
     */
    private String socketsPerNode;

    private String coresPerNode;
    /**
     * Number of cores in a socket to dedicate to a job (minimum).
     */
    private String threadsPerSocket;
    /**
     * Prints the task ID of the job array.
     */
    private String arrayTaskId;
    /**
     * Time left for the job to execute in days-hours:minutes:seconds. This value is calculated by subtracting
     * the job's time used from its time limit. The value may be "NOT_SET" if not yet established or "UNLIMITED"
     * for no limit.
     */
    private String timeLeft;
    /**
     * Time used by the job or job step in days-hours:minutes:seconds. The days and hours are printed only as needed.
     * For job steps this field shows the elapsed time since execution began and thus will be inaccurate for job steps
     * which have been suspended. Clock skew between nodes in the cluster will cause the time to be inaccurate.
     * If the time is obviously wrong (e.g. negative), it displays as "INVALID".
     */
    private String timeUsed;
    /**
     * List of nodes allocated to the job or job step. In the case of a COMPLETING job, the list of nodes will comprise
     * only those nodes that have not yet been returned to service.
     */
    private List<String> nodeList;
    /**
     * Are contiguous nodes requested by the job.
     */
    private int contiguous;
    /**
     * Partition of the job or job step.
     * <p>
     * According to <a href="https://slurm.schedmd.com/quickstart.html">documentation</a>, "The partitions
     * can be considered job queues, each of which has an assortment of constraints such as job size limit,
     * job time limit, users permitted to use it, etc."
     */
    private String partition;
    /**
     * Priority of the job (generally a very large unsigned integer).
     */
    private long priorityLong;
    /**
     * Indicates where the job is running or the reason it is still pending.
     */
    private List<String> nodeListReason;
    /**
     * Actual or expected start time of the job or job step.
     */
    private LocalDateTime startTime;
    /**
     * Job state in extended form. See the JOB STATE CODES section below for a list of possible states.
     */
    private String state;
    /**
     * User ID for a job or job step.
     */
    private int uid;
    /**
     * The time that the job was submitted at.
     */
    private LocalDateTime submissionTime;
    /**
     * Licenses reserved for the job.
     */
    private String licenses;
    /**
     * Count of cores reserved on each node for system use (core specialization).
     */
    private String coreSpec;
    /**
     * For pending jobs, a list of the nodes expected to be used when the job is started.
     */
    private String schedNodes;
    /**
     * The job's working directory.
     */
    private String workDir;
}
