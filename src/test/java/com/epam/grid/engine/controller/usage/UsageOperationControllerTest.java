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

package com.epam.grid.engine.controller.usage;

import com.epam.grid.engine.controller.AbstractControllerTest;
import com.epam.grid.engine.entity.usage.UsageReport;
import com.epam.grid.engine.entity.usage.UsageReportFilter;
import com.epam.grid.engine.service.UsageOperationProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@WebMvcTest(UsageOperationController.class)
public class UsageOperationControllerTest extends AbstractControllerTest {

    private static final String URI = "/usage";

    @MockBean
    private UsageOperationProviderService usageOperationProviderService;

    @Test
    public void shouldReturnJsonValueAndOkStatus() throws Exception {
        final UsageReport expectedUsageReport = getUsageReport();
        doReturn(expectedUsageReport).when(usageOperationProviderService).getUsageReport(new UsageReportFilter());

        final MvcResult mvcResult = performMvcResultWithContent(MockMvcRequestBuilders.post(URI),
                new UsageReportFilter());
        verify(usageOperationProviderService).getUsageReport(new UsageReportFilter());
        final String actual = mvcResult.getResponse().getContentAsString();
        final String current = removeNulls(objectMapper.writeValueAsString(expectedUsageReport));
        assertThat(actual).isEqualToIgnoringWhitespace(current);
    }

    private String removeNulls(final String writeValueAsString) {
        final int wallClockIndex = writeValueAsString.indexOf("wallClock");
        if (wallClockIndex != 0) {
            return "{\"" + writeValueAsString.substring(wallClockIndex);
        }
        return writeValueAsString;
    }

    private UsageReport getUsageReport() {
        return new UsageReport(5, 0.628, 0.377, 1.005, 0, 0, 0);
    }
}
