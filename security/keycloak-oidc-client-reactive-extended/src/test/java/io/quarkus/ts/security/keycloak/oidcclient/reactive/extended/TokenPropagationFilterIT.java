package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.OidcItUtils.USER;
import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.OidcItUtils.createToken;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-3680")
@QuarkusScenario
public class TokenPropagationFilterIT {

    @KeycloakContainer(runKeycloakInProdMode = true, command = { "start", "--import-realm", "--hostname-strict=false",
            "--features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void usernameTest() {
        given()
                .auth().oauth2(createToken(keycloak))
                .when().get("/token-propagation-filter")
                .then().statusCode(HttpStatus.SC_OK)
                .body(containsString(USER));
    }
}
