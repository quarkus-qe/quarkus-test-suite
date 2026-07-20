package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6051")
@QuarkusScenario
public class NamedSyslogPlainHandlerIT {

    @Container(image = "linuxserver/syslog-ng", port = 8514, expectedLog = ".*syslog-ng starting up")
    static RestService syslog = new RestService()
            .withProperty("LOG_TO_STDOUT", "true")
            .withProperty("_ignored", "resource_with_destination::/config/|syslog-ng.conf");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.log.syslog.enabled", "true")
            .withProperty("quarkus.log.syslog.json.enabled", "true")
            .withProperty("quarkus.log.handler.syslog.\"plain-syslog-handler\".enabled", "true")
            .withProperty("quarkus.log.handler.syslog.\"plain-syslog-handler\".endpoint", () -> syslog.getURI().toString())
            .withProperty("quarkus.log.handler.syslog.\"plain-syslog-handler\".protocol", "tcp")
            .withProperty("quarkus.log.handler.syslog.\"plain-syslog-handler\".json.enabled", "false")
            .withProperty("quarkus.log.handler.syslog.\"plain-syslog-handler\".format", "PLAIN:%p:%c:%s%n")
            .withProperty("quarkus.log.category.\"named-syslog-plain-category\".handlers", "plain-syslog-handler")
            .withProperty("quarkus.log.category.\"named-syslog-plain-category\".use-parent-handlers", "false")
            .withProperty("quarkus.log.category.\"named-syslog-plain-category\".level", "INFO");

    @Test
    void shouldUsePlainForNamedSyslogHandler() {
        app.given().get("/named-syslog/plain");

        syslog.logs().assertContains("PLAIN:INFO:named-syslog-plain-category:Named Syslog Handler Plain");
    }
}