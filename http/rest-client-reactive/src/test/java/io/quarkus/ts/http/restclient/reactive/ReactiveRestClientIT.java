package io.quarkus.ts.http.restclient.reactive;

import static io.quarkus.ts.http.restclient.reactive.PlainBookResource.SEARCH_TERM_VAL;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class ReactiveRestClientIT {

    private static final String HEMINGWAY_BOOKS = "In Our Time, The Sun Also Rises, A Farewell to Arms, The Old Man and the Sea";
    private static final String DISABLE_IF_NOT_QUARKUS_2_7_6_OR_2_8_3_OR_HIGHER = "(2\\.[0-6]\\..*)|(2\\.7\\.[0-5]\\..*)|(2\\.8\\.[0-2]\\..*)";
    private static final String FIXED_IN_2_7_6_AND_2_8_3 = "Fixed in Quarkus 2.8.3.Final and 2.7.6.Final";

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
    public void subResource() {
        Response response = app.given().get("/client/book/author/?author=Heller");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Heller", response.getBody().asString());

        Response sub = app.given().get("/client/book/profession");
        assertEquals(HttpStatus.SC_OK, sub.statusCode());
        assertEquals("writer", sub.getBody().asString());
    }

    @Test
    public void deepLevel() {
        Response response = app.given().get("/client/book/currency");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("USD", response.getBody().asString());
    }

    @DisabledOnQuarkusVersion(version = DISABLE_IF_NOT_QUARKUS_2_7_6_OR_2_8_3_OR_HIGHER, reason = FIXED_IN_2_7_6_AND_2_8_3)
    @Test
    public void decodedRequestPath() {
        Response response = app.given().given().queryParam("searchTerm", SEARCH_TERM_VAL)
                .get("/client/book/quick-search/decoded");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals(HEMINGWAY_BOOKS, response.getBody().asString());
    }

    @DisabledOnQuarkusVersion(version = DISABLE_IF_NOT_QUARKUS_2_7_6_OR_2_8_3_OR_HIGHER, reason = FIXED_IN_2_7_6_AND_2_8_3)
    @Test
    public void encodedRequestPath() {
        Response response = app.given().given().queryParam("searchTerm", SEARCH_TERM_VAL)
                .get("/client/book/quick-search/encoded");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals(HEMINGWAY_BOOKS, response.getBody().asString());
    }

    /**
     * Test class annotated with {@link javax.ws.rs.Path} and registered as client via
     * {@link org.eclipse.microprofile.rest.client.inject.RegisterRestClient} must not be included in OpenAPI Document.
     */
    @DisabledOnQuarkusVersion(version = "(2\\.[0-6]\\..*)|(2\\.7\\.[0-5]\\..*)", reason = "Fixed in Quarkus 2.7.6.")
    @Test
    public void restClientIsNotIncludedInOpenApiDocument() {
        // Path '/books/author/profession/name' is unique to AuthorClient#getProfession() and should not be part of OpenAPI document
        app.given().get("/q/openapi?format=json").then().body("paths.\"/books/author/profession/name\"", nullValue());
    }
}
