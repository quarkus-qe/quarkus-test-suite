package io.quarkus.ts.security.vertx;

import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class NoSecuredResourceIT extends AbstractCommonIT {
    @Test
    public void noSecuredHttpServer() {
        app.given().when().get("/replicant/noExistID")
                .then()
                .statusCode(404);
    }
}
