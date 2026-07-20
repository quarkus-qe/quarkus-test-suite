package io.quarkus.ts.logging.jboss;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6051")
@QuarkusScenario
public class NamedFileJsonHandlerIT {
    private static final String LOG_FILE_NAME = "named-file-json.log";
    private static final Path LOG_FILE_DIR = Path.of("target/NamedFileJsonHandlerIT/app");

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.log.file.json.enabled", "false")
            .withProperty("quarkus.log.handler.file.\"json-file-handler\".enabled", "true")
            .withProperty("quarkus.log.handler.file.\"json-file-handler\".path", LOG_FILE_NAME)
            .withProperty("quarkus.log.handler.file.\"json-file-handler\".json.enabled", "true")
            .withProperty("quarkus.log.category.\"named-file-json-category\".handlers", "json-file-handler")
            .withProperty("quarkus.log.category.\"named-file-json-category\".use-parent-handlers", "false")
            .withProperty("quarkus.log.category.\"named-file-json-category\".level", "INFO");

    @AfterAll
    static void cleanup() throws IOException {
        app.stop();
        Files.deleteIfExists(LOG_FILE_DIR.resolve(LOG_FILE_NAME));
    }

    @Test
    public void shouldUseJsonForNamedFileHandler() throws IOException {
        app.given().get("/named-file/json");

        Path logFile = LOG_FILE_DIR.resolve(LOG_FILE_NAME);

        assertTrue(Files.exists(logFile));

        String logContent = Files.readString(logFile);

        assertTrue(logContent.contains("\"message\":\"Named File Handler JSON\""));
        assertTrue(logContent.contains("\"loggerName\":\"named-file-json-category\""));
        assertTrue(logContent.contains("\"level\":\"INFO\""));
    }
}
