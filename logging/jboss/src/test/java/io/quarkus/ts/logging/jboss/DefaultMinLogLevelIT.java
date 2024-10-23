package io.quarkus.ts.logging.jboss;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.FileUtils;

@QuarkusScenario
public class DefaultMinLogLevelIT {

    private static final String LOG_FILE_NAME = "quarkus.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String EXPECTED_LOG_MESSAGE = "Example log message";

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("default.properties");

    @Test
    public void checkDefaultLogMinLevel() {
        app.given().when().get("/log").then().statusCode(204);

        app.logs().assertContains("Fatal log example");
        app.logs().assertContains("Error log example");
        app.logs().assertContains("Warn log example");
        app.logs().assertContains("Info log example");
        app.logs().assertContains("Debug log example");

        // the value of minimum logging level overrides the logging level
        app.logs().assertDoesNotContain("Trace log example");
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/issues/40016")
    public void checkLogRotationContent() throws IOException {
        Path logDir = app.getServiceFolder();
        // one main log file plus others rotated
        int logFilesNumber = Integer.parseInt(app.getProperty("quarkus.log.file.rotation.max-backup-index").get()) + 1;

        app.given().when().get("/log").then().statusCode(204);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
            File[] rotatedFiles = logDir.toFile().listFiles((dir, name) -> name.startsWith(LOG_FILE_NAME));
            return rotatedFiles != null && rotatedFiles.length == logFilesNumber;
        });

        // Verify that the rotated file with the expected suffix exists
        String expectedSuffix = "." + DATE_FORMATTER.format(LocalDate.now()) + "\\.\\d+$";
        List<Path> rotatedFiles = Files.list(logDir)
                .filter(path -> path.getFileName().toString().matches(LOG_FILE_NAME + expectedSuffix)).toList();

        assertFalse(rotatedFiles.isEmpty(), "Rotated log files with expected suffix were not found.");

        // Check if rotated files contain the log message
        boolean allFilesContainMessage = rotatedFiles.stream()
                .allMatch(rotatedFile -> {
                    String fileContent = FileUtils.loadFile(rotatedFile.toFile());
                    return fileContent.contains(EXPECTED_LOG_MESSAGE);
                });

        assertTrue(allFilesContainMessage, "Rotated log files do not contain the expected message " + EXPECTED_LOG_MESSAGE);
    }
}
