package io.quarkus.ts.security.https.secured;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@EnabledOnOs(OS.WINDOWS)
public class WindowsAuthzHttpsSecurityIT extends BaseAuthzHttpsSecurityIT {

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService().withProperties("windows.properties");

    @Override
    public RestService getApp() {
        return app;
    }
}
