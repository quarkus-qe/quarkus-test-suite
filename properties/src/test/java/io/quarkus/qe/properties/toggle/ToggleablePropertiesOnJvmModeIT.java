package io.quarkus.qe.properties.toggle;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class ToggleablePropertiesOnJvmModeIT extends BaseToggleablePropertiesIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService();

    @Override
    protected RequestSpecification given() {
        return app.given();
    }

    @Override
    protected void whenChangeServiceAtRuntime(ToggleableServices service, boolean enable) {
        app.stop();
        app.withProperty(service.getToggleProperty(), "" + enable);
        app.start();
    }
}
