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

import com.epam.grid.engine.entity.HostFilter;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.provider.host.sge.SgeHostProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

@SpringBootTest(properties = {"grid.engine.type=SGE"})
public class HostOperationProviderServiceTest {

    private static final String TEST = "test-ip";
    private static final String TYPE = "lx-amd64";

    @Autowired
    HostOperationProviderService hostOperationProviderService;

    @SpyBean
    SgeHostProvider hostProvider;

    @Test
    public void shouldReturnCorrectResponse() {
        final Listing<Host> hostListing = listingParser(listParser());
        final HostFilter hostFilter = new HostFilter();
        hostFilter.setHosts(Collections.singletonList(TEST));

        doReturn(hostListing).when(hostProvider).listHosts(hostFilter);
        Assertions.assertEquals(hostListing, hostOperationProviderService.filter(hostFilter));
        Mockito.verify(hostProvider, times(1)).listHosts(hostFilter);
    }

    private static List<Host> listParser() {
        return Collections.singletonList(Host.builder()
                .hostname(TEST)
                .typeOfArchitect(TYPE)
                .numOfProcessors(2)
                .numOfSocket(1)
                .numOfCore(1)
                .numOfThread(2)
                .load(0.0)
                .memTotal(3600000000L)
                .memUsed(311600000L)
                .totalSwapSpace(0.0)
                .usedSwapSpace(0.0)
                .build());
    }

    private static Listing<Host> listingParser(final List<Host> hosts) {
        return new Listing<>(hosts.stream()
                .map(host -> Host.builder()
                        .hostname(host.getHostname())
                        .typeOfArchitect(host.getTypeOfArchitect())
                        .numOfProcessors(host.getNumOfProcessors())
                        .numOfSocket(host.getNumOfSocket())
                        .numOfCore(host.getNumOfCore())
                        .numOfThread(host.getNumOfThread())
                        .load(host.getLoad())
                        .memTotal(host.getMemTotal())
                        .memUsed(host.getMemUsed())
                        .totalSwapSpace(host.getTotalSwapSpace())
                        .usedSwapSpace(host.getUsedSwapSpace())
                        .build())
                .collect(Collectors.toList()));
    }
}
