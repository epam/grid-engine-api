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
import com.epam.grid.engine.entity.HostGroupFilter;
import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.provider.hostgroup.HostGroupProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertiesWithSgeEngine
public class HostGroupOperationProviderServiceTest {

    private static final String HOST_GROUP_NAME = "@allhosts";
    private static final String HOST_GROUP_ENTRY = "0447c6c3047c";

    @Autowired
    private HostGroupOperationProviderService hostGroupOperationProviderService;

    @MockBean
    private HostGroupProvider hostGroupProvider;

    @Test
    public void shouldReturnCorrectResponse() {
        final List<HostGroup> hostGroups = Collections.singletonList(HostGroup.builder()
                .hostGroupName(HOST_GROUP_NAME)
                .hostGroupEntry(Collections.singletonList(HOST_GROUP_ENTRY))
                .build());

        final HostGroupFilter hostGroupFilter = new HostGroupFilter();
        hostGroupFilter.setHostGroupNames(Collections.singletonList(HOST_GROUP_NAME));

        doReturn(hostGroups).when(hostGroupProvider).listHostGroups(hostGroupFilter);
        Assertions.assertEquals(hostGroups, hostGroupOperationProviderService.listHostGroups(hostGroupFilter));
        verify(hostGroupProvider, times(1)).listHostGroups(hostGroupFilter);
    }

    @Test
    public void shouldReturnCorrectResponseWhenHostGroupNameIsFill() {
        final HostGroup hostGroup = HostGroup.builder()
                .hostGroupName(HOST_GROUP_NAME)
                .hostGroupEntry(Collections.singletonList(HOST_GROUP_ENTRY))
                .build();
        doReturn(hostGroup).when(hostGroupProvider).getHostGroup(HOST_GROUP_NAME);
        Assertions.assertEquals(hostGroup, hostGroupOperationProviderService.getHostGroup(HOST_GROUP_NAME));
        verify(hostGroupProvider, times(1)).getHostGroup(HOST_GROUP_NAME);
    }
}
