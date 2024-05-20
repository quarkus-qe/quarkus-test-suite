package io.quarkus.ts.http.advanced.reactive;

import org.junit.jupiter.api.BeforeEach;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.services.URILike;

@QuarkusScenario
public class DevModeHttpsIT extends AbstractDevModeIT {

    @DevModeQuarkusApplication(ssl = true, certificates = @Certificate(configureKeystore = true))
    static RestService app = new DevModeQuarkusService()
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.keycloak.policy-enforcer.enable", "false")
            .withProperty("quarkus.keycloak.devservices.enabled", "false");

    @BeforeEach
    void setUp() {
        super.setUp();
        //The execution breaks without the option below
        webClient.getOptions().setUseInsecureSSL(true);
    }

    @Override
    protected URILike getUri() {
        return app.getURI(Protocol.HTTPS);
    }

    @Override
    protected RestService getApp() {
        return app;
    }
}
