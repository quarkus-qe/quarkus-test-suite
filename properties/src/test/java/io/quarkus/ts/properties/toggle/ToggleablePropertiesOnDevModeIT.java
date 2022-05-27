package io.quarkus.ts.properties.toggle;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class ToggleablePropertiesOnDevModeIT extends BaseToggleablePropertiesIT {

    private static final String APPLICATION_PROPERTIES = "src/main/resources/application.properties";

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = new DevModeQuarkusService();

    @Override
    protected RequestSpecification given() {
        return app.given();
    }

    @Override
    protected void whenChangeServiceAtRuntime(ToggleableServices service, boolean enable) {
        app.modifyFile(APPLICATION_PROPERTIES,
                s -> s.replace(service.getToggleProperty() + "=" + !enable, service.getToggleProperty() + "=" + enable));
    }
}
