package io.quarkus.ts.logging.jboss;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7866")
@QuarkusScenario
public class JsonProviderFileIT {
    private static final String LOG_FILE_NAME = "json-provider.log";
    private static final Path LOG_FILE_DIR = Path.of("target/JsonProviderFileIT/app");

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.log.file.enabled", "true")
            .withProperty("quarkus.log.file.path", LOG_FILE_NAME)
            .withProperty("quarkus.log.file.json.excluded-keys", "excludedField")
            .withProperty("json-provider-test.enabled", "true");

    @AfterAll
    static void cleanup() throws IOException {
        app.stop();
        Files.deleteIfExists(LOG_FILE_DIR.resolve(LOG_FILE_NAME));
    }

    @AfterEach
    void restartApp() {
        app.stop();
        app.start();
    }

    @Test
    void shouldWriteFieldsToFile() throws IOException {
        app.given().get("/json-provider/info");

        Path logFile = LOG_FILE_DIR.resolve(LOG_FILE_NAME);
        assertTrue(Files.exists(logFile));
        String log = Files.readString(logFile);

        assertTrue(log.contains("\"message\":\"JSON Provider Info\""));
        assertTrue(log.contains("\"customField\":\"customValue\""));
        assertTrue(log.contains("\"loggerNameFromRecord\":\"json-provider-category\""));
        assertTrue(log.contains("\"requestIdFromMdc\":\"request-123\""));
        assertTrue(log.contains("\"nestedObject\":{\"nestedField1\":\"nestedValue1\",\"nestedField2\":\"nestedValue2\"}"));
        assertFalse(log.contains("\"excludedField\""));
        assertFalse(log.contains("\"isErrorRecord\""));
    }

    @Test
    void shouldWriteErrorFieldsToFile() throws IOException {
        app.given().get("/json-provider/error");

        Path logFile = LOG_FILE_DIR.resolve(LOG_FILE_NAME);
        assertTrue(Files.exists(logFile));
        String log = Files.readString(logFile);

        assertTrue(log.contains("\"message\":\"JSON Provider Error\""));
        assertTrue(log.contains("\"isErrorRecord\":\"true\""));
        assertFalse(log.contains("\"requestIdFromMdc\""));
    }
}
