package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.MultipartResource.MULTIPART_FORM_PATH;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MultipartInputstreamIT {

    @QuarkusApplication(classes = { MultipartResource.class, MediaTypeResource.class, MediaTypeWrapper.class,
            MultipartFormDataDTO.class }, properties = "oidcdisable.properties")
    static RestService app = new RestService();

    @Tag("https://github.com/quarkusio/quarkus/issues/43364")
    @Test
    public void testInputStreamWithoutMediaType() {
        String testContent = "test content without media type";
        InputStream fileStream = new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8));
        String description = "Test Description";

        app.given()
                .multiPart("file", "test.txt", fileStream)
                .multiPart("description", description)
                .when()
                .post(MULTIPART_FORM_PATH + "/inputstream/without-media-type")
                .then()
                .statusCode(200)
                .body(equalTo("Received description: " + description + " with file content: " + testContent));
    }

    @Tag("https://github.com/quarkusio/quarkus/issues/43364")
    @Test
    public void testInputStreamWithMediaType() {
        String testContent = "test content with media type";
        InputStream fileStream = new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8));
        String description = "Test Description";

        app.given()
                .multiPart("file", "test.txt", fileStream, MediaType.TEXT_PLAIN)
                .multiPart("description", description)
                .when()
                .post(MULTIPART_FORM_PATH + "/inputstream/with-media-type")
                .then()
                .statusCode(200)
                .body(equalTo("Received description: " + description + " with file content: " + testContent));
    }

}
