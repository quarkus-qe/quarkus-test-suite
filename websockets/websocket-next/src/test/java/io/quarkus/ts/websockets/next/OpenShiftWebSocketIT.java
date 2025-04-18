package io.quarkus.ts.websockets.next;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftWebSocketIT extends BaseWebSocketIT {

    @QuarkusApplication
    protected static final RestService server = new RestService();

    @Override
    protected RestService getServer() {
        return server;
    }
}
