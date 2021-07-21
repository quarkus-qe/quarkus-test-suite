package io.quarkus.ts.security.vertx;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.http.ContentType;

@QuarkusScenario
public class BladeRunnerHandlerIT extends AbstractCommonIT {
    @Test
    public void retrieveBladeRunnerById() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.EMPTY, "admin"))
                .when()
                .get("/bladeRunner/" + bladeRunner.getId())
                .then()
                .statusCode(200);
    }

    @Test
    public void retrieveAllBladeRunners() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.EMPTY, "admin"))
                .when()
                .get("/bladeRunner/")
                .then()
                .assertThat().body("size()", is(1))
                .statusCode(200);
    }

    @Test
    public void deleteBladeRunner() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.EMPTY, "admin"))
                .when()
                .delete("/bladeRunner/" + bladeRunner.getId())
                .then()
                .statusCode(204);
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.EMPTY, "admin"))
                .when()
                .get("/bladeRunner/" + bladeRunner.getId())
                .then()
                .statusCode(404);
    }
}
