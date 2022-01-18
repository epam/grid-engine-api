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

package com.epam.grid.engine.mapper.host.sge;

import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.entity.host.sge.SgeHost;
import com.epam.grid.engine.entity.host.sge.SgeHostProperty;
import com.epam.grid.engine.entity.host.sge.SgeHostValue;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Stream;

public class SgeHostMapperTest {

    private static final SgeHostMapper hostMapper = Mappers.getMapper(SgeHostMapper.class);
    private static final String TEST_PLATFORM = "lx-amd64";
    private static final String TEST_HOSTNAME = "test-ip";
    private static final String EMPTY_ATTR_VALUE = "-";

    @ParameterizedTest
    @MethodSource({"provideExceptionalHostMappingParameters"})
    public void shouldThrowExceptionDuringMapping(final SgeHost wrongSgeHost) {

        Assertions.assertThrows(GridEngineException.class, () -> hostMapper.mapToHost(wrongSgeHost));
    }

    public static Stream<Arguments> provideExceptionalHostMappingParameters() {
        return Stream.of(
                Arguments.of(
                        buildSgeHost(TEST_PLATFORM, "2.0",
                                "1.0", "1.0", "2.0", "0",
                                "3", "311", "0", "0")),
                Arguments.of(
                        buildSgeHost("", "",
                                null, null, null, null,
                                null, null, null, null))
        );
    }

    @ParameterizedTest
    @MethodSource({"provideHostMappingParameters"})
    public void shouldReturnCorrectHost(final SgeHost sgeHost, final Host expectedHost) {

        Assertions.assertEquals(expectedHost, hostMapper.mapToHost(sgeHost));
    }

    public static Stream<Arguments> provideHostMappingParameters() {
        return Stream.of(
                Arguments.of(
                        buildSgeHost(EMPTY_ATTR_VALUE, EMPTY_ATTR_VALUE, EMPTY_ATTR_VALUE,
                                EMPTY_ATTR_VALUE, EMPTY_ATTR_VALUE, EMPTY_ATTR_VALUE, EMPTY_ATTR_VALUE,
                                EMPTY_ATTR_VALUE, EMPTY_ATTR_VALUE, EMPTY_ATTR_VALUE),
                        Host.builder().hostname(TEST_HOSTNAME).build()
                ),
                Arguments.of(
                        buildSgeHost(TEST_PLATFORM, "2",
                                "1", "1", "2", "0.00",
                                "3.6G", "311.6M", "0.0", "0.0"),
                        Host.builder()
                                .hostname(TEST_HOSTNAME)
                                .typeOfArchitect(TEST_PLATFORM)
                                .numOfProcessors(2)
                                .numOfSocket(1)
                                .numOfCore(1)
                                .numOfThread(2)
                                .load(0.0)
                                .memTotal(3600000000L)
                                .memUsed(311600000L)
                                .totalSwapSpace(0.0)
                                .usedSwapSpace(0.0)
                                .build()
                )
        );
    }

    private static SgeHost buildSgeHost(final String typeOfArch,
                                        final String numOfProc,
                                        final String numOfSocket,
                                        final String numOfCore,
                                        final String numOfThread,
                                        final String load,
                                        final String memTotal,
                                        final String memUsed,
                                        final String swapTotal,
                                        final String swapUsed) {

        final List<SgeHostValue> attributes = List.of(
                buildHostAttribute(SgeHostProperty.TYPE_OF_ARCHITECT, typeOfArch),
                buildHostAttribute(SgeHostProperty.NUM_OF_PROCESSORS, numOfProc),
                buildHostAttribute(SgeHostProperty.NUM_OF_SOCKET, numOfSocket),
                buildHostAttribute(SgeHostProperty.NUM_OF_CORE, numOfCore),
                buildHostAttribute(SgeHostProperty.NUM_OF_THREAD, numOfThread),
                buildHostAttribute(SgeHostProperty.LOAD, load),
                buildHostAttribute(SgeHostProperty.MEM_TOTAL, memTotal),
                buildHostAttribute(SgeHostProperty.MEM_USED, memUsed),
                buildHostAttribute(SgeHostProperty.TOTAL_SWAP_SPACE, swapTotal),
                buildHostAttribute(SgeHostProperty.USED_SWAP_SPACE, swapUsed));

        return SgeHost.builder()
                .hostname(TEST_HOSTNAME)
                .hostAttributes(attributes)
                .build();
    }

    private static SgeHostValue buildHostAttribute(final SgeHostProperty property, final String value) {
        final SgeHostValue hostAttribute = new SgeHostValue();
        hostAttribute.setName(property);
        hostAttribute.setValue(value);
        return hostAttribute;
    }
}
