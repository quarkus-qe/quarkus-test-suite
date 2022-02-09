package io.quarkus.ts.http.minimum.reactive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class HttpMinimumReactiveIT {

    private RequestSpecification HTTP_CLIENT_SPEC = given();

    @Test
    public void httpServer() {
        givenSpec().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Hello, World!"));
    }

    @Test
    @Tag("QUARKUS-1543")
    public void nativeListSerialization() {
        String teamId = (String) givenSpec().get("/api/cluster/default").then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getMap("clusters[0]")
                .get("team_id");

        assertEquals("qe", teamId);
    }

    protected RequestSpecification givenSpec() {
        return HTTP_CLIENT_SPEC;
    }
}
