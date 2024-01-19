package io.quarkus.ts.http.restclient.reactive;

import static com.github.tomakehurst.wiremock.core.Options.ChunkedEncodingPolicy.NEVER;
import static io.quarkus.ts.http.restclient.reactive.resources.PlainBookResource.SEARCH_TERM_VAL;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.restclient.reactive.json.Book;
import io.quarkus.ts.http.restclient.reactive.json.BookRepository;
import io.restassured.response.Response;

@QuarkusScenario
public class ReactiveRestClientIT {

    private static final String HEMINGWAY_BOOKS = "In Our Time, The Sun Also Rises, A Farewell to Arms, The Old Man and the Sea";
    private static WireMockServer mockServer;

    static {
        mockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort()
                .useChunkedTransferEncoding(NEVER));
        mockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/malformed/"))
                .willReturn(WireMock.aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        mockServer.start();
    }

    static final String MALFORMED_URL = "quarkus.rest-client.\"io.quarkus.ts.http.restclient.reactive.MalformedClient\".url";
    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("modern.properties")
            .withProperty(MALFORMED_URL, () -> mockServer.baseUrl());

    @Test
    public void shouldGetBookFromRestClientJson() {
        Response response = app.given().with().pathParam("id", "123")
                .get("/client/book/{id}/json");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Title in Json: 123", response.jsonPath().getString("title"));
    }

    @Tag("QUARKUS-1568")
    @Test
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

    @Tag("QUARKUS-2148")
    @Test
    public void restQueryParam() {
        Response response = app.given().when().get("/client/book/rest-query");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        var books = response.jsonPath().getList(".", Book.class);
        assertEquals(BookRepository.count(), books.size());
        for (int i = 0; i < books.size(); i++) {
            var expectedBook = BookRepository.getById(i + 1);
            var actualBook = books.get(i);
            assertEquals(expectedBook.getTitle(), actualBook.getTitle());
            assertEquals(expectedBook.getAuthor(), actualBook.getAuthor());
        }
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

    @Test
    public void decodedRequestPath() {
        Response response = app.given().given().queryParam("searchTerm", SEARCH_TERM_VAL)
                .get("/client/book/quick-search/decoded");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals(HEMINGWAY_BOOKS, response.getBody().asString());
    }

    @Test
    public void encodedRequestPath() {
        Response response = app.given().given().queryParam("searchTerm", SEARCH_TERM_VAL)
                .get("/client/book/quick-search/encoded");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals(HEMINGWAY_BOOKS, response.getBody().asString());
    }

    /**
     * Test class annotated with {@link jakarta.ws.rs.Path} and registered as client via
     * {@link org.eclipse.microprofile.rest.client.inject.RegisterRestClient} must not be included in OpenAPI Document.
     */
    @Test
    public void restClientIsNotIncludedInOpenApiDocument() {
        // Path '/books/author/profession/name' is unique to AuthorClient#getProfession() and should not be part of OpenAPI document
        app.given().get("/q/openapi?format=json").then().body("paths.\"/books/author/profession/name\"", nullValue());
    }

    @Test
    @Tag("QUARKUS-2098")
    public void checkMediaTypeWithSuffix() {
        Response complete = app.given().get("/client/book/suffix/complete/?content=Hello");
        assertEquals(HttpStatus.SC_OK, complete.statusCode());
        assertEquals("Hello_text+json", complete.getBody().asString());
    }

    @Test
    @Tag("QUARKUS-2098")
    public void checkDifferentMediaType() {
        Response other = app.given().get("/client/book/suffix/other/?content=Hello");
        assertEquals(HttpStatus.SC_OK, other.statusCode());
        assertEquals("Hello_other", other.getBody().asString());
    }

    @Test
    @Tag("QUARKUS-2098")
    public void checkSuffixOnly() {
        Response subtype = app.given().get("/client/book/suffix/subtype/?content=Heller");
        assertEquals(HttpStatus.SC_OK, subtype.statusCode());
        assertEquals("Heller_text", subtype.getBody().asString());
    }

    @Test
    @Tag("QUARKUS-2098")
    public void checkSuffixesOnly() {
        Response suffix = app.given().get("/client/book/suffix/suffix/?content=Heller");
        assertEquals(HttpStatus.SC_OK, suffix.statusCode());
        assertEquals("Heller_json", suffix.getBody().asString());
    }

    @Test
    @Tag("QUARKUS-2098")
    public void checkSuffixPriority() {
        Response suffix = app.given().get("/client/book/suffix/priority/?content=Heller");
        assertEquals(HttpStatus.SC_OK, suffix.statusCode());
        assertEquals("Heller_text", suffix.getBody().asString());
    }

    @Tag("QUARKUS-2741")
    @Test
    public void checkProcessPathBeforeSubResources() {
        final String randomId = UUID.randomUUID().toString();
        String result = app.given().get("clients/myRealm/clientResource/" + randomId).then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertEquals("/clients/myRealm/resource-server/" + randomId, result);
    }

    @Tag("QUARKUS-2741")
    @Test
    public void checkProcessPathBeforeSubResourcesManualRestClientBuild() {
        final String randomId = UUID.randomUUID().toString();
        String result = app.given()
                .get("clients/myRealm/clientResource/" + randomId + "/?baseUri=" + app.getURI(Protocol.HTTP).toString())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertEquals("/clients/myRealm/resource-server/" + randomId, result);
    }

    @Tag("QUARKUS-3170")
    @Test
    public void programmaticRestClient() {
        app
                .given()
                .get("/books/programmatic-way")
                .then()
                .statusCode(200)
                .body(is("The Hobbit: An Unexpected Journey"));
    }

    @Test
    @DisabledOnQuarkusVersion(version = "3\\.2\\.(6|7).*", reason = "Fixed in 3.2.8")
    public void malformedChunk() {
        Response response = app.given().get("/client/malformed");
        Assertions.assertEquals("io.vertx.core.http.HttpClosedException", response.body().asString());
    }

    @Test
    @EnabledOnNative
    @Tag("https://github.com/quarkusio/quarkus/issues/36986")
    public void sseIndexMethodOnNativeTest() {
        app.given().get("/sse/client")
                .then()
                .statusCode(200)
                .body(containsString("random SSE data"));
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/pull/37268")
    public void clientHeaderInjectionTest() {
        app.given().get("/headers")
                .then()
                .body(containsString("clientFilterInvoked"));
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }
}
