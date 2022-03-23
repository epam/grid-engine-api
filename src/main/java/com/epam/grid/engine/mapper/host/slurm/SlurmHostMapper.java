package com.epam.grid.engine.mapper.host.slurm;

import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.entity.host.slurm.SlurmHost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface SlurmHostMapper {

    @Mappings({
            @Mapping(target = "hostname", source = "slurmHost.nodeName"),
            @Mapping(target = "typeOfArchitect", source = "slurmHost.arch"),
            @Mapping(target = "numOfProcessors", source = "slurmHost.cpuTotal"),
            @Mapping(target = "numOfSocket", source = "slurmHost.sockets"),
            @Mapping(target = "numOfCore", source = "slurmHost.coresPerSocket"),
            @Mapping(target = "numOfThread", source = "slurmHost.threadsPerCore"),
            @Mapping(target = "memTotal", source = "slurmHost.realMemory"),
            @Mapping(target = "memUsed", source = "slurmHost.allocatedMemory")})
    Host mapToHost(final SlurmHost slurmHost);
}
