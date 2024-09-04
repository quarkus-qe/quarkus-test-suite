package io.quarkus.ts.http.advanced.reactive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.Header;
import io.restassured.response.ValidatableResponse;

@QuarkusScenario
public class HeadersIT {

    @QuarkusApplication(classes = { PathSpecificHeadersResource.class,
            HeadersMessageBodyWriter.class,
            CustomHeaderResponse.class,
            HeadersResource.class }, properties = "headers.properties")
    static RestService app = new RestService();

    @Test
    void testAdditionalHeaders() {
        whenGet("/headers/any")
                .header("Pragma", emptyOrNullString());
    }

    @Test
    void testAdditionalHeadersOverride() {
        whenGet("/headers/override")
                .header("foo", is("abc"))
                .header("Pragma", emptyOrNullString());
    }

    @Test
    void testAdditionalHeadersPragmaIsSent() {
        whenGet("/headers/pragma")
                .header("foo", is("bar"))
                .header("Pragma", is("no-cache"));
    }

    @Test
    @Tag("QUARKUS-2490")
    void testPathSpecificHeadersHead() {
        // HEAD requests should not include the header
        final ValidatableResponse response = given()
                .head("/filter/any")
                .then()
                .statusCode(200);
        final List<String> headerValues = response.extract().headers().getValues("Cache-Control");
        assertThat(headerValues, not(hasItem(containsString("max-age=31536000"))));
    }

    @Test
    @Tag("QUARKUS-2490")
    void testPathSpecificHeadersGet() {
        final ValidatableResponse response = whenGet("/filter/any");
        cacheControlMatches(response, "max-age=31536000");
    }

    @Test
    @Tag("QUARKUS-2490")
    void testPathSpecificAnotherPathHeadersGet() {
        final ValidatableResponse response = whenGet("/filter/another");
        cacheControlMatches(response, "max-age=31536000");
    }

    @Test
    @Tag("QUARKUS-2490")
    void testPathSpecificHeadersOverride() {
        final ValidatableResponse response = whenGet("/filter/override");
        cacheControlMatches(response, "max-age=0");
    }

    @Test
    @Tag("QUARKUS-2490")
    void testPathSpecificCacheControlIsSent() {
        final ValidatableResponse response = whenGet("/filter/no-cache");
        cacheControlMatches(response, "none");
    }

    @Test
    @Tag("QUARKUS-2490")
    void testPathSpecificHeaderRulesOrder() {
        final ValidatableResponse response = whenGet("/filter/order");
        cacheControlMatches(response, "max-age=1");
    }

    private ValidatableResponse whenGet(String path) {
        return given()
                .get(path)
                .then()
                .statusCode(200)
                .body(is("ok"));
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/pull/41411")
    void testWithNoAcceptHeader() {
        Header header = new Header("Accept", null);
        given()
                .when()
                .header(header)
                .get("/headers/no-accept")
                .then()
                .statusCode(200)
                .body(is("Headers response: ok headers"));
    }

    /**
     * Cache-Control header may be present multiple times in the response, e.g. in an OpenShift deployment. That is why we need
     * to look for a specific value among all headers of the same name, and not just match the last one of them, which is what
     * would be done by a test like this:
     *
     * <pre>
     * ...
     * given()
     *     .get(path)
     *     .then()
     *     .header("Cache-Control", is(expectedValue));
     * ...
     * </pre>
     */
    private void cacheControlMatches(ValidatableResponse response, String expectedValue) {
        final List<String> headerValues = response.extract().headers().getValues("Cache-Control");
        assertThat(headerValues, hasItem(containsString(expectedValue)));
    }
}
