package io.quarkus.ts.configmap.api.server;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

public class OpenShiftApiServerConfigMapConfigIT extends OpenShiftBaseConfigIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("configmap.api-server.properties")
            .onPreStart(OpenShiftApiServerConfigMapConfigIT::loadDefaultConfigMap);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected String getConfigType() {
        return CONFIGMAP;
    }
}
