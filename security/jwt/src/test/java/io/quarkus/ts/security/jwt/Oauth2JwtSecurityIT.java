package io.quarkus.ts.security.jwt;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
@Tag("fips-incompatible")
public class Oauth2JwtSecurityIT extends BaseJwtSecurityIT {

    @Override
    protected RequestSpecification givenWithToken(String token) {
        return given().when().auth().oauth2(token);
    }
}
