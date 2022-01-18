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

package com.epam.grid.engine.controller.hostgroup;

import com.epam.grid.engine.controller.AbstractControllerTest;
import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.service.HostGroupOperationProviderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(HostGroupOperationController.class)
public class HostGroupOperationControllerTest extends AbstractControllerTest {

    private static final String URI = "/clusters/filter";
    private static final String HOST_GROUP_NAME = "@allhosts";
    private static final String HOST_GROUP_ENTRY = "0447c6c3047c";
    private static final String URI_HOST_GROUP = "/clusters/@allhosts";

    @MockBean
    HostGroupOperationProviderService hostGroupOperationProviderService;

    @Test
    public void shouldReturnStatusOkAndJsonVAlue() throws Exception {
        final HostGroup expectedHostGroup = HostGroup.builder()
                .hostGroupEntry(Collections.singletonList(HOST_GROUP_NAME))
                .hostGroupEntry(Collections.singletonList(HOST_GROUP_ENTRY))
                .build();
        final List<HostGroup> expectedResult = Collections.singletonList(expectedHostGroup);
        Mockito.when(hostGroupOperationProviderService.listHostGroups(null)).thenReturn(expectedResult);
        final String response = performMvcRequest(MockMvcRequestBuilders.post(URI))
                .getResponse()
                .getContentAsString();
        assertThat(response).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedResult));
    }

    @Test
    public void shouldReturnStatusOkAndJsonValueWhenHostGroupNameFill() throws Exception {
        final HostGroup expectedHostGroup = HostGroup.builder()
                .hostGroupEntry(Collections.singletonList(HOST_GROUP_NAME))
                .hostGroupEntry(Collections.singletonList(HOST_GROUP_ENTRY))
                .build();
        Mockito.when(hostGroupOperationProviderService.getHostGroup(HOST_GROUP_NAME)).thenReturn(expectedHostGroup);
        final String response = performMvcRequest(MockMvcRequestBuilders.get(URI_HOST_GROUP))
                .getResponse()
                .getContentAsString();
        assertThat(response).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedHostGroup));
    }
}
