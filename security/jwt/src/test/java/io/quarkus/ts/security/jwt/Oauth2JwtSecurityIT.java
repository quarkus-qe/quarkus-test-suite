package io.quarkus.ts.security.jwt;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class Oauth2JwtSecurityIT extends BaseJwtSecurityIT {
    @QuarkusApplication
    static RestService app = new RestService();

    @Override
    protected RequestSpecification givenWithToken(String token) {
        return app.given().when().auth().oauth2(token);
    }
}
