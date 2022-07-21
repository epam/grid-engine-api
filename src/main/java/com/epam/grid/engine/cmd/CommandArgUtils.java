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

import com.epam.grid.engine.utils.TextConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class performs the formation of the structure of the executed command
 * according to the template.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandArgUtils {

    private static final char CR = '\r';
    private static final char LF = '\n';
    private static final char TAB = '\t';
    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char BACKSLASH = '\\';

    /**
     * This method forms a command structure from a prepared string by command template.
     *
     * <p> It iterates over the received string character by character. The command line will be split
     * into tokens by the whitespace characters.
     *
     * <p> If it comes across an unescaped quote character (the previous character was not a backslash) - next
     * some characters will be a token until it comes across next an unescaped quote.
     *
     * <p> If quotes will be used in the token, they must be additionally escaped with a slash, and the token
     * itself in the command template must be enclosed in quotes. Also, the token in the command template
     * must be enclosed in quotes if  whitespace characters are to be used in it.
     *
     * <blockquote>For example, the command string
     * <p><b> {@code "  firstToken  \"second token\"  \"third token with \\"quotes\\"   \""}</b>
     * <p> yields the following result
     * <p><b> {@code {"firstToken", "second token", "third token with \"quotes\"   "}}</b>
     * </blockquote>
     *
     * @param command The command from the template engine.
     * @return The command's structure that is ready for execution.
     */
    public static String[] splitCommandIntoArgs(final String command) {
        final List<String> result = new ArrayList<>();
        final StringBuilder token = new StringBuilder();

        boolean isQuote = false;
        int backslashes = 0;

        for (final char ch : command.toCharArray()) {
            if (!isQuote && ch == QUOTE && token.length() == 0) {
                isQuote = true;
            } else {
                if (isQuote) {
                    switch (ch) {
                        case BACKSLASH:
                            backslashes++;
                            continue;
                        case QUOTE:
                            if (backslashes == 0) {
                                result.add(token.toString());
                                token.setLength(0);
                                isQuote = false;
                                continue;
                            }
                            backslashes--;
                            break;
                        default:
                    }
                    if (backslashes > 0) {
                        IntStream.range(0, backslashes).forEach((index) -> token.append(BACKSLASH));
                        backslashes = 0;
                    }
                } else {
                    if (isWhitespaceCharacter(ch)) {
                        result.add(token.toString());
                        token.setLength(0);
                        continue;
                    }
                }
                token.append(ch);
            }
        }

        result.add(token.toString());
        return result.stream().map(String::trim).filter(t -> !t.isEmpty()).toArray(String[]::new);
    }

    private static boolean isWhitespaceCharacter(final char ch) {
        switch (ch) {
            case CR:
            case LF:
            case SPACE:
            case TAB:
                return true;
            default:
                return false;
        }
    }

    /**
     * Forms a structure to pass environment variables as command argument.
     *
     * @param variables the map of environment variables as keys and their values.
     * @return the string of the created structure.
     */
    public static String envVariablesMapToString(final Map<String, String> variables) {
        final String envVariables = MapUtils.emptyIfNull(variables).entrySet().stream()
                .map(CommandArgUtils::envVarToString)
                .collect(Collectors.joining(TextConstants.COMMA));
        return toEscapeQuotes(envVariables);
    }

    private static String envVarToString(final Map.Entry<String, String> entry) {
        final String value = entry.getValue();
        if (StringUtils.hasText(value)) {
            return String.format("%s=\"%s\"", entry.getKey(), value);
        }
        return entry.getKey();
    }

    /**
     * Converts the token for use in the template as a "convertedToken" to avoid splitting
     * during further parsing. Adds another backslash before each quote.
     *
     * @param token the token to handle.
     * @return the converted token.
     */
    public static String toEscapeQuotes(final String token) {
        final StringBuilder result = new StringBuilder();
        int backslashes = 0;

        for (final char ch : token.toCharArray()) {
            if (ch == BACKSLASH) {
                backslashes++;
                continue;
            }
            if (ch == QUOTE) {
                backslashes++;
            }
            IntStream.range(0, backslashes).forEach((index) -> result.append(BACKSLASH));
            backslashes = 0;
            result.append(ch);
        }
        return result.toString();
    }

    /**
     * Converts each of tokens for use in the template as a "convertedToken" to avoid splitting
     * during further parsing.
     *
     * @param tokens the list of tokens to handle.
     * @return the converted token list.
     */
    public static List<String> toEscapeQuotes(final List<String> tokens) {
        return ListUtils.emptyIfNull(tokens).stream()
                .map(CommandArgUtils::toEscapeQuotes)
                .collect(Collectors.toList());
    }

    /**
     * Encloses the token in quotes.
     *
     * @param token the token to handle.
     * @return the quote enclosed token.
     */
    public static String toEncloseInQuotes(final String token) {
        return QUOTE + token + QUOTE;
    }
}
