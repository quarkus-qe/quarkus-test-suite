package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class ReactiveRestClientIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("modern.properties");

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "https://github.com/quarkusio/quarkus/issues/24763")
    public void shouldGetBookFromRestClientJson() {
        Response response = app.given().with().pathParam("id", "123")
                .get("/client/book/{id}/json");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Title in Json: 123", response.jsonPath().getString("title"));
    }

    @Tag("QUARKUS-1568")
    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "https://github.com/quarkusio/quarkus/issues/24763")
    public void supportPathParamFromBeanParam() {
        Response response = app.given().with().pathParam("id", "123")
                .get("/client/book/{id}/jsonByBeanParam");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Title in Json: 123", response.jsonPath().getString("title"));
    }

    @Test
    public void mapInQueryParam() {
        Response response = app.given()
                .when()
                .queryParam("param", "{\"id\":\"Hagakure\",\"author\":\"Tsuramoto\"}")
                .get("/books/map");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Hagakure", response.jsonPath().getString("title"));
        assertEquals("Tsuramoto", response.jsonPath().getString("author"));
    }
}
