package io.quarkus.ts.configmap.api.server;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

public class OpenShiftFileSystemConfigSecretConfigIT extends OpenShiftBaseConfigIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("secret.file-system.properties")
            .onPreStart(OpenShiftFileSystemConfigSecretConfigIT::loadDefaultConfigSecret);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected String getConfigType() {
        return SECRET;
    }
}
