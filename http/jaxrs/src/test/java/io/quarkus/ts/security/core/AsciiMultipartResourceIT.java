package io.quarkus.ts.security.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.MultiPartSpecification;

@QuarkusScenario
public class AsciiMultipartResourceIT {

    public static final String TEXT_WITH_DIACRITICS = "Přikrášlený žloťoučký kůň úpěl ďábelské ódy.";
    private static final String EXPECTED_ASCII_TEXT = new String(TEXT_WITH_DIACRITICS.getBytes(StandardCharsets.UTF_8),
            StandardCharsets.US_ASCII);

    @QuarkusApplication
    static RestService app = new RestService().withProperties("us-asscii.properties");

    @Test
    public void testMultipartText() {
        MultiPartSpecification multiPartSpecification = new MultiPartSpecBuilder(TEXT_WITH_DIACRITICS)
                .controlName("text")
                .header("Content-Type", "text/plain")
                .charset(StandardCharsets.UTF_8)
                .build();

        app.given().multiPart(multiPartSpecification)
                .post("/multipart/text")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(not(equalTo(TEXT_WITH_DIACRITICS)))
                .body(equalTo(EXPECTED_ASCII_TEXT));
    }
}
