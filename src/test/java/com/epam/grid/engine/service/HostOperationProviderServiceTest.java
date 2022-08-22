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

package com.epam.grid.engine.service;

import com.epam.grid.engine.TestPropertiesWithSgeEngine;
import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.provider.host.HostProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

@SpringBootTest
@TestPropertiesWithSgeEngine
public class HostOperationProviderServiceTest {

    private static final String HOST_NAME = "test-ip";

    @Autowired
    HostOperationProviderService hostOperationProviderService;

    @MockBean
    HostProvider hostProvider;

    @Test
    public void shouldReturnCorrectResponse() {
        final Listing<Host> hostListing = new Listing<>(
                List.of(Host.builder()
                    .hostname(HOST_NAME)
                    .typeOfArchitect("lx-amd64")
                    .numOfProcessors(2)
                    .numOfSocket(1)
                    .numOfCore(1)
                    .numOfThread(2)
                    .load(0.0)
                    .memTotal(3600000000L)
                    .memUsed(311600000L)
                    .totalSwapSpace(0.0)
                    .usedSwapSpace(0.0)
                    .build()));

        final HostFilter hostFilter = new HostFilter();
        hostFilter.setHosts(List.of(HOST_NAME));

        doReturn(hostListing).when(hostProvider).listHosts(hostFilter);
        Assertions.assertEquals(hostListing, hostOperationProviderService.filter(hostFilter));
        Mockito.verify(hostProvider, times(1)).listHosts(hostFilter);
    }
}
