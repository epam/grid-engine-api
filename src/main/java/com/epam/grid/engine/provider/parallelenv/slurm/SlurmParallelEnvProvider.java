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

package com.epam.grid.engine.provider.parallelenv.slurm;

import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.entity.ParallelEnvFilter;
import com.epam.grid.engine.entity.parallelenv.ParallelEnv;
import com.epam.grid.engine.entity.parallelenv.PeRegistrationVO;
import com.epam.grid.engine.exception.GridEngineException;
import com.epam.grid.engine.provider.parallelenv.ParallelEnvProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This is the implementation of the PE provider for SLURM. This class always returns an exception, as user should use
 * ParallelExecutionOptions for SLURM.
 */
@Service
@ConditionalOnProperty(name = "grid.engine.type", havingValue = "SLURM")
public class SlurmParallelEnvProvider implements ParallelEnvProvider {
    @Override
    public CommandType getProviderType() {
        return CommandType.SLURM;
    }

    @Override
    public List<ParallelEnv> listParallelEnv(final ParallelEnvFilter parallelEnvFilter) {
        throw new GridEngineException(HttpStatus.BAD_REQUEST, "Parallel environment is not supported in SLURM engine.");
    }

    @Override
    public ParallelEnv getParallelEnv(final String peName) {
        throw new GridEngineException(HttpStatus.BAD_REQUEST, "Parallel environment is not supported in SLURM engine.");
    }

    @Override
    public ParallelEnv deleteParallelEnv(final String parallelEnvName) {
        throw new GridEngineException(HttpStatus.BAD_REQUEST, "Parallel environment is not supported in SLURM engine.");
    }

    @Override
    public ParallelEnv registerParallelEnv(final PeRegistrationVO registrationRequest) {
        throw new GridEngineException(HttpStatus.BAD_REQUEST, "Parallel environment is not supported in SLURM engine.");
    }
}
