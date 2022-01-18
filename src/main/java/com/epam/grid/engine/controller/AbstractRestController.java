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

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractRestController {
    /**
     * Writes passed content to {@code HttpServletResponse} to allow it's downloading from the client.
     *
     * @param response The object to write data
     * @param stream   The file data stream
     * @param fileName The file name
     * @throws IOException if an I/O error occurs
     */
    protected void writeStreamToResponse(final HttpServletResponse response, final InputStream stream,
                                         final String fileName) throws IOException {
        try (InputStream s = stream) {
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=%s", fileName));
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM.toString());

            IOUtils.copy(s, response.getOutputStream());
            response.flushBuffer();
        }
    }
}
