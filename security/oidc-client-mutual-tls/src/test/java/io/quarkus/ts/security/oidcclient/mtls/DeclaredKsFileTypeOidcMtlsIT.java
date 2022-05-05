package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

/**
 * If keystore/truststore file type can't be detected from file extension, user can explicitly
 * declare file type in application properties.
 */
@Tag("fips-incompatible")
@QuarkusScenario
public class DeclaredKsFileTypeOidcMtlsIT extends KeycloakMtlsAuthN {

    /**
     * Custom keystore extension.
     */
    private static final String KEY_STORE_FILE_EXTENSION = "unknown-extension";

    /**
     * Actual Keystore file type is PKCS12.
     */
    private static final String KEY_STORE_FILE_TYPE = "PKCS12";

    @Container(image = "${keycloak.image}", expectedLog = EXPECTED_LOG, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = newKeycloakInstance();

    /**
     * Tells quarkus-oidc to use {@link DeclaredKsFileTypeOidcMtlsIT#KEY_STORE_FILE_TYPE}
     * as it can't autodetect keystore file type from extension.
     */
    @QuarkusApplication
    static RestService app = createRestService(KEY_STORE_FILE_EXTENSION, KEY_STORE_FILE_TYPE,
            keycloak::getRealmUrl);

    @Override
    protected String getKeyStoreFileExtension() {
        return KEY_STORE_FILE_EXTENSION;
    }

    @Override
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }
}
