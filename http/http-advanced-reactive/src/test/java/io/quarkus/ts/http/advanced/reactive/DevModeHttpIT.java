package io.quarkus.ts.http.advanced.reactive;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.services.URILike;

@QuarkusScenario
public class DevModeHttpIT extends AbstractDevModeIT {

    @DevModeQuarkusApplication(ssl = false)
    static RestService app = new DevModeQuarkusService()
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.keycloak.policy-enforcer.enable", "false")
            .withProperty("quarkus.keycloak.devservices.enabled", "false");

    @Override
    protected URILike getUri() {
        return app.getURI(Protocol.HTTP);
    }

    @Override
    protected RestService getApp() {
        return app;
    }
}
