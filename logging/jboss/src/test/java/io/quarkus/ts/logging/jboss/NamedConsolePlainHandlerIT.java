package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6051")
@QuarkusScenario
public class NamedConsolePlainHandlerIT {
    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.log.console.enabled", "true")
            .withProperty("quarkus.log.console.json.enabled", "true")
            .withProperty("quarkus.log.handler.console.\"plain-console-handler\".enabled", "true")
            .withProperty("quarkus.log.handler.console.\"plain-console-handler\".json.enabled", "false")
            .withProperty("quarkus.log.handler.console.\"plain-console-handler\".format", "PLAIN:%p:%c:%s%n")
            .withProperty("quarkus.log.category.\"named-console-plain-category\".handlers", "plain-console-handler")
            .withProperty("quarkus.log.category.\"named-console-plain-category\".use-parent-handlers", "false")
            .withProperty("quarkus.log.category.\"named-console-plain-category\".level", "INFO");

    @Test
    void shouldUsePlainForNamedConsoleHandler() {
        app.given().get("/named-console/plain");

        app.logs().assertContains("PLAIN:INFO:named-console-plain-category:Named Console Handler Plain");
    }
}
