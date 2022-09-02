package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.restclient.reactive.files.OsUtils;
import io.restassured.response.Response;

@QuarkusScenario
public class LargeFileHandlingIT {
    private static final Path files = getTempDirectory();

    private static Path getTempDirectory() {
        try {
            return OsUtils.get().createTempDirectory();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
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
