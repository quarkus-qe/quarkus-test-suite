package io.quarkus.ts.elasticsearch;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.restassured.http.ContentType;

@QuarkusScenario
public class DevModeElasticsearchIT {

    @DevModeQuarkusApplication()
    static RestService devModeApp = new RestService();

    @Test
    void dataCreatedAndAccessible() {
        devModeApp.given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"banány\", \"color\": \"žlutá\"}")
                .when()
                .post("fruits")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        await().ignoreExceptions().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            devModeApp.given()
                    .when()
                    .get("fruits/search?color=žlutá")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body(containsString("banány"));
        });
    }
}
