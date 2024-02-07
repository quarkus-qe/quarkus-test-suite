package io.quarkus.ts.configmap.api.server;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

public class OpenShiftFileSystemConfigMapConfigIT extends OpenShiftBaseConfigIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("configmap.file-system.properties")
            .onPreStart(OpenShiftFileSystemConfigMapConfigIT::loadDefaultConfigMap);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected String getConfigType() {
        return CONFIGMAP;
    }
}
