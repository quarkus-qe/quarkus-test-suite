package io.quarkus.ts.microprofile.opentracing;

import io.quarkus.test.scenarios.OpenShiftScenario;

// I just want to trigger a matrix build in Jenkins workflow.
@OpenShiftScenario
public class OpenShiftMicroProfileIT extends MicroProfileIT {

    private static final int QUARKUS_INTERNAL_HTTP_PORT = 8080;

    @Override
    protected int getAppPort() {
        return QUARKUS_INTERNAL_HTTP_PORT;
    }
}
