package com.epam.grid.engine.entity.host.slurm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class maps values obtained from grid engine.
 *
 * @see com.epam.grid.engine.entity.host.Host
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlurmHost {

    /**
     * The name of the host.
     */
    private String nodeName;

    /**
     * The operating system architecture.
     */
    private String arch;

    /**
     * Number of cores reported for all sockets on a host.
     */
    private Integer coresPerSocket;

    /**
     * The number of processors provided by the execution host.
     */
    private Integer cpuTotal;

    /**
     * The total amount of memory.
     */
    private Long realMemory;

    /**
     * The amount of memory used.
     */
    private Long allocatedMemory;

    /**
     * Number of sockets available on the reporting host.
     */
    private Integer sockets;

    /**
     * Number of hardware threads reported for all cores on a host.
     */
    private Integer threadsPerCore;
}
