package io.quarkus.ts.jaxrs.reactive;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.MultiPartSpecification;

@Tag("QUARKUS-1075")
@QuarkusScenario
public class MultipartResourceIT {

    private static final String IMAGE_FILE_NAME = "/quarkus.png";
    private static final String TEXT_WITH_DIACRITICS = "Přikrášlený žloťoučký kůň úpěl ďábelské ódy.";
    private static byte[] randomBytes = new byte[120];
    private static File imageFile;
    private static byte[] imageBytes;

    @BeforeAll
    public static void beforeAll() throws IOException {
        imageFile = new File(MultipartResourceIT.class.getResource(IMAGE_FILE_NAME).getFile());
        imageBytes = IOUtils.toByteArray(MultipartResourceIT.class.getResourceAsStream(IMAGE_FILE_NAME));
        new Random().nextBytes(randomBytes);
    }

    @Test
    public void testMultipartIsSendAndReceived() {
        whenSendMultipartData("/multipart")
                .contentType("multipart/form-data");
    }

    @Test
    public void testTextVersionOfMultipart() {
        whenSendMultipartData("/multipart/echo")
                .contentType(ContentType.TEXT)
                .body(
                        containsString("Content-Disposition: form-data; name=\"text\""),
                        containsString("Content-Disposition: form-data; name=\"data\"; filename=\"random.dat\""),
                        containsString("Content-Disposition: form-data; name=\"image\"; filename=\"quarkus.png\""),
                        containsString(TEXT_WITH_DIACRITICS));
    }

    @Test
    public void testTextPartFromMultipart() {
        whenSendMultipartData("/multipart/text")
                .contentType(ContentType.TEXT)
                .body(equalTo(TEXT_WITH_DIACRITICS));
    }

    @Test
    public void testImagePartFromMultipart() {
        byte[] receivedBytes = whenSendMultipartData("/multipart/image")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .extract().asByteArray();
        assertThat(receivedBytes, equalTo(imageBytes));
    }

    @Test
    public void testDataPartFromMultipart() {
        byte[] receivedBytes = whenSendMultipartData("/multipart/data")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .extract().asByteArray();
        assertThat(receivedBytes, equalTo(randomBytes));
    }

    private ValidatableResponse whenSendMultipartData(String path) {
        MultiPartSpecification textSpec = new MultiPartSpecBuilder(TEXT_WITH_DIACRITICS)
                .controlName("text")
                .mimeType("text/plain")
                .charset(StandardCharsets.UTF_8)
                .build();
        MultiPartSpecification dataSpec = new MultiPartSpecBuilder(randomBytes)
                .controlName("data")
                .fileName("random.dat")
                .header("Content-Type", "application/octet-stream")
                .build();
        MultiPartSpecification imageSpec = new MultiPartSpecBuilder(imageFile)
                .controlName("image")
                .fileName("quarkus.png")
                .mimeType("image/png")
                .build();

        return RestAssured.given()
                .contentType("multipart/form-data")
                .multiPart(textSpec)
                .multiPart(imageSpec)
                .multiPart(dataSpec)
                .post(path)
                .then()
                .statusCode(200);
    }
}
