package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.restclient.reactive.files.OsUtils;
import io.restassured.response.Response;

@QuarkusScenario
public class LargeFileHandlingIT {

    private static final long BIGGER_THAN_TWO_GIGABYTES = OsUtils.SIZE_2049MiB;
    private static final Path files = getTempDirectory();
    private final Path downloaded;
    private final Path uploaded;
    private final OsUtils utils;

    private static Path getTempDirectory() {
        try {
            return Files.createTempDirectory("large_files");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("client.filepath", () -> files.toAbsolutePath().toString())
            .withProperties("modern.properties");

    @Inject
    public LargeFileHandlingIT() {
        downloaded = files.resolve("downloaded.txt");
        uploaded = files.resolve("uploaded.txt");
        utils = OsUtils.get();
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "https://github.com/quarkusio/quarkus/issues/24763")
    public void uploadMultipart() {
        Response hashSum = app.given().get("/file-client/client-hash");
        assertEquals(HttpStatus.SC_OK, hashSum.statusCode());
        String before = hashSum.body().asString();

        Response upload = app.given().post("/file-client/multipart");
        assertEquals(HttpStatus.SC_OK, upload.statusCode());
        String after = upload.body().asString();

        assertEquals(before, after);
    }

    @AfterAll
    static void afterAll() throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(files)) {
            for (Path path : paths) {
                Files.delete(path);
            }
        }
        app.given().delete("/file-client/");
        app.given().delete("/file/");
        Files.delete(files);
    }
}
