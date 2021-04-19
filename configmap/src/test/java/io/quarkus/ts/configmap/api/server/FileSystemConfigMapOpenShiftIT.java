package io.quarkus.ts.configmap.api.server;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

public class FileSystemConfigMapOpenShiftIT extends BaseConfigMapOpenShiftIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("file-system.application.properties")
            .onPreStart(FileSystemConfigMapOpenShiftIT::loadDefaultConfigMap);

    @Override
    protected RestService getApp() {
        return app;
    }
}
