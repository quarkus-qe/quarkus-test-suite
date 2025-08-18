package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;

@QuarkusScenario
public class OidcMtlsBindingIT extends AbstractOidcMtlsBindingIT {

    @KeycloakContainer(exposeRawTls = true, command = { "start", "--import-realm", "--hostname-strict=false",
            "--features=token-exchange",
            "--hostname=localhost",
            "--https-client-auth=required", "--https-key-store-file=/etc/server-keystore.p12",
            "--https-trust-store-file=/etc/server-truststore.p12",
            "--https-trust-store-password=password" }, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = newKeycloakInstance(DEFAULT_REALM_FILE, REALM_DEFAULT, DEFAULT_REALM_BASE_PATH)
            .withProperty("HTTPS_KEYSTORE", "resource_with_destination::/etc/|server-keystore.p12")
            .withProperty("HTTPS_TRUSTSTORE", "resource_with_destination::/etc/|server-truststore.p12");

}
