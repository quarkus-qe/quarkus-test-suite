package io.quarkus.ts.jaxrs.reactive.client;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

// TODO: Failing because of https://github.com/quarkusio/quarkus/issues/19892, enable after the issue is resolved
@Tag("QUARKUS-1225")
@Disabled
@QuarkusScenario
public class MultipartClientResourceIT {

    @Test
    public void testMultipartDataIsSent() {
        given()
                .when().post("/client/multipart")
                .then()
                .statusCode(200)
                .body(containsString("Content-Disposition: form-data; name=\"data\""),
                        containsString("Content-Type: application/json"),
                        containsString("{\"foo\":\"test1\",\"bar\":1}"));
    }
}
