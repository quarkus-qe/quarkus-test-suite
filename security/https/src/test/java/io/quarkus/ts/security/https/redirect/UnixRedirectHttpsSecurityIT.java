package io.quarkus.ts.security.https.redirect;

import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.vertx.http.runtime.HttpConfiguration;

@QuarkusScenario
@DisabledOnOs(OS.WINDOWS)
public class UnixRedirectHttpsSecurityIT extends BaseRedirectHttpsSecurityIT {

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService()
            .withProperty("quarkus.http.insecure-requests", HttpConfiguration.InsecureRequests.REDIRECT.name());

    @Override
    public RestService getApp() {
        return app;
    }
}
