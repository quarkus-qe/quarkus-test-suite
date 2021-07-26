package io.quarkus.ts.kamelet;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusScenario
class KameletIT {

    @Test
    public void testKameletProducerHelloWorld() {
        String message = "World";

        RestAssured.given()
                .contentType(ContentType.TEXT)
                .body(message)
                .post("/kamelet/produce")
                .then()
                .statusCode(200)
                .body(is("Hello " + message));
    }

    @Test
    public void testKameletTimerConsumer() {
        RestAssured.given().get("/kamelet/timer")
                .then()
                .statusCode(200)
                .body(is("1"));
    }

    @Test
    public void testKameletWithProperties() {
        RestAssured.given().get("/kamelet/property")
                .then()
                .statusCode(200)
                .body(is("Hello World from property"));
    }

    @Test
    public void testKameletChain() {
        RestAssured.given()
                .contentType(ContentType.TEXT)
                .body("Kamelet")
                .post("/kamelet/chain")
                .then()
                .statusCode(200)
                .body(is("Hello Camel Quarkus Kamelet Chained Route"));
    }
}
