package io.quarkus.qe.messaging.infinispan.greeting;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

// TODO: change to @QuarkusScenario once https://github.com/quarkus-qe/quarkus-test-framework/issues/292 is solved
@QuarkusTest
public class GreetingServiceTest {

    @Test
    public void testBlockingGreetingResource() {
        given()
                .when().get("/hello/blocking/neo")
                .then().statusCode(200)
                .body(is("Hello neo"));
    }

    @Test
    public void testMutinyGreetingResource() {
        given()
                .when().get("/hello/mutiny/neo")
                .then().statusCode(200)
                .body(is("Hello neo"));
    }

}
