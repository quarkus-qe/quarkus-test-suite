package io.quarkus.ts.configmap.api.server;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

public class ApiServerConfigMapOpenShiftIT extends BaseConfigMapOpenShiftIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("api-server.application.properties")
            .onPreStart(ApiServerConfigMapOpenShiftIT::loadDefaultConfigMap);

    @Override
    protected RestService getApp() {
        return app;
    }
}
