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

import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.EntitiesRawOutput;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.epam.grid.engine.utils.TextConstants.SPACE;

/**
 * This class contains methods for parsing raw tables into objects.
 *
 * @see com.epam.grid.engine.entity.parallelenv.ParallelEnv
 * @see com.epam.grid.engine.entity.queue.Queue
 * @see com.epam.grid.engine.entity.hostgroup.HostGroup
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SgeOutputParsingUtils {

    /**
     * Parses stdOut of {@link CommandResult} after execution of SGE command
     * to List where each inner List of Strings containing SGE parameters names
     * and their descriptions and starting with entityType parameter, that allows
     * to retrieve the parameters of each certain entity.
     * The number of inner Lists equals to the number of entities depending on the
     * SGE command output.
     *
     * @param stdOut a List containing stdOut of CommandResult
     * @param entityType determines begging of an entity
     * @return a List containing Lists of Strings
     */
    public static List<EntitiesRawOutput> splitOutputToEntities(final List<String> stdOut, final String entityType) {
        final List<Integer> indices = IntStream.range(0, stdOut.size())
                .filter(i -> stdOut.get(i).startsWith(entityType))
                .boxed()
                .collect(Collectors.toList());
        indices.add(stdOut.size());

        return IntStream.range(0, indices.size() - 1)
                .mapToObj(i -> stdOut.subList(indices.get(i), indices.get(i + 1)))
                .map(i -> EntitiesRawOutput.builder()
                        .rawEntitiesList(i).build())
                .collect(Collectors.toList());
    }

    /**
     * Converts raw SGE command output to {@link Map} where key represents SGE parameter name
     * and value is the SGE parameter description.
     * @param entitiesTable is a List of Strings containing SGE parameters names and their descriptions
     *                              and starting with prefix parameter
     * @return a Map of Strings
     */
    public static Map<String, String> parseEntitiesToMap(final List<String> entitiesTable) {
        return entitiesTable.stream()
                .collect(Collectors.toMap(line -> line.split(SPACE, 2)[0].trim(),
                        line -> line.split(SPACE, 2)[1].trim()));
    }
}
