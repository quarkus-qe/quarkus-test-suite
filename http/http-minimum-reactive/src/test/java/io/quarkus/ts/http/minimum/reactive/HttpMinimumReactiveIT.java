package io.quarkus.ts.http.minimum.reactive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class HttpMinimumReactiveIT {

    @Test
    public void httpServer() {
        given().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, World!"));
    }

    @Test
    @Tag("QUARKUS-1543")
    public void nativeListSerialization() {
        String teamId = (String) given().get("/api/cluster/default").then().statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getMap("clusters[0]")
                .get("team_id");

        assertEquals("qe", teamId);
    }
}
