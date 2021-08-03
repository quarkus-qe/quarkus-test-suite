package io.quarkus.ts.security.https.enabled;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.vertx.http.runtime.HttpConfiguration;

@QuarkusScenario
@EnabledOnOs(OS.WINDOWS)
public class WindowsEnabledHttpsSecurityIT extends BaseEnabledHttpsSecurityIT {

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService().withProperties("windows.properties")
            .withProperty("quarkus.http.insecure-requests", HttpConfiguration.InsecureRequests.ENABLED.name());

    @Override
    public RestService getApp() {
        return app;
    }
}
