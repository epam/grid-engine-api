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

package com.epam.grid.engine.provider.utils.sge.queue;

import com.epam.grid.engine.cmd.GridEngineCommandCompiler;
import com.epam.grid.engine.entity.CommandType;
import com.epam.grid.engine.exception.GridEngineException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SgeDeleteQueueCommandUtils {

    private static final String QUEUE_PARAMETER = "queue";
    private static final String QCONF_DQ = "qconf_dq";
    private static final String WRONG_QUEUE_NAME_MESSAGE = "Invalid queue deletion request:"
            + " Queue name should be specified!";

    /**
     * Checks if the filter is not empty or null.
     *
     * @param queueName Search parameters for queue to delete.
     */
    public static void validateDeletionRequest(final String queueName) {
        if (!StringUtils.hasText(queueName)) {
            throw new GridEngineException(HttpStatus.BAD_REQUEST, WRONG_QUEUE_NAME_MESSAGE);
        }
    }

    /**
     * Building SGE delete queue command that should be passed to an executor.
     *
     * @param queueName Search parameters for queue to delete.
     * @return array of string commands.
     */
    public static String[] buildDeleteQueueCommand(
            final String queueName,
            final GridEngineCommandCompiler commandCompiler,
            final CommandType commandType) {
        final Context context = new Context();
        context.setVariable(QUEUE_PARAMETER, queueName);
        return commandCompiler.compileCommand(commandType, QCONF_DQ, context);
    }
}
