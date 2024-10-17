package io.quarkus.ts.security.bouncycastle.fips;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

// see https://github.com/quarkusio/quarkus/issues/43935 for context why this test is disabled
@DisabledOnFipsAndNative(reason = "not supported in FIPS-enabled environment in native")
@QuarkusScenario
public class BouncyCastleFipsIT {

    @Test
    public void verifyBouncyCastleFipsProviderAvailability() {
        given()
                .when()
                .get("/api/listProviders")
                .then()
                .statusCode(200)
                .body(containsString("BCFIPS"));
    }

    @Test
    public void verifyBouncyCastleSHA256withRSAandMGF1Availability() {
        given()
                .when()
                .get("/api/SHA256withRSAandMGF1")
                .then()
                .statusCode(200)
                .body(equalTo("success"));
    }
}
