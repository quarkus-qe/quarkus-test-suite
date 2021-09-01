package io.quarkus.ts.jaxrs.reactive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.response.ValidatableResponse;

@QuarkusScenario
public class HttpCachingResourceIT {
    private static final String BASE_PATH = "/http-caching";

    @Test
    public void shouldGetEmptyCacheControl() {
        whenGet("/no-attributes").header(HttpHeaders.CACHE_CONTROL, is(""));
    }

    @Test
    public void shouldGetCacheControlAllAttributes() {
        whenGet("/all-attributes").header(
                HttpHeaders.CACHE_CONTROL,
                allOf(
                        List.of(containsString("no-cache"),
                                containsString("must-revalidate"),
                                containsString("no-transform"),
                                containsString("no-store"),
                                containsString("proxy-revalidate"),
                                containsString("s-maxage=1"),
                                containsString("max-age=1"),
                                containsString("private"))));
    }

    // TODO: https://github.com/quarkusio/quarkus/issues/19822
    @Disabled
    @Test
    public void shouldGetNoCacheUnqualified() {
        whenGet("/nocache-unqualified").header(HttpHeaders.CACHE_CONTROL, is("no-cache"));
    }

    @Test
    public void shouldGetNoCacheQualified() {
        whenGet("/nocache-qualified").header(HttpHeaders.CACHE_CONTROL, is("no-cache=\"field1\""));
    }

    private ValidatableResponse whenGet(String path) {
        return given()
                .get(BASE_PATH + path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(HttpCachingResource.RESPONSE));
    }
}
