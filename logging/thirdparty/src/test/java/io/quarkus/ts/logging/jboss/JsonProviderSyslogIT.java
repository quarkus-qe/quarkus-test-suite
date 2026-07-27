package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7866")
@QuarkusScenario
public class JsonProviderSyslogIT {
    @Container(image = "linuxserver/syslog-ng", port = 8514, expectedLog = ".*syslog-ng starting up")
    static RestService syslog = new RestService()
            .withProperty("LOG_TO_STDOUT", "true")
            .withProperty("_ignored", "resource_with_destination::/config/|syslog-ng.conf");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.log.syslog.enabled", "true")
            .withProperty("quarkus.log.syslog.json.enabled", "true")
            .withProperty("quarkus.log.syslog.endpoint", () -> syslog.getURI().toString())
            .withProperty("quarkus.log.syslog.protocol", "tcp")
            .withProperty("quarkus.log.syslog.json.excluded-keys", "excludedField")
            .withProperty("json-provider-test.enabled", "true");

    @AfterEach
    void restartServices() {
        syslog.stop();
        syslog.start();
        app.stop();
        app.start();
    }

    @Test
    void shouldWriteFieldsToSyslog() {
        app.given().get("/json-provider/info");

        syslog.logs().assertContains("\"message\":\"JSON Provider Info\"");
        syslog.logs().assertContains("\"customField\":\"customValue\"");
        syslog.logs().assertContains("\"loggerNameFromRecord\":\"json-provider-category\"");
        syslog.logs().assertContains("\"requestIdFromMdc\":\"request-123\"");
        syslog.logs().assertContains("\"nestedObject\":{\"nestedField1\":\"nestedValue1\",\"nestedField2\":\"nestedValue2\"}");
        syslog.logs().assertDoesNotContain("\"excludedField\"");
        syslog.logs().assertDoesNotContain("\"isErrorRecord\"");
    }

    @Test
    void shouldWriteErrorFieldsToSyslog() {
        app.given().get("/json-provider/error");

        syslog.logs().assertContains("\"message\":\"JSON Provider Error\"");
        syslog.logs().assertContains("\"isErrorRecord\":\"true\"");
        syslog.logs().assertDoesNotContain("\"requestIdFromMdc\"");
    }
}
