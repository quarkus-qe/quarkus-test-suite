package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.KC_DEV_MODE_P12_CMD;
import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

/**
 * Keystore file type is automatically detected in following tests by its extension in quarkus-oidc.
 * Extension declared here is used by tests only.
 */
@Tag("fips-incompatible")
@QuarkusScenario
public class Pkcs12OidcMtlsIT extends KeycloakMtlsAuthN {

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @KeycloakContainer(command = KC_DEV_MODE_P12_CMD, image = "quay.io/keycloak/keycloak:19.0.1", port = KEYCLOAK_PORT)
    static KeycloakService keycloak = newKeycloakInstance(REALM_FILE_PATH, REALM_DEFAULT, "realms")
            .withRedHatFipsDisabled()
            .withProperty("HTTPS_KEYSTORE", "resource_with_destination::/etc/|server-keystore." + P12_KEYSTORE_FILE_EXTENSION)
            .withProperty("HTTPS_TRUSTSTORE",
                    "resource_with_destination::/etc/|server-truststore." + P12_KEYSTORE_FILE_EXTENSION);

    /**
     * Keystore file type is automatically detected by file extension by quarkus-oidc.
     */
    @QuarkusApplication
    static RestService app = createRestService(P12_KEYSTORE_FILE_TYPE, P12_KEYSTORE_FILE_EXTENSION, keycloak::getRealmUrl)
            .withProperty("quarkus.oidc.tls.trust-store-file", "client-truststore." + P12_KEYSTORE_FILE_EXTENSION)
            .withProperty("quarkus.oidc.tls.key-store-file", "client-keystore." + P12_KEYSTORE_FILE_EXTENSION);

    @Override
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }

    @Override
    protected String getKeystoreFileExtension() {
        return P12_KEYSTORE_FILE_EXTENSION;
    }
}
