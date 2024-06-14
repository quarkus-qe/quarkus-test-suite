package io.quarkus.ts.http.advanced.reactive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.FileUtils;
import io.restassured.response.Response;

@DisabledOnNative(reason = "Because of https://github.com/quarkusio/quarkus/issues/40533")
@Tag("QQE-378")
@QuarkusScenario
public class Brotli4JHttpIT {
    @QuarkusApplication(classes = { Brotli4JHttpServerConfig.class, Brotli4JResource.class,
            Brotli4JRestMock.class }, properties = "compression.properties")
    static RestService app = new RestService();

    private final static String DEFAULT_TEXT_PLAIN = Brotli4JResource.DEFAULT_TEXT_PLAIN;

    final static int CONTENT_LENGTH_DEFAULT_TEXT_PLAIN = DEFAULT_TEXT_PLAIN.length();

    private final static String JSON_CONTENT = "Hello from a JSON sample";

    private final static String BROTLI_ENCODING = "br";

    @Test
    public void checkTextPlainDefaultWithoutBrotli4JEncoding() {
        // As we are using quarkus.http.enable-compression=true then gzip compression is used by default
        Response response = app.given()
                .get("/compression/default/text")
                .then()
                .statusCode(200)
                .and()
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .body(is(DEFAULT_TEXT_PLAIN)).extract().response();
        int contentLength = Integer.parseInt(response.getHeader(HttpHeaders.CONTENT_LENGTH));
        assertTrue(CONTENT_LENGTH_DEFAULT_TEXT_PLAIN > contentLength);
    }

    @Test
    public void checkTextPlainWithtBrotli4J() {
        int textPlainDataLenght = calculateTextLength("/sample.txt");
        assertBrotli4JCompression("/compression/text", MediaType.TEXT_PLAIN, BROTLI_ENCODING, BROTLI_ENCODING,
                textPlainDataLenght);
    }

    @Test
    public void checkBigTextPlainWithtBrotli4J() {
        int textPlainDataLenght = calculateTextLength("/big_sample.txt");
        assertBrotli4JCompression("/compression/text/big", MediaType.TEXT_PLAIN, BROTLI_ENCODING, BROTLI_ENCODING,
                textPlainDataLenght);
    }

    @Test
    public void checkXmlBrotli4JCompression() {
        int originalXMLLength = calculateXmlLength();
        assertBrotli4JCompression("/compression/brotli/xml", MediaType.APPLICATION_XML, BROTLI_ENCODING, BROTLI_ENCODING,
                originalXMLLength);
    }

    @Test
    public void checkJsonBrotli4JCompression() throws IOException {
        int originalJsonLength = calculateOriginalJsonLength();
        assertBrotli4JCompression("/compression/brotli/json", MediaType.APPLICATION_JSON, BROTLI_ENCODING, BROTLI_ENCODING,
                originalJsonLength);
    }

    @Test
    public void checkCompressedAndDecompressedWithQuarkus() {
        testCompressedAndDecompressed("/compression/default/text", DEFAULT_TEXT_PLAIN);
    }

    @Test
    public void checkCompressedAndDecompressedJSONWithQuarkus() {
        testCompressedAndDecompressed("/compression/brotli/json", JSON_CONTENT);
    }

    @Test
    public void checkCompressedAndDecompressedXMLWithQuarkus() {
        testCompressedAndDecompressed("/compression/brotli/xml", "Bob Dylan");
    }

    public void assertBrotli4JCompression(String path, String contentHeaderType, String acceptHeaderEncoding,
            String expectedHeaderContentEncoding, int originalContentLength) {
        Response response = app.given()
                .when()
                .contentType(contentHeaderType)
                .header(HttpHeaders.ACCEPT_ENCODING, acceptHeaderEncoding)
                .get(path)
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_ENCODING, expectedHeaderContentEncoding)
                .extract().response();
        byte[] responseBody = response.getBody().asByteArray();
        int compressedContentLength = responseBody.length;
        assertTrue(compressedContentLength < originalContentLength);
    }

    private void testCompressedAndDecompressed(String compressionPath, String contentExpected) {
        Response response = app.given()
                .header(HttpHeaders.ACCEPT_ENCODING, BROTLI_ENCODING)
                .get(compressionPath)
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_ENCODING, BROTLI_ENCODING)
                .extract().response();
        byte[] compressedBytes = response.asByteArray();

        Response decompressionResponse = app.given()
                .header(HttpHeaders.CONTENT_ENCODING, BROTLI_ENCODING)
                .body(compressedBytes)
                .post("/compression/decompression")
                .then()
                .statusCode(200)
                .extract().response();
        assertThat(decompressionResponse.asString(), containsString(contentExpected));
    }

    private int calculateOriginalJsonLength() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/sample.json")) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(inputStream);
            String jsonString = objectMapper.writeValueAsString(jsonNode);
            return jsonString.getBytes().length;
        }
    }

    private int calculateXmlLength() {
        return FileUtils.loadFile("/sample.xml").length();
    }

    private int calculateTextLength(String fileName) {
        return FileUtils.loadFile(fileName).length();
    }
}
