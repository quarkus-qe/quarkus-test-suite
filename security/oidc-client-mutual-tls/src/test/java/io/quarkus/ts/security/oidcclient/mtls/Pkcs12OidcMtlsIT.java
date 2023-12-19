package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

/**
 * Keystore file type is automatically detected in following tests by its extension in quarkus-oidc.
 * Extension declared here is used by tests only.
 */
@QuarkusScenario
public class Pkcs12OidcMtlsIT extends KeycloakMtlsAuthN {

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--hostname-strict=false",
            "--hostname-strict-https=false", "--features=token-exchange",
            "--hostname=localhost", // required by LocalHostKeycloakContainerManagedResourceBuilder
            "--https-client-auth=required", "--https-key-store-file=/etc/server-keystore.p12",
            "--https-trust-store-file=/etc/server-truststore.p12",
            "--https-trust-store-password=password" }, port = KEYCLOAK_PORT, builder = LocalHostKeycloakContainerManagedResourceBuilder.class)
    static KeycloakService keycloak = newKeycloakInstance(DEFAULT_REALM_FILE, REALM_DEFAULT, "realms")
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
