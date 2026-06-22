package io.quarkus.ts.reactive.rest.data.panache.secured;

import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@Tag("QUARKUS-7179")
@QuarkusScenario
public class PermissionsAllowedPanacheEntityResourceIT extends AbstractPermissionsAllowedPanacheResourceIT {
    private static final String BASE_URL = "/secured/entity";

    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }

    @Test
    void customMethodPermissionsAllowedNoAuth() {
        given()
                .get(getBaseUrl() + "/permissions-allowed-method/custom-count")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void customMethodPermissionsAllowedForbidden() {
        given()
                .auth().basic("user", "user")
                .get(getBaseUrl() + "/permissions-allowed-method/custom-count")
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void customMethodPermissionsAllowedPermitted() {
        given()
                .auth().basic("admin", "admin")
                .get(getBaseUrl() + "/permissions-allowed-method/custom-count")
                .then().statusCode(HttpStatus.SC_OK);
    }
}
