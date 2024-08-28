package io.quarkus.ts.http.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class GzipMaxInputIT {
    final byte[] zero_bytes = new byte[0];
    final String invalid_value = "";
    final byte[] SMALL_PAYLOAD = new byte[512];
    final byte[] LIMIT_PAYLOAD = new byte[100 * 1024 * 1024];
    final byte[] OVER_LIMIT_PAYLOAD = new byte[200 * 1024 * 1024];

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

    @Test
    void sendInvalidContent() {
        Response response = sendStringDataToGzipEndpoint(invalid_value);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.statusCode(),
                "Invalid data as this void string should result in 400 BAD_REQUEST response");
    }

    @Test
    void sendZeroBytesPayload() throws IOException {
        byte[] compressedData = generateCompressedData(zero_bytes);
        Response response = sendDataToGzipEndpoint(compressedData);
        assertEquals(HttpStatus.SC_OK, response.statusCode(),
                "The response should be 200 OK because the compression returns 2 bytes");
    }

    @Test
    void sendPayloadBelowMaxInputLimit() throws IOException {
        byte[] compressedData = generateCompressedData(SMALL_PAYLOAD);
        Response response = sendDataToGzipEndpoint(compressedData);
        assertEquals(HttpStatus.SC_OK, response.statusCode(),
                "The response should be 200 OK because sending just 512 bytes");
    }

    @Test
    void sendMaximumAllowedPayload() throws IOException {
        byte[] compressedData = generateCompressedData(LIMIT_PAYLOAD);
        Response response = sendDataToGzipEndpoint(compressedData);
        assertEquals(HttpStatus.SC_OK, response.statusCode(),
                "The response should be 200 OK because sending just the limit payload configured using " +
                        "quarkus.resteasy.gzip.max-input=100M. This fails if the suffix format parsing is not " +
                        "working and RESTEasy falls back to its default which is 10M");
    }

    @Test
    void sendMoreThanMaximumAllowedPayload() throws IOException {
        byte[] compressedData = generateCompressedData(OVER_LIMIT_PAYLOAD);
        Response response = sendDataToGzipEndpoint(compressedData);
        assertEquals(HttpStatus.SC_REQUEST_TOO_LONG, response.statusCode(),
                "The response should be 413 REQUEST_TOO_LONG when sending larger payload than the limit");
    }

    private Response sendDataToGzipEndpoint(byte[] data) {
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

    public byte[] generateCompressedData(byte[] data) throws IOException {
        byte[] result;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
            gzipOut.close();
            result = baos.toByteArray();
        }
        return result;
    }
}
