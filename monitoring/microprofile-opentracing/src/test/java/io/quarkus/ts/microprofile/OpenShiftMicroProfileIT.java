package io.quarkus.ts.microprofile;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;

@OpenShiftScenario
@DisabledOnQuarkusVersion(version = "1\\.3\\..*", reason = "https://github.com/quarkusio/quarkus/pull/7987")
public class OpenShiftMicroProfileIT extends MicroProfileIT {

    private static final int QUARKUS_INTERNAL_HTTP_PORT = 8080;

    @Override
    protected int getAppPort() {
        return QUARKUS_INTERNAL_HTTP_PORT;
    }
}
