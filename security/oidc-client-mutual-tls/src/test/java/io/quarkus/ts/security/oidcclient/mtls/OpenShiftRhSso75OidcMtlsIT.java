package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newRhSsoInstance;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftRhSso75OidcMtlsIT extends KeycloakMtlsAuthN {

    @Container(image = "${rhsso.75.image}", expectedLog = EXPECTED_LOG, port = KEYCLOAK_PORT)
    static KeycloakService rhsso = newRhSsoInstance().withRedHatFipsDisabled();

    /**
     * Keystore file type is automatically detected by file extension by quarkus-oidc.
     */
    @QuarkusApplication
    static RestService app = createRestService(JKS_KEY_STORE_FILE_EXTENSION, "", rhsso::getRealmUrl);

    @Override
    protected String getKeyStoreFileExtension() {
        return JKS_KEY_STORE_FILE_EXTENSION;
    }

    @Override
    protected KeycloakService getKeycloakService() {
        return rhsso;
    }
}
