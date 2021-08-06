package io.quarkus.ts.security;

import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-1026")
@QuarkusScenario
@DisabledOnNativeImage
public class DevModeOidcSecurityIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService();

    @Test
    public void keycloakContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: quay.io/keycloak/keycloak");
    }

    @Test
    public void noUserUserResource() {
        given()
                .when()
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void noUserAdminResource() {
        given()
                .when()
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}
