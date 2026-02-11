package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.utils.OidcItUtils.createToken;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class SecurityClaimsIT {

    private static final String SECURED_PATH = "/secured";
    private static final String CLAIMS_FROM_BEANS_PATH = "/getClaimsFromBeans";
    private static final String CLAIMS_FROM_TOKEN_PATH = "/getClaimsFromToken";

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void verifySecuredEndpointIsProtected() {
        given().get(SECURED_PATH + CLAIMS_FROM_BEANS_PATH)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void verifyClaimsAreTheSame() {
        String claimsFromBeans = getClaimsFromBeans();
        String claimsFromToken = getClaimsFromToken();

        assertEquals(claimsFromBeans, claimsFromToken, "Claims should be equal");
    }

    private String getClaimsFromBeans() {
        return getClaimsInstancesFromPath(CLAIMS_FROM_BEANS_PATH);
    }

    private String getClaimsFromToken() {
        return getClaimsInstancesFromPath(CLAIMS_FROM_TOKEN_PATH);
    }

    private String getClaimsInstancesFromPath(String path) {
        return given()
                .auth().preemptive().oauth2(createToken(keycloak))
                .get(SECURED_PATH + path)
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }

}
