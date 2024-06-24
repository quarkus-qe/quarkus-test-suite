package io.quarkus.ts.http.restclient.reactive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.restclient.reactive.multipart.Item;
import io.quarkus.ts.http.restclient.reactive.multipart.MyMultipartDTO;
import io.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.junit5.VertxExtension;

/**
 * Integration test for multipart encoder mode handling in the REST client.
 *
 * This test verifies:
 * - Multipart requests are correctly encoded using HTML5, RFC1738, and RFC3986 modes.
 * - A Vert.x server successfully parses and interprets the encoded data.
 * - File contents and additional form fields are transmitted without corruption.
 *
 * Note: Although the exact encoder mode used in transmission cannot be directly verified,
 * test ensure content is correctly parsed and interpreted by the server, providing confidence that the appropriate encoder mode
 * was applied.
 */

@ExtendWith(VertxExtension.class)
@QuarkusScenario
public class HttpMultipartEncodeModeAndContentIT {

    private static final int VERTX_SERVER_PORT = 8081;
    private static HttpServer httpServer;
    private static final File FILE1;
    private static final File FILE2;
    static {
        FILE1 = Paths.get("src", "test", "resources", "sample.txt").toFile();
        FILE2 = Paths.get("src", "test", "resources", "sampl'eñ.txt").toFile();
    }

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("vertx-server-port", String.valueOf(VERTX_SERVER_PORT));

    @BeforeAll
    public static void setUpVertxServer(Vertx vertx) {
        final var mapper = new ObjectMapper();
        vertx.createHttpServer(new HttpServerOptions().setPort(VERTX_SERVER_PORT))
                .requestHandler(httpServerRequest -> {
                    if (!httpServerRequest.path().contains("/encoder-mode")) {
                        httpServerRequest.response().setStatusCode(500).end();
                        return;
                    }

                    httpServerRequest.setExpectMultipart(true);
                    AtomicInteger fileCount = new AtomicInteger(0);
                    List<Item> items = new ArrayList<>();

                    httpServerRequest.uploadHandler(upload -> {
                        upload.handler(buffer -> {
                            Map<String, List<String>> headers = new HashMap<>();

                            headers.put("Content-Type", List.of(upload.contentType()));
                            if (upload.contentTransferEncoding() != null) {
                                headers.put("Content-Transfer-Encoding", List.of(upload.contentTransferEncoding()));
                            }
                            headers.put("Content-Disposition", List.of(
                                    "form-data; name=\"" + upload.name() + "\"; filename=\"" + upload.filename() + "\""));

                            items.add(new Item(
                                    upload.name(),
                                    buffer.length(),
                                    upload.charset(),
                                    upload.filename(),
                                    true,
                                    headers,
                                    true,
                                    buffer.toString()));
                        });

                        upload.endHandler(v -> {
                            if (fileCount.incrementAndGet() == 2) {
                                try {
                                    httpServerRequest.response()
                                            .setStatusCode(200)
                                            .putHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                            .end(mapper.writeValueAsString(new MyMultipartDTO(items)));
                                } catch (JsonProcessingException e) {
                                    httpServerRequest.response().setStatusCode(500).end(e.getMessage());
                                }
                            }
                        });
                    });

                })

                .listen()
                .onSuccess(server -> HttpMultipartEncodeModeAndContentIT.httpServer = server);
    }

    @ParameterizedTest
    @EnumSource(EncoderMode.class)
    public void testMultipartEncodeMode(EncoderMode encoderMode) throws IOException {

        Response response = app.given()
                .multiPart("file1", FILE1, "text/plain")
                .multiPart("file2", FILE2, "text/plain")
                .multiPart("otherField", "other field")
                .header("Content-Type", MediaType.MULTIPART_FORM_DATA)
                .pathParam("encoder-mode", encoderMode)
                .when()
                .post("/encode/{encoder-mode}")
                .then()
                .statusCode(200)
                .extract().response();

        Assertions.assertNotNull(response.getBody(), "Response body should not be null");

        MyMultipartDTO responseDto = response.as(MyMultipartDTO.class);
        Assertions.assertNotNull(responseDto, "Response DTO should not be null");

        for (Item item : responseDto.getItems()) {
            if (item.isFileItem()) {
                Map<String, List<String>> headers = item.getHeaders();

                // Content-Disposition
                List<String> contentDispositionList = headers.get("Content-Disposition");
                Assertions.assertNotNull(contentDispositionList,
                        "Content-Disposition header should not be null for file item: " + item.getName());
                Assertions.assertFalse(contentDispositionList.isEmpty(),
                        "Content-Disposition header should not be empty for file item: " + item.getName());
                String contentDisposition = contentDispositionList.get(0);
                Assertions.assertTrue(contentDisposition.contains("name=\"" + item.getName() + "\""));
                Assertions.assertTrue(contentDisposition.contains("filename=\"" + item.getFileName() + "\""));

                // Content-Transfer-Encoding
                List<String> contentTransferEncodingList = headers.get("Content-Transfer-Encoding");
                Assertions.assertNotNull(contentTransferEncodingList,
                        "Content-Transfer-Encoding header should not be null for file item: " + item.getName());
                Assertions.assertFalse(contentTransferEncodingList.isEmpty(),
                        "Content-Transfer-Encoding header should not be empty for file item: " + item.getName());
                String contentTransferEncoding = contentTransferEncodingList.get(0);
                Assertions.assertEquals("binary", contentTransferEncoding);

                // Content-Type
                List<String> contentTypeList = headers.get("Content-Type");
                Assertions.assertNotNull(contentTypeList,
                        "Content-Type header should not be null for file item: " + item.getName());
                Assertions.assertFalse(contentTypeList.isEmpty(),
                        "Content-Type header should not be empty for file item: " + item.getName());
                String contentType = contentTypeList.get(0);
                Assertions.assertEquals("application/octet-stream", contentType);

                // File content verification
                File expectedFile = null;
                if (item.getName().equals("file1")) {
                    expectedFile = FILE1;
                } else if (item.getName().equals("file2")) {
                    expectedFile = FILE2;
                } else {
                    Assertions.fail("Unexpected file item name: " + item.getName());
                }
                String expectedFileContent = Files.readString(expectedFile.toPath());
                Assertions.assertEquals(expectedFileContent, item.getFileContent(),
                        "File content mismatch for " + item.getName());

            }

        }
        Assertions.assertEquals(2, responseDto.getItems().size(), "Expected exactly 2 file items in response");
    }

    @Test
    public void testIncorrectMultipartEncoderMode() {
        String incorrectEncodeMode = "XXHTML3";
        app.given()
                .multiPart("file1", FILE1, "text/plain")
                .multiPart("file2", FILE2, "text/plain")
                .multiPart("otherField", "other field")
                .header("Content-Type", MediaType.MULTIPART_FORM_DATA)
                .pathParam("encoder-mode", incorrectEncodeMode) // Client mode in the path
                .when()
                .post("/encode/{encoder-mode}")
                .then()
                .statusCode(400);

    }

    @AfterAll
    public static void stopServer() {
        if (httpServer != null) {
            httpServer.close();
        }
    }

    private enum EncoderMode {
        HTML5,
        RFC1738,
        RFC3986
    }

}
