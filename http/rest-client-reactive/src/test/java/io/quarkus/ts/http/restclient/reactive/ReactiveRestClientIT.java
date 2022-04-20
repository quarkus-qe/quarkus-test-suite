package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
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

    @Test
    public void resourceDirectly() {
        Response response = app.given()
                .when()
                .get("/books/?title=Catch-22&author=Heller");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Catch-22", response.jsonPath().getString("title"));
        assertEquals("Heller", response.jsonPath().getString("author"));
    }

    @Test
    public void resourceClient() {
        Response response = app.given()
                .when()
                .get("/client/book/?title=Catch-22&author=Heller");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Catch-22", response.jsonPath().getString("title"));
        assertEquals("Heller", response.jsonPath().getString("author"));
    }

    @Test
    public void subResourceDirectly() {
        Response response = app.given()
                .when()
                .get("/books/author/name?author=Cimrman");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Cimrman", response.getBody().asString());
    }

    @Test
    public void deepestLevelDirectly() {
        Response response = app.given()
                .when()
                .get("/books/author/profession/wage/currency/name");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("USD", response.getBody().asString());
    }

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/25028")
    public void subResource() {
        Response response = app.given().get("/client/book/author/?author=Heller");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Heller", response.getBody().asString());

        Response sub = app.given().get("/client/book/profession");
        assertEquals(HttpStatus.SC_OK, sub.statusCode());
        assertEquals("writer", sub.getBody().asString());
    }

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/25028")
    public void deepLevel() {
        Response response = app.given().get("/client/book/currency");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Heller", response.getBody().asString());
    }
}
