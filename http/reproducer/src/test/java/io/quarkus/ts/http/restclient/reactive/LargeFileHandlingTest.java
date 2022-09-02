package io.quarkus.ts.http.restclient.reactive;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.ts.http.restclient.reactive.files.OsUtils;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class LargeFileHandlingTest {
    private static final long BIGGER_THAN_TWO_GIGABYTES = OsUtils.SIZE_2049MiB;
    private static final Path files = getTempDirectory();
    private final Path downloaded;
    private final Path uploaded;
    private final OsUtils utils;

    private static Path getTempDirectory() {
        try {
            return OsUtils.get().createTempDirectory();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public LargeFileHandlingTest() {
        downloaded = files.resolve("downloaded.txt");
        uploaded = files.resolve("uploaded.txt");
        utils = OsUtils.get();
    }

    @Test
    public void validateClientResponse() {
        Response original = given().get("/file/hash");
        Response wrapped = given().get("/file-client/hash");
        assertEquals(HttpStatus.SC_OK, original.statusCode());
        assertEquals(HttpStatus.SC_OK, wrapped.statusCode());
        assertNotNull(original.body().asString());
        assertEquals(original.body().asString(), wrapped.body().asString());
    }

    @Test
    public void downloadDirectly() throws IOException {
        Response hashSum = given().get("/file/hash");
        assertEquals(HttpStatus.SC_OK, hashSum.statusCode());
        String serverSum = hashSum.body().asString();

        Response download = given().get("/file/download");
        assertEquals(HttpStatus.SC_OK, download.statusCode());
        InputStream stream = download.body().asInputStream();
        Files.copy(stream, downloaded);
        String clientSum = utils.getSum(downloaded);
        assertEquals(serverSum, clientSum);
    }

    @Test
    public void downloadThroughClient() {
        Response hashSum = given().get("/file/hash");
        assertEquals(HttpStatus.SC_OK, hashSum.statusCode());
        String serverSum = hashSum.body().asString();

        Response download = given().get("/file-client/download");
        assertEquals(HttpStatus.SC_OK, download.statusCode());
        String clientSum = download.body().asString();

        assertEquals(serverSum, clientSum);
    }

    @Test
    public void uploadFileThroughClient() {
        Response hashSum = given().get("/file-client/client-hash");
        assertEquals(HttpStatus.SC_OK, hashSum.statusCode());
        String before = hashSum.body().asString();

        Response upload = given().post("/file-client/upload-file");
        assertEquals(HttpStatus.SC_OK, upload.statusCode());
        String after = upload.body().asString();

        assertEquals(before, after);
    }

    @Test
    public void uploadMultipart() {
        Response hashSum = given().get("/file-client/client-hash");
        assertEquals(HttpStatus.SC_OK, hashSum.statusCode());
        String before = hashSum.body().asString();

        Response upload = given().post("/file-client/multipart");
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
        given().delete("/file-client/");
        given().delete("/file/");
        Files.delete(files);
    }
}
