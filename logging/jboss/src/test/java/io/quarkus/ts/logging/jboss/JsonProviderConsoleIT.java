package io.quarkus.ts.logging.jboss;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7866")
@QuarkusScenario
public class JsonProviderConsoleIT {
    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.log.console.json.excluded-keys", "excludedField")
            .withProperty("json-provider-test.enabled", "true");

    @AfterEach
    void restartApp() {
        app.stop();
        app.start();
    }

    @Test
    void shouldWriteFieldsToConsoleLog() {
        app.given().get("/json-provider/info");

        app.logs().assertContains("\"message\":\"JSON Provider Info\"");
        app.logs().assertContains("\"customField\":\"customValue\"");
        app.logs().assertContains("\"loggerNameFromRecord\":\"json-provider-category\"");
        app.logs().assertContains("\"requestIdFromMdc\":\"request-123\"");
        app.logs().assertContains("\"nestedObject\":{\"nestedField1\":\"nestedValue1\",\"nestedField2\":\"nestedValue2\"}");
        app.logs().assertContains("\"serviceLoaderField\":\"serviceLoaderValue\"");
        app.logs().assertDoesNotContain("\"excludedField\"");
        app.logs().assertDoesNotContain(("\"isErrorRecord\""));
    }

    @Test
    void shouldWriteErrorFieldsToConsole() {
        app.given().get("/json-provider/error");

        app.logs().assertContains("\"message\":\"JSON Provider Error\"");
        app.logs().assertContains("\"isErrorRecord\":\"true\"");
        app.logs().assertDoesNotContain("\"requestIdFromMdc\"");
    }
}
