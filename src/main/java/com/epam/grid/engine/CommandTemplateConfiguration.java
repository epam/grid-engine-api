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

package com.epam.grid.engine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;

/**
 * This class performs the configuration of thymeleaf templates.
 */
@Configuration
public class CommandTemplateConfiguration {

    /**
     * This method insists the thymeleaf template engine.
     *
     * @param commandPath The path to the template folder.
     * @return Configured template engine.
     */
    @Bean
    public SpringTemplateEngine templateEngine(@Value("${command.template.path}") final String commandPath) {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(getResolverByCommandPath(commandPath));
        return templateEngine;
    }

    private AbstractConfigurableTemplateResolver getResolverByCommandPath(final String commandPath) {
        final File file = new File(commandPath);
        final AbstractConfigurableTemplateResolver resolver = file.exists() && file.isDirectory()
                ? new FileTemplateResolver()
                : new ClassLoaderTemplateResolver();
        resolver.setPrefix(commandPath);
        resolver.setTemplateMode(TemplateMode.TEXT);
        return resolver;
    }
}
