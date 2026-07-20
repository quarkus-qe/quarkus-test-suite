package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6051")
@QuarkusScenario
public class NamedSocketJsonHandlerIT {

    @Container(image = "linuxserver/syslog-ng", port = 8514, expectedLog = ".*syslog-ng starting up")
    static RestService receiver = new RestService()
            .withProperty("LOG_TO_STDOUT", "true")
            .withProperty("_ignored", "resource_with_destination::/config/|syslog-ng.conf");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.log.socket.enabled", "true")
            .withProperty("quarkus.log.socket.json.enabled", "false")
            .withProperty("quarkus.log.handler.socket.\"json-socket-handler\".enabled", "true")
            .withProperty("quarkus.log.handler.socket.\"json-socket-handler\".endpoint", () -> receiver.getURI().toString())
            .withProperty("quarkus.log.handler.socket.\"json-socket-handler\".protocol", "tcp")
            .withProperty("quarkus.log.handler.socket.\"json-socket-handler\".json.enabled", "true")
            .withProperty("quarkus.log.category.\"named-socket-json-category\".handlers", "json-socket-handler")
            .withProperty("quarkus.log.category.\"named-socket-json-category\".use-parent-handlers", "false")
            .withProperty("quarkus.log.category.\"named-socket-json-category\".level", "INFO");

    @Test
    void shouldUseJsonForNamedSocketHandler() {
        app.given().get("/named-socket/json");

        receiver.logs().assertContains("\"message\":\"Named Socket Handler JSON\"");
        receiver.logs().assertContains("\"loggerName\":\"named-socket-json-category\"");
        receiver.logs().assertContains("\"level\":\"INFO\"");
    }
}