package io.quarkus.ts.many.extensions;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
public class DevModeManyExtensionsIT extends ManyExtensionsIT {

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = new DevModeQuarkusService();
}
