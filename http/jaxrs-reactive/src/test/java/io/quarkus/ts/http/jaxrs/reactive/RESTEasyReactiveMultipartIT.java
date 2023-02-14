package io.quarkus.ts.http.jaxrs.reactive;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import jakarta.ws.rs.core.MediaType;

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
public class RESTEasyReactiveMultipartIT {

    private static final String IMAGE_FILE_NAME = "/quarkus.png";
    private static final String TEXT_WITH_DIACRITICS = "Přikrášlený žloťoučký kůň úpěl ďábelské ódy.";
    private static byte[] randomBytes = new byte[120];
    private static final String DATA = "data";
    private static final String IMAGE = "image";
    private static File imageFile;
    private static byte[] imageBytes;

    @BeforeAll
    public static void beforeAll() throws IOException {
        imageFile = new File(RESTEasyReactiveMultipartIT.class.getResource(IMAGE_FILE_NAME).getFile());
        imageBytes = IOUtils.toByteArray(RESTEasyReactiveMultipartIT.class.getResourceAsStream(IMAGE_FILE_NAME));
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

    @Tag("QUARKUS-2744")
    @Test
    public void testPlainTextFilePartFromMultipart() {
        // verifies that every multipart form data field regardless of media type can be used as file
        // as long as Java data type of DTO field is java.io.File
        String text = "Old Time Rock & Roll";
        MultiPartSpecification textSpec = new MultiPartSpecBuilder(text)
                .controlName("plainTextFile")
                .fileName("plainTextFile")
                .header("Content-Type", MediaType.TEXT_PLAIN)
                .build();
        String receivedString = RestAssured.given()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart(textSpec)
                .post("/multipart/plain-text-file")
                .then()
                .statusCode(200)
                .extract()
                .asString();
        assertThat(receivedString, equalTo(text));
    }

    @Tag("QUARKUS-2744")
    @Test
    public void testAllFilesPartFromMultipart() {
        // test all file uploads from a multipart form are accessible
        String otherImage = "otherImage";
        String xmlFileName = "xmlFile";
        String customContentTypeFileName = "customContentTypeFile";
        String[] controlNames = RestAssured.given()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart(createXmlSpec(xmlFileName))
                .multiPart(createCustomContentTypeSpec(customContentTypeFileName))
                .multiPart(createDataSpec())
                .multiPart(createImageSpec())
                .multiPart(createOtherImage(otherImage))
                .post("/multipart/all-file-control-names")
                .then()
                .statusCode(200)
                .extract()
                .as(String[].class);
        // verify files with content types specified via 'quarkus.http.body.multipart.file-content-types' property
        assertThat(controlNames.length, equalTo(5));
        assertThat(controlNames[0], equalTo(xmlFileName));
        assertThat(controlNames[1], equalTo(customContentTypeFileName));
        // verify files that are also MultipartBody fields
        assertThat(controlNames[2], equalTo(DATA));
        assertThat(controlNames[3], equalTo(IMAGE));
        // verify file that is neither MultipartBody field nor file content type specified via config property
        // file is present as image/png is known file type
        assertThat(controlNames[4], equalTo(otherImage));
    }

    private static MultiPartSpecification createOtherImage(String otherImage) {
        return new MultiPartSpecBuilder(imageFile)
                .controlName(otherImage)
                .fileName("other.png")
                .mimeType("image/png")
                .build();
    }

    private static ValidatableResponse whenSendMultipartData(String path) {
        MultiPartSpecification textSpec = new MultiPartSpecBuilder(TEXT_WITH_DIACRITICS)
                .controlName("text")
                .mimeType("text/plain")
                .charset(StandardCharsets.UTF_8)
                .build();
        MultiPartSpecification dataSpec = createDataSpec();
        MultiPartSpecification imageSpec = createImageSpec();

        return RestAssured.given()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart(textSpec)
                .multiPart(imageSpec)
                .multiPart(dataSpec)
                .post(path)
                .then()
                .statusCode(200);
    }

    private static MultiPartSpecification createImageSpec() {
        return new MultiPartSpecBuilder(imageFile)
                .controlName(IMAGE)
                .fileName("quarkus.png")
                .mimeType("image/png")
                .build();
    }

    private static MultiPartSpecification createDataSpec() {
        return new MultiPartSpecBuilder(randomBytes)
                .controlName(DATA)
                .fileName("random.dat")
                .header("Content-Type", "application/octet-stream")
                .build();
    }

    private static MultiPartSpecification createXmlSpec(String xmlFileName) {
        return new MultiPartSpecBuilder("<song>Born To Be Wild</song>")
                .controlName(xmlFileName)
                .fileName("my.xml")
                .header("Content-Type", MediaType.TEXT_XML)
                .build();
    }

    private static MultiPartSpecification createCustomContentTypeSpec(String customContentTypeFileName) {
        return new MultiPartSpecBuilder("The Rocky Road to Dublin")
                .controlName(customContentTypeFileName)
                .fileName("your.custom")
                .header("Content-Type", "custom/content-type")
                .build();
    }

}
