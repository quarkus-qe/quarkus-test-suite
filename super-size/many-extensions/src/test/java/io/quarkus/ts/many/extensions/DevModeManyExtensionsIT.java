package io.quarkus.ts.many.extensions;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
@DisabledOnNative
public class DevModeManyExtensionsIT extends ManyExtensionsIT {

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = new DevModeQuarkusService();

    @Override
    protected RequestSpecification given() {
        return app.given();
    }
}
