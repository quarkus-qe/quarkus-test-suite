package io.quarkus.ts.logging.jboss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("https://issues.redhat.com/browse/QUARKUS-4663")
public class AccessLoggingIT {
    private static final String ACCESS_LOG_FILENAME = "appAccess.log";
    private static final Path ACCESS_LOG_DIR = Path.of("target/AccessLoggingIT/app");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("accessLogging.properties")
            .withProperty("quarkus.log.handler.file.access-log.path", ACCESS_LOG_FILENAME);

    /**
     * We want each test to start with no log files from previous tests, so remove them;
     */
    @AfterEach
    public void removeAccessLogs() throws IOException {
        // App throws exception if we delete log file it is currently working with.
        // Thus, we need to stop the app while manipulating with log files.
        app.stop();

        List<Path> accessLogFiles = Files.list(ACCESS_LOG_DIR)
                .filter(file -> file.getFileName().toString().startsWith(ACCESS_LOG_FILENAME))
                .toList();
        for (Path file : accessLogFiles) {
            Files.delete(file);
        }

        app.start();
    }

    @Test
    public void accessTest() throws IOException {
        sendRequests(3);

        assertTrue(Files.exists(accessLogPath()), "There should be an access log file.");
        // check that one request actually match one line in a log file
        assertEquals(3, Files.lines(accessLogPath()).count(), "There should be 3 lines in access log.");

        assertEquals(1, getLogFileNames().size(), "There should be just one log file");
    }

    @Test
    public void logRotationTest() throws IOException {
        // use zip compression for archived logs
        setPropertyTo("quarkus.log.handler.file.access-log.rotation.file-suffix", "yyyy-MM-dd.zip");

        // one request should do about 400-500B
        // We have configured max 5KB for one log file, and max two archive log files
        // 50 requests should be enough to fill this all
        // sending more requests to check that log archives are actually discarded when they go over limit
        sendRequests(80);

        List<String> logFileNames = getLogFileNames();
        // there should be 3 files - one active log and two archives/backups
        assertEquals(3, logFileNames.size(), "There should be 3 log file - one active and two backups");

        // check main log file size, we have max 5KB configured
        // adding extra 420 because the max file is not actual maximum of the file size :-)
        // it is just a limit after which Quarkus rotates (when logging finishes)
        // see https://github.com/quarkusio/quarkus/issues/44346
        assertTrue(Files.size(accessLogPath()) <= 5420, "Main log size should be max 5KB");

        // check that archive logs are valid zip files
        for (String filename : logFileNames) {
            // we don't check the main log file, that one is not archived nor compressed
            if (filename.equals(ACCESS_LOG_FILENAME)) {
                continue;
            }

            assertTrue(filename.endsWith(".zip"), "Archived logs should have suffix \".zip\"");
            assertTrue(isValidZipFile(ACCESS_LOG_DIR.resolve(filename)),
                    "Log file " + filename + " is not a valid zip");
        }
    }

    @Test
    public void gzipCompressionTest() throws IOException {
        // use gzip compression for archived logs
        setPropertyTo("quarkus.log.handler.file.access-log.rotation.file-suffix", "yyyy-MM-dd.gz");

        sendRequests(50);

        List<String> logFileNames = getLogFileNames();
        // there should be 3 files - one active log and two archives/backups
        assertEquals(3, logFileNames.size(), "There should be 3 log file - one active and two backups");

        // check that logs are valid .gz files
        for (String filename : logFileNames) {
            // we don't check the main log file, that one is not archived nor compressed
            if (filename.equals(ACCESS_LOG_FILENAME)) {
                continue;
            }

            assertTrue(filename.endsWith(".gz"), "Archived logs should have suffix \".gz\"");
            assertTrue(isValidGzipFile(ACCESS_LOG_DIR.resolve(filename)),
                    "Log file " + filename + " is not a valid zip");
        }
    }

    private void setPropertyTo(String property, String value) {
        app.stop();
        app.withProperty(property, value);
        app.start();
    }

    private boolean isValidGzipFile(Path file) throws IOException {
        try (InputStream is = new FileInputStream(file.toFile())) {
            byte[] signature = new byte[2];
            int nread = is.read(signature);
            // check that file headers matches the spec - https://docs.fileformat.com/compression/gz/#gz-file-header
            return nread == 2 && signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b;
        }
    }

    private boolean isValidZipFile(Path file) throws IOException {
        try {
            ZipFile zipFile = new ZipFile(file.toFile());
            zipFile.close();
        } catch (ZipException ex) {
            // if it is not a valid zip file, ZipException will be thrown
            return false;
        }
        return true;
    }

    private List<String> getLogFileNames() throws IOException {
        return Files.list(ACCESS_LOG_DIR)
                .map(file -> file.getFileName().toString())
                .filter(filename -> filename.startsWith(ACCESS_LOG_FILENAME))
                .toList();
    }

    private Path accessLogPath() {
        return ACCESS_LOG_DIR.resolve(ACCESS_LOG_FILENAME);
    }

    /**
     * Send simple request to the app.
     * One request should make about 400-500 bytes in the access log file.
     */
    private void sendRequests(int number) {
        for (int i = 0; i < number; i++) {
            app.given().get("/access").then().statusCode(200);
        }
    }
}
