package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;
import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

/**
 * If keystore/truststore file type does not match declared one, communication between OIDC server
 * and client should fail.
 */
@Tag("fips-incompatible")
@QuarkusScenario
public class IncorrectKsFileTypeOidcMtlsIT extends BaseOidcMtlsIT {

    @Container(image = "${keycloak.image}", expectedLog = EXPECTED_LOG, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = newKeycloakInstance();

    @QuarkusApplication
    static RestService app = createRestService(JKS_KEY_STORE_FILE_EXTENSION, "incorrect-type",
            keycloak::getRealmUrl);

    @Test
    void failAuthenticate() {
        given()
                .when()
                .auth().oauth2(getToken(NORMAL_USER))
                .get(RESOURCE_PATH)
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected String getKeyStoreFileExtension() {
        return JKS_KEY_STORE_FILE_EXTENSION;
    }

    @Override
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }
}
