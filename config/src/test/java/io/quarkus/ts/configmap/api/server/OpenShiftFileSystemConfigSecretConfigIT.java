package io.quarkus.ts.configmap.api.server;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

@Disabled("https://github.com/quarkusio/quarkus/issues/38018")
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
