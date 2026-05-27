package io.quarkus.ts.lifecycle;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@Tag("QUARKUS-6793")
@QuarkusTest
public class CdiContainerTest {

    @Test
    public void verifyCdiContainerIsActive() {
        given()
                .when().get("/cdi/container-status")
                .then()
                .statusCode(200)
                .body(is("active"));
    }
}
