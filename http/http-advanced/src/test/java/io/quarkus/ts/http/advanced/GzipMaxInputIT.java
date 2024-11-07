package io.quarkus.ts.http.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import jakarta.ws.rs.core.HttpHeaders;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class GzipMaxInputIT {

    final static String INVALID_VALUE = "";
    final static long SMALL_PAYLOAD = 512;
    final static long LIMIT_PAYLOAD = 100 * 1024 * 1024;
    final static long OVER_LIMIT_PAYLOAD = 200 * 1024 * 1024;

    private final byte[] buffer = new byte[4096];

    /**
     *
     * Tests are checking server response on different size of sent payload
     * Limit is configured using quarkus.resteasy.gzip.max-input property
     * (According "All configurations options" guide the property 'quarkus.resteasy.gzip.max-input' refers to
     * Maximum deflated file bytes size)
     * If the limit is exceeded, Resteasy will return a response with status 413("Request Entity Too Large")
     */
    @QuarkusApplication(classes = { GzipResource.class }, properties = "gzip.properties")
    static RestService app = new RestService();

    private ByteArrayInputStream generateCompressedDataStream(long sizeInBytes) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                long bytesRemaining = sizeInBytes;

                while (bytesRemaining > 0) {
                    int bytesToWrite;

                    if (bytesRemaining >= buffer.length) {
                        bytesToWrite = buffer.length;
                    } else {
                        bytesToWrite = (int) bytesRemaining;
                    }

                    gzipOutputStream.write(buffer, 0, bytesToWrite);
                    bytesRemaining = bytesRemaining - bytesToWrite;
                }
            }

            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Error generating compressed data stream", e);
        }
    }

    @Test
    void sendInvalidContent() {
        Response response = sendStringDataToGzipEndpoint(INVALID_VALUE);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.statusCode(),
                "Invalid data as this void string should result in 400 BAD_REQUEST response");
    }

    @Test
    void sendZeroBytesPayload() throws IOException {
        ByteArrayInputStream compressedData = generateCompressedDataStream(0);
        Response response = sendDataToGzipEndpoint(compressedData);
        assertEquals(HttpStatus.SC_OK, response.statusCode(),
                "The response should be 200 OK because the compression returns 2 bytes");
    }

    @Test
    void sendPayloadBelowMaxInputLimit() throws IOException {
        ByteArrayInputStream compressedData = generateCompressedDataStream(SMALL_PAYLOAD);
        Response response = sendDataToGzipEndpoint(compressedData);
        assertEquals(HttpStatus.SC_OK, response.statusCode(),
                "The response should be 200 OK because sending just 512 bytes");
    }

    @Tag("https://github.com/quarkusio/quarkus/issues/39636")
    @Test
    void sendMaximumAllowedPayload() throws IOException {
        ByteArrayInputStream compressedData = generateCompressedDataStream(LIMIT_PAYLOAD);
        Response response = sendDataToGzipEndpoint(compressedData);
        assertEquals(HttpStatus.SC_OK, response.statusCode(),
                "The response should be 200 OK because sending just the limit payload configured using " +
                        "quarkus.resteasy.gzip.max-input=100M. This fails if the suffix format parsing is not " +
                        "working and RESTEasy falls back to its default which is 10M");
    }

    @Test
    void sendMoreThanMaximumAllowedPayload() throws IOException {
        ByteArrayInputStream compressedData = generateCompressedDataStream(OVER_LIMIT_PAYLOAD);
        Response response = sendDataToGzipEndpoint(compressedData);
        assertEquals(HttpStatus.SC_REQUEST_TOO_LONG, response.statusCode(),
                "The response should be 413 REQUEST_TOO_LONG when sending larger payload than the limit");
    }

    private Response sendDataToGzipEndpoint(ByteArrayInputStream data) {
        return app.given()
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .body(data)
                .when()
                .post("/gzip")
                .then()
                .extract().response();
    }

    private Response sendStringDataToGzipEndpoint(String data) {
        return app.given()
                .header("Content-Encoding", "gzip")
                .body(data)
                .when()
                .post("/gzip")
                .then()
                .extract().response();
    }

}
