package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.KC_DEV_MODE_JKS_CMD;
import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@Tag("fips-incompatible")
@QuarkusScenario
public class JksOidcMtlsIT extends KeycloakMtlsAuthN {

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @KeycloakContainer(command = KC_DEV_MODE_JKS_CMD, port = KEYCLOAK_PORT, builder = LocalHostKeycloakContainerManagedResourceBuilder.class)
    static KeycloakService keycloak = newKeycloakInstance(REALM_FILE_PATH, REALM_DEFAULT, "realms")
            .withRedHatFipsDisabled()
            .withProperty("HTTPS_KEYSTORE", "resource_with_destination::/etc/|server-keystore." + JKS_KEYSTORE_FILE_EXTENSION)
            .withProperty("HTTPS_TRUSTSTORE",
                    "resource_with_destination::/etc/|server-truststore." + JKS_KEYSTORE_FILE_EXTENSION);

    /**
     * Keystore file type is automatically detected by file extension by quarkus-oidc.
     */
    @QuarkusApplication
    static RestService app = createRestService(JKS_KEYSTORE_FILE_TYPE, JKS_KEYSTORE_FILE_EXTENSION, keycloak::getRealmUrl);

    @Override
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }

    @Override
    protected String getKeystoreFileExtension() {
        return JKS_KEYSTORE_FILE_EXTENSION;
    }

}
