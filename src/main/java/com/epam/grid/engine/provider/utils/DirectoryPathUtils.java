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

package com.epam.grid.engine.provider.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.Files;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectoryPathUtils {
    private static final String ALL_PERMISSIONS_STRING = "rwxrw-rw-";

    /**
     * If path directory has absolute path, checks, that it begins with root and exists, creates if
     * needed.
     * If path directory has relative path, adds root to its beginning, checks its existence and
     * creates, if needed.
     *
     * @param path Directory, which should be checked for correction path
     * @param root Primary working directory from properties
     * @return Adjusted directory path with added primary directory added if needed
     */

    public static Path resolvePathToAbsolute(final String root, final String path) {
        Path processingPath = Path.of(path);
        if (processingPath.isAbsolute()) {
            if (!path.startsWith(root)) {
                throw new IllegalStateException("Nested folder path is absolute, but doesn't start with "
                        + "grid.engine.shared.folder");
            }
        } else {
            processingPath = Paths.get(root, path);
            log.info("Nested folder path was changed to " + processingPath);
        }
        checkIfFolderNotExistsAndCreate(processingPath);
        return processingPath;
    }

    private static void checkIfFolderNotExistsAndCreate(final Path folderToCreate) {
        if (!Files.exists(folderToCreate)) {
            if (folderToCreate.toFile().mkdirs()) {
                log.info("Directory with path " + folderToCreate + " was created.");
                try {
                    grantAllPermissionsToFolder(folderToCreate);
                } catch (final IOException ioException) {
                    throw new IllegalArgumentException("Failed to grant permissions to the path " + folderToCreate,
                            ioException);
                }
            } else {
                throw new IllegalArgumentException("Failed to create directory with path " + folderToCreate);
            }
        }
    }

    private static void grantAllPermissionsToFolder(final Path directory) throws IOException {
        final Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(ALL_PERMISSIONS_STRING);
        Files.setPosixFilePermissions(directory, permissions);
    }
}
