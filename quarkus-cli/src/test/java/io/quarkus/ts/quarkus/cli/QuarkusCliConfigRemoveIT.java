package io.quarkus.ts.quarkus.cli;

import static io.quarkus.ts.quarkus.cli.config.surefire.RemovePropertyTest.TODO_PROPERTY_NAME;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.config.QuarkusConfigCommand;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.ts.quarkus.cli.config.surefire.RemovePropertyTest;

@DisabledOnQuarkusVersion(version = "3\\.(9|10|11|12)\\..*", reason = "https://github.com/quarkusio/quarkus/pull/41203 merged in 3.13")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // remember, this is stateful test as well as stateful cmd builder
@Tag("QUARKUS-3456")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public class QuarkusCliConfigRemoveIT {

    @Inject
    static QuarkusConfigCommand configCommand;

    @Order(1)
    @Test
    public void tryToRemoveProperty() {
        configCommand
                .removeProperty()
                .name(TODO_PROPERTY_NAME)
                .executeCommand()
                .assertCommandOutputContains("""
                        Could not find configuration %s
                        """.formatted(TODO_PROPERTY_NAME))
                .assertApplicationPropertiesDoesNotContain(TODO_PROPERTY_NAME);
    }

    @Order(2)
    @Test
    public void addAndRemoveProperty() {
        // property removal is assured by RemovePropertyTest#testTodoPropertyIsMissing
        configCommand.addToApplicationPropertiesFile(TODO_PROPERTY_NAME, "nice");
        configCommand
                .removeProperty()
                .name(TODO_PROPERTY_NAME)
                .executeCommand()
                .assertCommandOutputContains("""
                        Removing configuration %s
                        """.formatted(TODO_PROPERTY_NAME))
                .assertApplicationPropertiesDoesNotContain(TODO_PROPERTY_NAME);
    }

    @Order(3)
    @Test
    public void testRemoveCommandHelp() {
        configCommand
                .removeProperty()
                .printOutHelp()
                .assertCommandOutputContains("""
                        Removes a configuration from application.properties
                        """)
                .assertCommandOutputContains("""
                        Usage: quarkus config remove [-eh] [--verbose] NAME
                        """)
                .assertCommandOutputContains("""
                        Configuration name
                        """)
                .assertCommandOutputContains("""
                        Print more context on errors and exceptions
                        """)
                .assertCommandOutputContains("""
                        Display this help message
                        """)
                .assertCommandOutputContains("""
                        Verbose mode
                        """);
    }

    @Order(4)
    @Test
    public void testQuarkusApplicationWithRemovedApplicationProperties() {
        configCommand.buildAppAndExpectSuccess(RemovePropertyTest.class);
    }

}
