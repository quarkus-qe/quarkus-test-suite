package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class JksOidcMtlsIT extends KeycloakMtlsAuthN {

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @KeycloakContainer(command = { "start-dev", "--import-realm", "--hostname-strict=false",
            "--features=token-exchange",
            "--hostname=localhost", // required by LocalHostKeycloakContainerManagedResourceBuilder
            "--https-client-auth=required", "--https-key-store-file=/etc/server-keystore.jks",
            "--https-trust-store-file=/etc/server-truststore.jks",
            "--https-trust-store-password=password" }, port = KEYCLOAK_PORT, builder = LocalHostKeycloakContainerManagedResourceBuilder.class)
    static KeycloakService keycloak = newKeycloakInstance(DEFAULT_REALM_FILE, REALM_DEFAULT, "realms")
            .withRedHatFipsDisabled()
            .withProperty("HTTPS_KEYSTORE", "resource_with_destination::/etc/|server-keystore." + JKS_KEYSTORE_FILE_EXTENSION)
            .withProperty("HTTPS_TRUSTSTORE",
                    "resource_with_destination::/etc/|server-truststore." + JKS_KEYSTORE_FILE_EXTENSION);

    /**
     * Keystore file type is automatically detected by file extension by quarkus-oidc.
     */
    @QuarkusApplication(ssl = true)
    static RestService app = createRestService(JKS_KEYSTORE_FILE_TYPE, JKS_KEYSTORE_FILE_EXTENSION,
            keycloak::getRealmUrl);

    @Tag("QUARKUS-3466")
    @Test
    void verifyMtlsRolesPolicySuccess() {
        verifyAuthSuccess("/mtls/roles-policy", getExpectedMtlsPrincipal());
    }

    @Tag("QUARKUS-3466")
    @Test
    void verifyMtlsPermissionsAllowedFailure() {
        verifyUnauthorized("/mtls/permissions-allowed");
    }

    @Override
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }

    @Override
    protected String getKeystoreFileExtension() {
        return JKS_KEYSTORE_FILE_EXTENSION;
    }

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected String getExpectedMtlsPrincipal() {
        return "CN=client,OU=cert,O=quarkus,L=city,ST=state,C=AU";
    }
}
