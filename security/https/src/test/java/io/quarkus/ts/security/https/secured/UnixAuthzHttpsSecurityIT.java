package io.quarkus.ts.security.https.secured;

import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnOs(OS.WINDOWS)
public class UnixAuthzHttpsSecurityIT extends BaseAuthzHttpsSecurityIT {

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService();

    @Override
    public RestService getApp() {
        return app;
    }
}
