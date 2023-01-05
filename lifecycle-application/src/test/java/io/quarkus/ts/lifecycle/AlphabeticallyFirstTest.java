package io.quarkus.ts.lifecycle;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * The name of the test is crucial to make it run before HelloMainTest.
 * See comments to the issue for the explanation.
 */
@QuarkusTest
@Tag("QUARKUS-2789")
public class AlphabeticallyFirstTest {

    @Test
    public void shouldBeOk() {
        given()
                .when().get("/args")
                .then()
                .statusCode(200)
                .body(is(""));
    }
}
