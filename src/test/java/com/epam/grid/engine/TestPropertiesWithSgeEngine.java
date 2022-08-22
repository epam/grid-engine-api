package com.epam.grid.engine;

import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource(locations = "classpath:test-application.properties", properties = {"grid.engine.type=SGE"})
public @interface TestPropertiesWithSgeEngine {
}
