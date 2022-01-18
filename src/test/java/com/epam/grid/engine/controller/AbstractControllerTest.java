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

import com.epam.grid.engine.exception.GridEngineException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractControllerTest {

    private static final String EXPECTED_CONTENT_TYPE = "application/json;charset=UTF-8";
    protected ObjectMapper objectMapper = createObjectMapper();

    @Autowired
    protected MockMvc mvc;

    public MvcResult performMvcRequest(final MockHttpServletRequestBuilder servletRequestBuilder) throws Exception {
        return mvc.perform(servletRequestBuilder
                .accept(EXPECTED_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
                .andReturn();
    }

    public MvcResult performMvcResultWithContent(
            final MockHttpServletRequestBuilder servletRequestBuilder, final Object bodyObject
    ) throws Exception {
        return mvc.perform(servletRequestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(bodyObject))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    public ObjectMapper createObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (final Exception e) {
            throw new GridEngineException(HttpStatus.NOT_FOUND,
                    "something went wrong during conversion object to JSON String", e);
        }
    }
}
