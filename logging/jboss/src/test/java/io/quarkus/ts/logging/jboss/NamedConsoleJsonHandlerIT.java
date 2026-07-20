package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6051")
@QuarkusScenario
public class NamedConsoleJsonHandlerIT {
    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.log.console.enabled", "true")
            .withProperty("quarkus.log.console.json.enabled", "false")
            .withProperty("quarkus.log.handler.console.\"json-console-handler\".enabled", "true")
            .withProperty("quarkus.log.handler.console.\"json-console-handler\".json.enabled", "true")
            .withProperty("quarkus.log.category.\"named-console-json-category\".handlers", "json-console-handler")
            .withProperty("quarkus.log.category.\"named-console-json-category\".use-parent-handlers", "false")
            .withProperty("quarkus.log.category.\"named-console-json-category\".level", "INFO");

    @Test
    void shouldUseJsonForNamedConsoleHandler() {
        app.given().get("/named-console/json");

        app.logs().assertContains("\"message\":\"Named Console Handler JSON\"");
        app.logs().assertContains("\"loggerName\":\"named-console-json-category\"");
        app.logs().assertContains("\"level\":\"INFO\"");
    }
}
