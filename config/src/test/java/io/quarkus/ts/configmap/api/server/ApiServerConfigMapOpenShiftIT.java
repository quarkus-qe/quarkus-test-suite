package io.quarkus.ts.configmap.api.server;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

public class ApiServerConfigMapOpenShiftIT extends BaseConfigOpenShiftIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("configmap.api-server.properties")
            .onPreStart(ApiServerConfigMapOpenShiftIT::loadDefaultConfigMap);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected String getConfigType() {
        return CONFIGMAP;
    }
}
