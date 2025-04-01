package io.quarkus.ts.security.keycloak.oidcclient.standalone;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-5611")
@QuarkusScenario
public class DevModeOidcClientTokensIT {

    public static String DEFAULT_USER = "bob";
    public static String CUSTOM_USER = "alice";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperties("oidc-client.properties");

    @Test
    public void keycloakContainerShouldStart() {
        app.logs().assertContains("Creating container for image: quay.io/keycloak/keycloak");
        app.logs().assertContains("Dev Services for Keycloak started");
    }

    @Test
    public void checkTokenUpnIsDefaultUserUsingInjectToken() {
        given()
                .when()
                .get("/oidc-client-tokens")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("\"upn\":\"" + DEFAULT_USER + "\""));
    }

    @Test
    public void checkTokenUpnIsDefaultUserUsingInjectOidcClient() {
        given()
                .when()
                .get("/oidc-client-tokens/default-user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("\"upn\":\"" + DEFAULT_USER + "\""));
    }

    @Test
    public void checkTokenUpnIsCustomUser() {
        given()
                .when()
                .get("/oidc-client-tokens/custom-user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("\"upn\":\"" + CUSTOM_USER + "\""));
    }
}
