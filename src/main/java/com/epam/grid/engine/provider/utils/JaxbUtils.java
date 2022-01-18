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

package com.epam.grid.engine.provider.utils;

import com.epam.grid.engine.exception.GridEngineException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.http.HttpStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.StringReader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JaxbUtils {

    public static <T> T unmarshall(final String xml, final Class<T> clazz) {

        try {
            final JAXBContext context = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (final JAXBException e) {
            throw new GridEngineException(HttpStatus.NOT_FOUND,
                    "Some problems during unmarshalling XML data", e);
        }
    }
}
