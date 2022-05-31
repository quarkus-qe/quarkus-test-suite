package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

//TODO https://github.com/keycloak/keycloak/issues/9967
@Disabled
@QuarkusScenario
public class BcfksOidcMtlsIT extends KeycloakMtlsAuthN {

    private static final String KS_TYPE = "BCFKS";
    private static final String BCFIPS = BouncyCastleFipsProvider.PROVIDER_NAME;
    private static final String BCFKS_CLIENT_KEYSTORE_PATH = "bcfks-client-keystore.";
    private static final String BCFKS_CLIENT_TRUSTSTORE_PATH = "bcfks-client-truststore.";

    @Container(image = "${keycloak.image}", expectedLog = EXPECTED_LOG, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = newKeycloakInstance();

    @QuarkusApplication
    static RestService app = createRestService(JKS_KEY_STORE_FILE_EXTENSION, KS_TYPE, keycloak::getRealmUrl)
            .withProperty("quarkus.oidc.tls.key-store-file", "bcfks-client-keystore.jks")
            .withProperty("quarkus.oidc.tls.trust-store-file", "bcfks-client-truststore.jks")
            .withProperty("quarkus.oidc.tls.trust-store-provider", BCFIPS)
            .withProperty("quarkus.oidc.tls.key-store-provider", BCFIPS);

    @Override
    protected String getKeyStoreFileExtension() {
        return JKS_KEY_STORE_FILE_EXTENSION;
    }

    @Override
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }

    @Override
    protected String getTrustStorePath() {
        return BCFKS_CLIENT_TRUSTSTORE_PATH + getKeyStoreFileExtension();
    }

    @Override
    protected String getKeyStorePath() {
        return BCFKS_CLIENT_KEYSTORE_PATH + getKeyStoreFileExtension();
    }

}
