package io.quarkus.ts.http.restclient.reactive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.vertx.core.json.Json;

@QuarkusScenario
public class ClientBookResourceIT {

    @Test
    public void shouldGetBookFromRestClientJson() {
        given().with().pathParam("id", "123")
                .get("/client/{id}/book/json").then().statusCode(HttpStatus.SC_OK)
                .body(is(Json.encode(Map.of("title", "Title in Json: 123"))));
    }

    @Tag("QUARKUS-1568")
    @Test
    public void supportPathParamFromBeanParam() {
        given().with().pathParam("id", "123")
                .get("/client/{id}/book/jsonByBeanParam").then().statusCode(HttpStatus.SC_OK)
                .body(is(Json.encode(Map.of("title", "Title in Json: 123"))));
    }

}
