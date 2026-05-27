package io.quarkus.ts.lifecycle;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@Tag("QUARKUS-6793")
@QuarkusTest
@TestProfile(CdiContainerProfileTest.CdiDiagnosticProfile.class)
public class CdiContainerProfileTest {

    public static class CdiDiagnosticProfile implements QuarkusTestProfile {
    }

    @Test
    public void verifyCdiContainerIsActiveWithProfile() {
        given()
                .when().get("/cdi/container-status")
                .then()
                .statusCode(200)
                .body(is("active"));
    }
}
