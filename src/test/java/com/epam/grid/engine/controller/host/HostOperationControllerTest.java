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

package com.epam.grid.engine.controller.host;

import com.epam.grid.engine.controller.AbstractControllerTest;
import com.epam.grid.engine.entity.Listing;
import com.epam.grid.engine.entity.host.Host;
import com.epam.grid.engine.service.HostOperationProviderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(HostController.class)
public class HostOperationControllerTest extends AbstractControllerTest {

    private static final String URI = "/hosts";

    @MockBean
    private HostOperationProviderService hostOperationProviderService;

    @Test
    public void shouldReturnStatusAndValue() throws Exception {
        final Host expectedHost = Host.builder()
                .hostname("test-ip")
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
                .build();

        final List<Host> hostList = Collections.singletonList(expectedHost);
        final Listing<Host> hostListing = new Listing<>();
        hostListing.setElements(hostList);
        Mockito.when(hostOperationProviderService.filter(null)).thenReturn(hostListing);

        final String response = performMvcRequest(MockMvcRequestBuilders.post(URI))
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(hostListing));
    }
}
