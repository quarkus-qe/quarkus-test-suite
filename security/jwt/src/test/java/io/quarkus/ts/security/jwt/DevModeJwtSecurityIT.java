package io.quarkus.ts.security.jwt;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class DevModeJwtSecurityIT extends BaseJwtSecurityIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService();

    @Override
    protected RequestSpecification givenWithToken(String token) {
        return app.given().auth().oauth2(token);
    }
}
