package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6051")
@QuarkusScenario
public class NamedSyslogJsonHandlerIT {
    @Container(image = "linuxserver/syslog-ng", port = 8514, expectedLog = ".*syslog-ng starting up")
    static RestService syslog = new RestService()
            .withProperty("LOG_TO_STDOUT", "true")
            .withProperty("_ignored", "resource_with_destination::/config/|syslog-ng.conf");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.log.syslog.enabled", "true")
            .withProperty("quarkus.log.syslog.json.enabled", "false")
            .withProperty("quarkus.log.handler.syslog.\"json-syslog-handler\".enabled", "true")
            .withProperty("quarkus.log.handler.syslog.\"json-syslog-handler\".endpoint", () -> syslog.getURI().toString())
            .withProperty("quarkus.log.handler.syslog.\"json-syslog-handler\".protocol", "tcp")
            .withProperty("quarkus.log.handler.syslog.\"json-syslog-handler\".json.enabled", "true")
            .withProperty("quarkus.log.category.\"named-syslog-json-category\".handlers", "json-syslog-handler")
            .withProperty("quarkus.log.category.\"named-syslog-json-category\".use-parent-handlers", "false")
            .withProperty("quarkus.log.category.\"named-syslog-json-category\".level", "INFO");

    @Test
    void shouldUseJsonForNamedSyslogHandler() {
        app.given().get("/named-syslog/json");

        syslog.logs().assertContains("\"message\":\"Named Syslog Handler JSON\"");
        syslog.logs().assertContains("\"loggerName\":\"named-syslog-json-category\"");
        syslog.logs().assertContains("\"level\":\"INFO\"");
    }

}
