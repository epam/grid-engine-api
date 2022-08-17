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

package com.epam.grid.engine.entity.queue.slurm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class SlurmQueue {

    /**
     * Partition state: up or down.
     */
    private String avail;

    /**
     * Count of CPUs (processors) on each node.
     */
    private int cpus;

    /**
     * Count of sockets (S), cores (C), and threads (T) on these nodes.
     */
    private String socketsCoresThreads;

    /**
     * Count of sockets on these nodes.
     */
    private int sockets;

    /**
     * Count of cores on these nodes.
     */
    private int cores;

    /**
     * Count of threads on these nodes.
     */
    private int threads;

    /**
     * Resource allocations in this partition  are  restricted  to  the named  groups. ALL  indicates  that  all
     * groups  may use this partition.
     */
    private List<String> groups;

    /**
     * Minimum and maximum node count that can be allocated to any user job.   A  single  number  indicates
     * the minimum and maximum node count are the same. INFINITE is  used  to  identify  partitions
     * without a maximum node count.
     */
    private String jobSize;

    /**
     * Maximum     time     limit     for     any     user    job    in days-hours:minutes:seconds.
     * INFINITE  is  used   to   identify partitions without a job time limit.
     */
    private String timelimit;

    /**
     * Size of real memory in megabytes on these nodes.
     */
    private long memory;

    /**
     * Nodelist or BP_LIST (BlueGene systems only). Names of nodes associated with this configuration/partition.
     */
    private List<String> nodelist;

    /**
     * Count of nodes with this particular configuration.
     */
    private int nodes;

    /**
     * Count  of nodes with this particular configuration by node state in the form "available/idle".
     */
    private String nodesAvailableIdle;

    /**
     * Count of nodes with this particular configuration by node state in the form "available/idle/other/total".
     */
    private String nodesAvailableIdleOtherTotal;

    /**
     * Name  of  a  partition.  Note that the suffix "*" identifies the default partition.
     */
    private String partition;

    /**
     * Is  the  ability  to  allocate  resources  in   this   partition restricted to user root, yes or no.
     */
    private String root;

    /**
     * Will  jobs  allocated  resources  in  this partition share those resources.
     * NO indicates resources are never shared.
     * EXCLUSIVE indicates  whole nodes are dedicated to jobs (equivalent to srun --exclusive  option,  may  be  used
     * even  with  shared/cons_res managing  individual processors).
     * FORCE indicates resources are always available to be shared.
     * YES indicates resource may be shared or not per job’s resource allocation.
     */
    private String share;

    /**
     * State   of  the  nodes.   Possible  states  include:  allocated, completing, down, drained, draining, fail,
     * failing,  idle, and unknown  plus their abbreviated forms: alloc, comp, donw, drain, drng, fail, failg, idle,
     * and unk respectively. Note that the suffix "*" identifies nodes that are presently not responding.
     */
    private String state;

    /**
     * Size of temporary disk space in megabytes on these nodes.
     */
    private long tmpDisk;
}
