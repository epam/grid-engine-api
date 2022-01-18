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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.epam.grid.engine.utils.TextConstants.SPACE;

/**
 * This class performs the formation of the structure of the executed command
 * according to the template.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandArgUtils {

    private static final String DELIMITERS = " \\" + System.lineSeparator();
    private static final String APOSTROPHE = "\"";

    /**
     * This method forms a command structure from a string command.
     *
     * @param command The command from the template.
     * @return The command's structure that is ready for execution.
     */
    public static String[] splitCommandIntoArgs(final String command) {
        final String[] arr = checkAndMergeQuotesStrings(StringUtils.tokenizeToStringArray(command, DELIMITERS));
        return Arrays.stream(arr).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    /**
     * This method searches for parts of quoted strings that are split into several strings when parsing
     * and combines them into one string.
     *
     * @param commandParts The command from the template after parsing.
     * @return The command's structure.
     */
    private static String[] checkAndMergeQuotesStrings(final String[] commandParts) {
        final List<String> result = new ArrayList<>();
        final StringBuilder quotedPart = new StringBuilder();

        for (String commandPart : commandParts) {
            if (commandPart.startsWith(APOSTROPHE)) {
                appendCommandToBuilder(quotedPart, commandPart);
            } else if (commandPart.endsWith(APOSTROPHE)) {
                appendCommandToBuilder(quotedPart, commandPart);
                dumpQuotedToResults(quotedPart, result);
            } else {
                addQuotedPartToBuilderOrDump(commandPart, quotedPart, result);
            }
        }
        dumpQuotedToResultsIfPresent(quotedPart, result);
        return result.toArray(new String[0]);
    }

    private static void addQuotedPartToBuilderOrDump(final String commandPart, final StringBuilder quotedPart,
                                                     final List<String> result) {
        if (quotedPart.length() != 0 && !quotedPart.toString().endsWith(APOSTROPHE)) {
            appendCommandToBuilder(quotedPart, commandPart);
        } else {
            dumpQuotedToResultsIfPresent(quotedPart, result);
            result.add(commandPart);
        }
    }

    private static void dumpQuotedToResults(final StringBuilder quotedPart, final List<String> result) {
        result.add(quotedPart.toString());
        quotedPart.setLength(0);
    }

    private static void dumpQuotedToResultsIfPresent(final StringBuilder quotedPart, final List<String> result) {
        if (quotedPart.length() > 0) {
            dumpQuotedToResults(quotedPart, result);
        }
    }

    private static void appendCommandToBuilder(final StringBuilder quotedPart, final String commandPart) {
        quotedPart.append(SPACE).append(commandPart);
    }
}
