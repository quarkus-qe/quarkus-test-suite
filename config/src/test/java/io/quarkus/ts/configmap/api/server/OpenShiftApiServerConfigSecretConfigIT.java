package io.quarkus.ts.configmap.api.server;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

public class OpenShiftApiServerConfigSecretConfigIT extends OpenShiftBaseConfigIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("secret.api-server.properties")
            .onPreStart(OpenShiftApiServerConfigSecretConfigIT::loadDefaultConfigSecret);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected String getConfigType() {
        return SECRET;
    }
}
