package io.quarkus.ts.security.core;

import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.RestAssured;

@QuarkusScenario
public class BaseQuarkusBundleIT {

    @Test
    public void testQuarkusEndpoint() {
        RestAssured.given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("hello"));
    }

}
