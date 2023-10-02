package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.KC_DEV_MODE_P12_CMD;
import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.condition.DisabledIf;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.configuration.PropertyLookup;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.Command;

/**
 * Keystore file type is automatically detected in following tests by its extension in quarkus-oidc.
 * Extension declared here is used by tests only.
 */
@DisabledIf(value = "cannotRunOnFIPS", disabledReason = "PKCS12 keystore is not FIPS compliant on Red Hat OpenJDK 11")
@QuarkusScenario
public class Pkcs12OidcMtlsIT extends KeycloakMtlsAuthN {

    @KeycloakContainer(command = KC_DEV_MODE_P12_CMD, port = KEYCLOAK_PORT, builder = LocalHostKeycloakContainerManagedResourceBuilder.class)
    static KeycloakService keycloak = newKeycloakInstance(REALM_FILE_PATH, REALM_DEFAULT, "realms")
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

    private static boolean cannotRunOnFIPS() {
        String javaVersion = new PropertyLookup("java.version").get();
        String javaVMVendor = new PropertyLookup("java.vm.vendor").get();

        if (javaVersion.matches("11.*") && javaVMVendor.matches(".*Red.*Hat.*")) {
            List<String> commandOutputLines = new ArrayList<>();

            try {
                new Command("sysctl", "crypto.fips_enabled").outputToLines(commandOutputLines).runAndWait();
            } catch (IOException | InterruptedException e) {
                return false;
            }

            boolean isFipsEnabled = commandOutputLines.get(0).matches(".*1");

            return javaVersion.matches("11.*") && javaVMVendor.matches(".*Red.*Hat.*") && isFipsEnabled;
        }

        return false;
    }
}
