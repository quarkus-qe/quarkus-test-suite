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
public class NamedFilePlainHandlerIT {
    private static final String LOG_FILE_NAME = "named-file-plain.log";
    private static final Path LOG_FILE_DIR = Path.of("target/NamedFilePlainHandlerIT/app");

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.log.file.json.enabled", "true")
            .withProperty("quarkus.log.handler.file.\"plain-file-handler\".enabled", "true")
            .withProperty("quarkus.log.handler.file.\"plain-file-handler\".path", LOG_FILE_NAME)
            .withProperty("quarkus.log.handler.file.\"plain-file-handler\".json.enabled", "false")
            .withProperty("quarkus.log.handler.file.\"plain-file-handler\".format", "PLAIN:%p:%c:%s%n")
            .withProperty("quarkus.log.category.\"named-file-plain-category\".handlers", "plain-file-handler")
            .withProperty("quarkus.log.category.\"named-file-plain-category\".use-parent-handlers", "false")
            .withProperty("quarkus.log.category.\"named-file-plain-category\".level", "INFO");

    @AfterAll
    static void cleanup() throws IOException {
        app.stop();
        Files.deleteIfExists(LOG_FILE_DIR.resolve(LOG_FILE_NAME));
    }

    @Test
    public void shouldUsePlainFormatForNamedFileHandler() throws IOException {
        app.given().get("/named-file/plain");

        Path logFile = LOG_FILE_DIR.resolve(LOG_FILE_NAME);

        assertTrue(Files.exists(logFile));

        String logContent = Files.readString(logFile);

        assertTrue(logContent.contains("PLAIN:INFO:named-file-plain-category:Named File Handler Plain"));
    }
}
