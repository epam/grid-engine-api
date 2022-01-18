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

import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.exception.GridEngineException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.epam.grid.engine.utils.TextConstants.SPACE;

@Component
public class SimpleCmdExecutor implements CmdExecutor {

    @Override
    public CommandResult execute(final String... arguments) {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);
        Process process = null;
        try {
            process = processBuilder.start();

            final AsyncOutputReader asyncOutputReader = new AsyncOutputReader(process);
            final List<String> stdOut = new ArrayList<>();
            final List<String> stdErr = new ArrayList<>();
            asyncOutputReader.readLinesToLists(stdOut, stdErr);
            final int exitCode = process.waitFor();
            asyncOutputReader.shutdown();

            return new CommandResult(stdOut, exitCode, stdErr);
        } catch (final InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new GridEngineException(HttpStatus.NOT_FOUND,
                    "Something went wrong while executing the command: " + String.join(SPACE, arguments), e);
        } catch (final RuntimeException | IOException e) {
            throw new GridEngineException(HttpStatus.NOT_FOUND,
                    "Something went wrong while reading output of the command: " + String.join(SPACE, arguments), e);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    @AllArgsConstructor
    static class AsyncOutputReader {
        private static final String CANNOT_READ_CMD_EXECUTION_RESULT = "Cannot read command execution result";

        private final Process process;
        private final ExecutorService executorService = Executors.newFixedThreadPool(2);
        private final List<Future<?>> futures = new ArrayList<>();

        public void readLinesToLists(final List<String> outLines, final List<String> errLines) {
            futures.add(executorService.submit(() -> readOutputStream(process.getInputStream(), outLines)));
            futures.add(executorService.submit(() -> readOutputStream(process.getErrorStream(), errLines)));
        }

        private void readOutputStream(final InputStream inputStream, final List<String> result) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.lines().forEach(result::add);
            } catch (final IOException e) {
                throw new GridEngineException(HttpStatus.INTERNAL_SERVER_ERROR, CANNOT_READ_CMD_EXECUTION_RESULT, e);
            }
        }

        public void shutdown() throws ExecutionException, InterruptedException {
            try {
                for (Future<?> future : futures) {
                    future.get();
                }
            } finally {
                executorService.shutdown();
            }
        }
    }
}
