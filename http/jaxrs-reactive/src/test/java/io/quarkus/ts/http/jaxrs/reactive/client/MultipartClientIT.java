package io.quarkus.ts.http.jaxrs.reactive.client;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsStringIgnoringCase;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;

@Tag("QUARKUS-1225")
@QuarkusScenario
public class MultipartClientIT {

    @Test
    //TODO: https://github.com/quarkusio/quarkus/issues/28782
    @DisabledOnQuarkusSnapshot(reason = "multiplart form data request body is always null")
    public void testMultipartDataIsSent() {
        given()
                .when().post("/client/multipart")
                .then()
                .statusCode(200)
                .body(containsStringIgnoringCase("Content-Disposition: form-data; name=\"pojoData\""),
                        containsStringIgnoringCase("Content-Type: application/json"),
                        containsStringIgnoringCase("{\"foo\":\"test1\",\"bar\":1}"));
    }
}
