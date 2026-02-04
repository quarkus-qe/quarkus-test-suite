package io.quarkus.ts.http.advanced;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.s390x.missing.services.excludes", matches = "true", disabledReason = "Quarkus QE TS doesn't support TLS on Openshift for s390x")
public class OpenShiftHttpAdvancedIT extends BaseHttpAdvancedIT {

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--hostname-strict=false" }, image = "${rhbk.image}")
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(ssl = true, certificates = @Certificate(configureKeystore = true, configureHttpServer = true, useTlsRegistry = false))
    static RestService app = new RestService().withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected Protocol getProtocol() {
        // HTTPs is not supported in OpenShift yet. The same happens in OpenShift TS.
        return Protocol.HTTP;
    }

    @Override
    @Test
    @DisplayName("Http/2 Server test")
    @DisabledIfSystemProperty(named = "ts.s390x.missing.services.excludes", matches = "true", disabledReason = "Quarkus QE TS doesn't support TLS on Openshift for s390x")
    public void http2Server() throws InterruptedException {
        super.http2Server();
    }

}
