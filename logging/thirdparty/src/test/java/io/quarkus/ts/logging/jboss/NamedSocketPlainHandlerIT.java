package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6051")
@QuarkusScenario
public class NamedSocketPlainHandlerIT {

    @Container(image = "linuxserver/syslog-ng", port = 8514, expectedLog = ".*syslog-ng starting up")
    static RestService receiver = new RestService()
            .withProperty("LOG_TO_STDOUT", "true")
            .withProperty("_ignored", "resource_with_destination::/config/|syslog-ng.conf");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.log.socket.enabled", "true")
            .withProperty("quarkus.log.socket.json.enabled", "true")
            .withProperty("quarkus.log.handler.socket.\"plain-socket-handler\".enabled", "true")
            .withProperty("quarkus.log.handler.socket.\"plain-socket-handler\".endpoint", () -> receiver.getURI().toString())
            .withProperty("quarkus.log.handler.socket.\"plain-socket-handler\".protocol", "tcp")
            .withProperty("quarkus.log.handler.socket.\"plain-socket-handler\".json.enabled", "false")
            .withProperty("quarkus.log.handler.socket.\"plain-socket-handler\".format", "PLAIN:%p:%c:%s%n")
            .withProperty("quarkus.log.category.\"named-socket-plain-category\".handlers", "plain-socket-handler")
            .withProperty("quarkus.log.category.\"named-socket-plain-category\".use-parent-handlers", "false")
            .withProperty("quarkus.log.category.\"named-socket-plain-category\".level", "INFO");

    @Test
    void shouldUsePlainForNamedSocketHandler() {
        app.given().get("/named-socket/plain");

        receiver.logs().assertContains("PLAIN:INFO:named-socket-plain-category:Named Socket Handler Plain");
    }
}