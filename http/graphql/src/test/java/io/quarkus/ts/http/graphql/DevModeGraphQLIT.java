package io.quarkus.ts.http.graphql;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
public class DevModeGraphQLIT extends AbstractGraphQLIT {

    @DevModeQuarkusApplication
    static final RestService app = new RestService();

}
