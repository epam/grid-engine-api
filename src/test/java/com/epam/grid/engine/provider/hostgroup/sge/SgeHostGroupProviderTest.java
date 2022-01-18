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

package com.epam.grid.engine.provider.hostgroup.sge;

import com.epam.grid.engine.cmd.SimpleCmdExecutor;
import com.epam.grid.engine.entity.CommandResult;
import com.epam.grid.engine.entity.HostGroupFilter;
import com.epam.grid.engine.entity.hostgroup.HostGroup;
import com.epam.grid.engine.exception.GridEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static com.epam.grid.engine.provider.utils.sge.TestSgeConstants.SINGLETON_LIST_WITH_STANDARD_WARN;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
public class SgeHostGroupProviderTest {

    private static final String QCONF_COMMAND = "qconf";
    private static final String SHGRP_OPTION = "-shgrp";
    private static final String HOST_GROUP_NAME = "@allhosts";
    private static final String WRONG_HOST_GROUP_NAME = "allhosts";
    private static final List<String> HOST_GROUP_VALID_OUTPUT = List.of(
            "group_name @allhosts",
            "hostlist 0447c6c3047c");
    private static final HostGroup HOST_GROUP_EXPECTED = HostGroup.builder()
            .hostGroupName("@allhosts")
            .hostGroupEntry(List.of("0447c6c3047c"))
            .build();

    @MockBean
    private SimpleCmdExecutor mockCmdExecutor;

    @Autowired
    private SgeHostGroupProvider sgeHostGroupProvider;

    @Test
    public void shouldWorkWithCorrectFilter() {
        final CommandResult commandResult = new CommandResult();
        commandResult.setStdOut(HOST_GROUP_VALID_OUTPUT);
        commandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);
        doReturn(commandResult).when(mockCmdExecutor).execute(QCONF_COMMAND, SHGRP_OPTION, HOST_GROUP_NAME);
        final HostGroupFilter hostGroupFilter = new HostGroupFilter();
        hostGroupFilter.setHostGroupNames(Collections.singletonList(HOST_GROUP_NAME));
        final List<HostGroup> result = sgeHostGroupProvider.listHostGroups(hostGroupFilter);
        Assertions.assertEquals(HOST_GROUP_EXPECTED, result.get(0));
    }

    @Test
    public void shouldWorkWithCorrectHostGroupName() {
        final CommandResult commandResult = new CommandResult();
        commandResult.setStdOut(HOST_GROUP_VALID_OUTPUT);
        commandResult.setStdErr(SINGLETON_LIST_WITH_STANDARD_WARN);
        doReturn(commandResult).when(mockCmdExecutor).execute(QCONF_COMMAND, SHGRP_OPTION, HOST_GROUP_NAME);
        final HostGroup result = sgeHostGroupProvider.getHostGroup(HOST_GROUP_NAME);
        Assertions.assertEquals(HOST_GROUP_EXPECTED, result);
    }

    @Test
    public void shouldThrowExceptionWhenHostGroupNameIsIncorrect() {
        Assertions.assertThrows(GridEngineException.class,
                () -> sgeHostGroupProvider.getHostGroup(WRONG_HOST_GROUP_NAME));
    }
}

