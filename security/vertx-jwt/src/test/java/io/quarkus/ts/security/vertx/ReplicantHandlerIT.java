package io.quarkus.ts.security.vertx;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.http.ContentType;

@QuarkusScenario
public class ReplicantHandlerIT extends AbstractCommonIT {
    @Test
    public void retrieveReplicantById() {
        app.given().accept(ContentType.JSON)
                .when()
                .get("/replicant/" + replicant.getId())
                .then()
                .statusCode(200);
    }

    @Test
    public void retrieveAllReplicant() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.EMPTY, "admin"))
                .when()
                .get("/replicant/")
                .then()
                .assertThat().body("size()", is(1))
                .statusCode(200);
    }

    @Test
    public void deleteReplicant() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.EMPTY, "admin"))
                .when()
                .delete("/replicant/" + replicant.getId())
                .then()
                .statusCode(204);
        app.given().accept(ContentType.JSON)
                .when()
                .get("/replicant/" + replicant.getId())
                .then()
                .statusCode(404);
    }
}
