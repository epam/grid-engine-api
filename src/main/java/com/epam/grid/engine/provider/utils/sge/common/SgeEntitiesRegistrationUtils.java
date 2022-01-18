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

package com.epam.grid.engine.provider.utils.sge.common;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.entity.EngineType;
import com.epam.grid.engine.exception.GridEngineException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SgeEntitiesRegistrationUtils {

    private static final String TEMP_FILE_FIELD = "tempFile";
    private static final String TEMPORARY_FILE_WAS_NOT_FOUND =
            "Temporary file with entity's description was not found";

    public static String[] normalizePathToUnixFormat(final Path pathToDescription, final String command,
                                                     final GridEngineCommandCompiler commandCompiler) {
        final Context context = new Context();
        context.setVariable(TEMP_FILE_FIELD, pathToDescription.toFile().getAbsolutePath()
                .replaceAll("\\\\", "//"));
        return commandCompiler.compileCommand(EngineType.SGE, command, context);
    }

    public static void deleteTemporaryDescriptionFile(final Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (final IOException e) {
            throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, TEMPORARY_FILE_WAS_NOT_FOUND, e);
        }
    }
}
