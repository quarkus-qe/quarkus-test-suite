package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newRhSsoInstance;
import static org.keycloak.representations.idm.CredentialRepresentation.PASSWORD;

import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@Disabled("https://github.com/quarkusio/quarkus/issues/38803") //TODO mvavrik: fixing this will probably require fixing config map names created for resources
@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/1145")
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftRhSsoOidcMtlsIT extends KeycloakMtlsAuthN {

    @KeycloakContainer(command = { "start-dev", "--import-realm" }, image = "${rhbk.image}")
    static KeycloakService rhsso = newRhSsoInstance("/keycloak-realm.json", REALM_DEFAULT);

    /**
     * Keystore file type is automatically detected by file extension by quarkus-oidc.
     */
    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", rhsso::getRealmUrl)
            .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT)
            .withProperty("store-file-extension", JKS_KEYSTORE_FILE_TYPE)
            .withProperty("ks-file", "rhsso-client-keystore.jks")
            .withProperty("ts-file", "rhsso-client-truststore.jks")
            .withProperty("store-pwd", PASSWORD);

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

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected String getExpectedMtlsPrincipal() {
        // FIXME: impl. me before enabling the test as currently this can't be tested
        throw new UnsupportedOperationException("Needs to be fixed when the test is reworked");
    }
}
