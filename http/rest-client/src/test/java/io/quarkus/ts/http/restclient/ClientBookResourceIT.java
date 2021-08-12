package io.quarkus.ts.http.restclient;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class ClientBookResourceIT {

    @Test
    public void shouldGetBookFromRestClientXml() {
        given().get("/client/book/xml").then().statusCode(HttpStatus.SC_OK)
                .body(is(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><book><title>Title in Xml</title></book>"));
    }

    @Test
    public void shouldGetBookFromRestClientJson() {
        given().get("/client/book/json").then().statusCode(HttpStatus.SC_OK)
                .body(is("{\"title\":\"Title in Json\"}"));
    }
}
