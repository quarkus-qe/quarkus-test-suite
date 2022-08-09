package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.KC_DEV_MODE_JKS_CMD;
import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;
import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

/**
 * If keystore/truststore file type does not match declared one, communication between OIDC server
 * and client should fail.
 */
@Tag("fips-incompatible")
@QuarkusScenario
public class IncorrectKsFileTypeOidcMtlsIT extends BaseOidcMtlsIT {

    static final String KEYSTORE_FILE_EXTENSION = "jks";

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @KeycloakContainer(command = KC_DEV_MODE_JKS_CMD, image = "quay.io/keycloak/keycloak:19.0.1", port = KEYCLOAK_PORT)
    static KeycloakService keycloak = newKeycloakInstance(REALM_FILE_PATH, REALM_DEFAULT, "realms")
            .withRedHatFipsDisabled()
            .withProperty("HTTPS_KEYSTORE", "resource_with_destination::/etc/|server-keystore." + KEYSTORE_FILE_EXTENSION)
            .withProperty("HTTPS_TRUSTSTORE", "resource_with_destination::/etc/|server-truststore." + KEYSTORE_FILE_EXTENSION);

    @QuarkusApplication
    static RestService app = createRestService("incorrect-type", KEYSTORE_FILE_EXTENSION, keycloak::getRealmUrl);

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
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }

    @Override
    protected String getKeystoreFileExtension() {
        return KEYSTORE_FILE_EXTENSION;
    }
}
