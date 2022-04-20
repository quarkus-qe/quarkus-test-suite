package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

/**
 * Keystore file type is automatically detected in following tests by its extension in quarkus-oidc.
 * Extension declared here is used by tests only.
 */
@QuarkusScenario
public class Pkcs12OidcMtlsIT extends KeycloakMtlsAuthN {

    @Container(image = "${keycloak.image}", expectedLog = EXPECTED_LOG, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = newKeycloakInstance();

    /**
     * Keystore file type is automatically detected by file extension by quarkus-oidc.
     */
    @QuarkusApplication
    static RestService app = createRestService(PKCS12_KEY_STORE_FILE_EXTENSION, "", keycloak::getRealmUrl);

    @Override
    protected String getKeyStoreFileExtension() {
        return PKCS12_KEY_STORE_FILE_EXTENSION;
    }

    @Override
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }
}
