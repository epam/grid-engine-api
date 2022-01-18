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

package com.epam.grid.engine.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractRestControllerTest {

    private static final String SOME_FILE_NAME = "test_file.txt";
    private static final String CONTENT_DISPOSITION_HEADER = "attachment;filename=" + SOME_FILE_NAME;
    private static final String TEST_DATA = "There is some data to test a response.";

    @Test
    public void shouldSendCorrectDataToResponse() throws IOException {
        final AbstractRestController abstractController = Mockito.spy(AbstractRestController.class);

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final InputStream testInputStream = new ByteArrayInputStream(TEST_DATA.getBytes());

        abstractController.writeStreamToResponse(response, testInputStream, SOME_FILE_NAME);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM.toString(), response.getContentType());
        assertEquals(CONTENT_DISPOSITION_HEADER, response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(TEST_DATA, response.getContentAsString());
    }
}
