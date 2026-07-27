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
public class JsonProviderSocketIT {
    @Container(image = "linuxserver/syslog-ng", port = 8514, expectedLog = ".*syslog-ng starting up")
    static RestService receiver = new RestService()
            .withProperty("LOG_TO_STDOUT", "true")
            .withProperty("_ignored", "resource_with_destination::/config/|syslog-ng.conf");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.log.socket.enabled", "true")
            .withProperty("quarkus.log.socket.json.enabled", "true")
            .withProperty("quarkus.log.socket.endpoint", () -> receiver.getURI().toString())
            .withProperty("quarkus.log.socket.protocol", "tcp")
            .withProperty("quarkus.log.socket.json.excluded-keys", "excludedField")
            .withProperty("json-provider-test.enabled", "true");

    @AfterEach
    void restartServices() {
        receiver.stop();
        receiver.start();
        app.stop();
        app.start();
    }

    @Test
    void shouldWriteFieldsToSocket() {
        app.given().get("/json-provider/info");

        receiver.logs().assertContains("\"message\":\"JSON Provider Info\"");
        receiver.logs().assertContains("\"customField\":\"customValue\"");
        receiver.logs().assertContains("\"loggerNameFromRecord\":\"json-provider-category\"");
        receiver.logs().assertContains("\"requestIdFromMdc\":\"request-123\"");
        receiver.logs()
                .assertContains("\"nestedObject\":{\"nestedField1\":\"nestedValue1\",\"nestedField2\":\"nestedValue2\"}");
        receiver.logs().assertDoesNotContain("\"excludedField\"");
        receiver.logs().assertDoesNotContain("\"isErrorRecord\"");
    }

    @Test
    void shouldWriteErrorFieldsToSocket() {
        app.given().get("/json-provider/error");

        receiver.logs().assertContains("\"message\":\"JSON Provider Error\"");
        receiver.logs().assertContains("\"isErrorRecord\":\"true\"");
        receiver.logs().assertDoesNotContain("\"requestIdFromMdc\"");
    }
}
