package io.quarkus.ts.micrometer.oidc;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
public class DevMicrometerOidcSecurityIT extends BaseMicrometerOidcSecurityIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT)
            .withProperties(keycloak::getTlsProperties);

    @Override
    public RestService getApp() {
        return app;
    }
}
