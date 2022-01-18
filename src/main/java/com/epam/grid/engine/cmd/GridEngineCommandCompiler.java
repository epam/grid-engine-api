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

package com.epam.grid.engine.cmd;

import com.epam.grid.engine.entity.EngineType;
import org.thymeleaf.context.IContext;

import java.nio.file.Path;

/**
 * This is an interface that provides requirements to different compile methods by context.
 */
public interface GridEngineCommandCompiler {

    /**
     * Creates the command structure by engine type and context.
     *
     * @param engineType A grid engine type.
     * @param command    Name of the command used.
     * @param context    Data for forming a command.
     * @return The structure of the command execution.
     */
    String[] compileCommand(EngineType engineType, String command, IContext context);

    /**
     * Configures temporary file with entity information by engine type and context.
     *
     * @param engineType A grid engine type.
     * @param entity     Name of the entity used.
     * @param context    Structure with data for forming a template file.
     * @return Path to temporary file.
     */
    Path compileEntityConfigFile(EngineType engineType, String entity, IContext context);
}
