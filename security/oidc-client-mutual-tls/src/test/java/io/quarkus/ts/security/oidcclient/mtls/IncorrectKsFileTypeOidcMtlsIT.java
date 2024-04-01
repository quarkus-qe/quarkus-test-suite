package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
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
    @KeycloakContainer(command = { "start-dev", "--import-realm", "--hostname-strict=false",
            "--hostname-strict-https=false", "--features=token-exchange",
            "--hostname=localhost", // required by LocalHostKeycloakContainerManagedResourceBuilder
            "--https-client-auth=required", "--https-key-store-file=/etc/server-keystore.jks",
            "--https-trust-store-file=/etc/server-truststore.jks",
            "--https-trust-store-password=password" }, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = newKeycloakInstance(DEFAULT_REALM_FILE, REALM_DEFAULT, "realms")
            .withRedHatFipsDisabled()
            .withProperty("HTTPS_KEYSTORE", "resource_with_destination::/etc/|server-keystore." + KEYSTORE_FILE_EXTENSION)
            .withProperty("HTTPS_TRUSTSTORE", "resource_with_destination::/etc/|server-truststore." + KEYSTORE_FILE_EXTENSION);

    @QuarkusApplication
    static RestService app = createRestService("incorrect-type", KEYSTORE_FILE_EXTENSION, keycloak::getRealmUrl);

    @Test
    void failAuthenticate() {
        app
                .given()
                .when()
                .auth().oauth2(getToken(NORMAL_USER))
                .get(RESOURCE_PATH + "/oidc")
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
