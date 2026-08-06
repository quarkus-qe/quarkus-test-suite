package io.quarkus.ts.http.restclient.reactive;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-8369")
@QuarkusScenario
public class RestMultiResponseIT {
    @QuarkusApplication(properties = "modern.properties")
    static RestService app = new RestService();

    @Test
    public void shouldConsumeObjectSseStream() {
        app.given()
                .get("/rest-multi/verify/objects")
                .then()
                .statusCode(200)
                .header("Rest-Client-Status", equalTo("200"))
                .body("value", contains("one", "two"));
    }

    @Test
    public void shouldConsumeObjectSseStreamWithoutProduces() {
        app.given()
                .get("/rest-multi/verify/objects-without-produces")
                .then()
                .statusCode(200)
                .header("Rest-Client-Status", equalTo("200"))
                .body("value", contains("one", "two"));
    }

    @Test
    public void shouldConsumeStringSseStream() {
        app.given()
                .get("/rest-multi/verify/strings")
                .then()
                .statusCode(200)
                .header("Rest-Client-Status", equalTo("200"))
                .body("", contains("one", "two"));
    }

    @Test
    public void shouldReadCustomHeaderFromSseResponseMetadata() {
        app.given()
                .get("/rest-multi/verify/header")
                .then()
                .statusCode(200)
                .header("Rest-Client-Status", equalTo("200"))
                .header("Custom-Header", equalTo("hello"))
                .body("", contains("one", "two"));
    }
}
