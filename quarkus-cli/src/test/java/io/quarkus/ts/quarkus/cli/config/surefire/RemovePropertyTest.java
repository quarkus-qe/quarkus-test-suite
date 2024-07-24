package io.quarkus.ts.quarkus.cli.config.surefire;

import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * This test is only supposed to run inside QuarkusCliConfigRemoveIT.
 */
@QuarkusTest
public class RemovePropertyTest {

    public static String TODO_PROPERTY_NAME = "todo";

    @Inject
    Config config;

    @Test
    void testTodoPropertyIsMissing() {
        assertFalse(config.getOptionalValue(TODO_PROPERTY_NAME, String.class).isPresent());
    }

}
