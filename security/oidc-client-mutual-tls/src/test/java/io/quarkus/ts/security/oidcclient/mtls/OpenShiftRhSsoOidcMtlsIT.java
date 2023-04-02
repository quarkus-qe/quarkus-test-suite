package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newRhSsoInstance;
import static org.keycloak.representations.idm.CredentialRepresentation.PASSWORD;

import java.nio.file.Paths;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/1145")
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftRhSsoOidcMtlsIT extends KeycloakMtlsAuthN {

    protected static final String EXPECTED_LOG = "Http management interface listening";

    @Container(image = "${rhsso.image}", expectedLog = EXPECTED_LOG, port = KEYCLOAK_PORT)
    static KeycloakService rhsso = newRhSsoInstance("/keycloak-realm.json", REALM_DEFAULT).withRedHatFipsDisabled();

    /**
     * Keystore file type is automatically detected by file extension by quarkus-oidc.
     */
    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", rhsso::getRealmUrl)
            .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.tls.trust-store-file-type", JKS_KEYSTORE_FILE_EXTENSION)
            .withProperty("quarkus.oidc.tls.key-store-file-type", JKS_KEYSTORE_FILE_EXTENSION)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT)
            .withProperty("ks-file-extension", JKS_KEYSTORE_FILE_TYPE)
            .withProperty("quarkus.oidc.tls.key-store-file", "rhsso-client-keystore.jks")
            .withProperty("quarkus.oidc.tls.trust-store-file", "rhsso-client-truststore.jks")
            .withProperty("ks-pwd", PASSWORD);

    @Override
    protected String getKeystoreFileExtension() {
        return JKS_KEYSTORE_FILE_EXTENSION;
    }

    @Override
    protected KeycloakService getKeycloakService() {
        return rhsso;
    }

    @Override
    protected String getTrustStorePath() {
        String truststore = "rhsso-client-truststore." + getKeystoreFileExtension();
        return Paths.get("src", "main", "resources", truststore).toAbsolutePath().toString();
    }

    @Override
    protected String getKeyStorePath() {
        String keystore = "rhsso-client-keystore." + getKeystoreFileExtension();
        return Paths.get("src", "main", "resources", keystore).toAbsolutePath().toString();
    }
}
