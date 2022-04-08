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
import com.epam.grid.engine.exception.GridEngineException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class realizes different compile methods by context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GridEngineCommandCompilerImpl implements GridEngineCommandCompiler {

    private static final String CREATING_ENTITY_DESCRIPTION_FILE_ERROR =
            "Unable to create temporary entity description file";
    private static final String COMMAND_PATH = "command";
    private static final String ENTITY_PATH = "entity";

    private final SpringTemplateEngine templateEngine;

    /**
     * This method creates the command structure by engine type and context.
     *
     * @param engineType A grid engine type.
     * @param command    Name of the command used.
     * @param context    Data for forming a command.
     * @return The structure of the command execution.
     */
    @Override
    public String[] compileCommand(final EngineType engineType, final String command,
                                   final IContext context) {
        final String commandFolderPath = Paths.get(getPathByEngineType(engineType), COMMAND_PATH, command).toString();
        return CommandArgUtils.splitCommandIntoArgs(templateEngine.process(commandFolderPath, context));
    }

    /**
     * Configures temporary file with entity information by engine type and context.
     *
     * @param engineType A grid engine type.
     * @param entity     Name of the entity used.
     * @param context    Structure with data for forming a template file.
     * @return Path to temporary file.
     */
    @Override
    public Path compileEntityConfigFile(final EngineType engineType, final String entity, final IContext context) {
        final String commandFolderPath = Paths.get(getPathByEngineType(engineType), ENTITY_PATH, entity).toString();
        final String entityDescription = templateEngine.process(commandFolderPath, context);
        return writeEntityDescriptionToTemporaryFile(entityDescription);
    }

    private String getPathByEngineType(final EngineType engineType) {
        final String path;
        switch (engineType) {
            case SGE:
                path = "sge";
                break;
            case SLURM:
                path = "slurm";
                break;
            default:
                throw new GridEngineException(HttpStatus.NO_CONTENT,
                        String.format("Engine type %s is not supported", engineType));
        }
        return path;
    }

    private Path writeEntityDescriptionToTemporaryFile(final String entityDescription) {
        try {
            final Path tempFile = Files.createTempFile("sge-api_entity_tmp_files", null);
            return Files.writeString(tempFile, entityDescription);
        } catch (final IOException e) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, CREATING_ENTITY_DESCRIPTION_FILE_ERROR, e);
        }
    }
}
