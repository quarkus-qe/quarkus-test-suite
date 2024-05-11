package io.quarkus.ts.security.vertx;

import static org.hamcrest.Matchers.is;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
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

    @Tag("QUARKUS-2746 ")
    @Test
    public void verifyConsumeEventAnnotation() {
        List<String> actualLogs = app.getLogs();
        List<String> helloEvents = actualLogs.stream()
                .filter(l -> l.contains("Consuming generated HelloEvent at starting point"))
                .collect(Collectors.toList());

        Assertions.assertTrue(new HashSet<>(helloEvents).size() == helloEvents.size(),
                "@ConsumeEvent annotation should be invoked once per event");
    }
}
