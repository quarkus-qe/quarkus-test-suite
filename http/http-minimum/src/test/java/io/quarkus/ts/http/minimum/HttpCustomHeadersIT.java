package io.quarkus.ts.http.minimum;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@Tag("QUARKUS-1574")
@QuarkusScenario
public class HttpCustomHeadersIT {

    @Test
    public void caseInsensitiveAcceptHeader() {
        given()
                .accept("Application/json")
                .get("/api/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", is("Hello, World!"));
    }
}
