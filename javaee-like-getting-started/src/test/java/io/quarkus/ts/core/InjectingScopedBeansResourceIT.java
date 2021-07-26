package io.quarkus.ts.core;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class InjectingScopedBeansResourceIT {
    @Test
    public void shouldInjectScopedBeans() {
        given().when().get("/scopedbeans/sessionId")
                .then().body(notNullValue());

        given().when().get("/scopedbeans/requestId")
                .then().body(notNullValue());

        given().get("/scopedbeans/contextPath")
                .then().body(notNullValue());
    }
}
