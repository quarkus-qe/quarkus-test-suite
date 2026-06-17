package io.quarkus.ts.reactive.rest.data.panache;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnNative(reason = "H2 database compiled into a native-image is only functional as a client")
public class H2SqlPanacheResourceIT extends AbstractPanacheResourceIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("h2.properties");
}
