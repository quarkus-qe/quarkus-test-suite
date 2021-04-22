package io.quarkus.ts.security.jwt;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class CookieJwtSecurityIT extends BaseJwtSecurityIT {

    static final String COOKIE_NAME = "MY_COOKIE_NAME";

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("smallrye.jwt.token.header", "Cookie")
            .withProperty("smallrye.jwt.token.cookie", COOKIE_NAME);

    @Override
    protected RequestSpecification givenWithToken(String token) {
        return app.given().when().cookie(COOKIE_NAME, token);
    }
}
