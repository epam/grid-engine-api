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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SimpleCmdExecutorTest {

    private static final List<String> EMPTY_LIST = Collections.emptyList();
    private static final String[] WINDOWS_SUCCESSFUL_COMMAND = {"cmd.exe", "/c", "echo \"hello\""};
    private static final String[] LINUX_SUCCESSFUL_COMMAND = {"sh", "-c", "echo \"hello\""};
    private static final String[] WINDOWS_NOT_EXISTING_FUNCTION = {"cmd.exe", "/c", "eco \"hello\""};
    private static final String[] LINUX_NOT_EXISTING_FUNCTION = {"sh", "-c", "eco \"hello\""};
    private static final String[] WINDOWS_INVALID_COMMAND = {"cfghmd.exe", "/c", "echo \"hello\""};
    private static final String[] LINUX_INVALID_COMMAND = {"serh", "-c", "echo \"hello\""};
    private final SimpleCmdExecutor executeImpl = new SimpleCmdExecutor();
    private final boolean isWindows = System.getProperty("os.name")
            .toLowerCase(Locale.US)
            .startsWith("windows");

    @Test
    public void shouldSucceed() {
        final CommandResult returnObject = isWindows ? executeImpl.execute(WINDOWS_SUCCESSFUL_COMMAND)
                : executeImpl.execute(LINUX_SUCCESSFUL_COMMAND);

        Assertions.assertEquals(0, returnObject.getExitCode());
        Assertions.assertEquals(EMPTY_LIST, returnObject.getStdErr());
        Assertions.assertNotEquals(EMPTY_LIST, returnObject.getStdOut());
    }

    @Test
    public void shouldFailWithError() {
        final CommandResult returnObject = isWindows ? executeImpl.execute(WINDOWS_NOT_EXISTING_FUNCTION)
                : executeImpl.execute(LINUX_NOT_EXISTING_FUNCTION);

        Assertions.assertNotEquals(0, returnObject.getExitCode());
        Assertions.assertNotEquals(EMPTY_LIST, returnObject.getStdErr());
        Assertions.assertEquals(EMPTY_LIST, returnObject.getStdOut());
    }

    @Test
    public void shouldFailWithInvalidCommand() {
        if (isWindows) {
            Assertions.assertThrows(GridEngineException.class, () ->
                    executeImpl.execute(WINDOWS_INVALID_COMMAND));
        } else {
            Assertions.assertThrows(GridEngineException.class, () ->
                    executeImpl.execute(LINUX_INVALID_COMMAND));
        }
    }
}
