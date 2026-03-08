package io.quarkus.ts.http.restclient.reactive;

import static io.quarkus.ts.http.restclient.reactive.resources.HttpVersionClientResource.NAME;
import static io.quarkus.ts.http.restclient.reactive.resources.HttpVersionClientResource.WRONG_HTTP_VERSION;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.core.http.HttpVersion;

@QuarkusScenario
public class HttpVersionWithConfigKeyIT {

    private static final String SUCCESSFUL_RESPONSE_STRING = "Hello " + NAME + " and your using http protocol in version "
            + HttpVersion.HTTP_2.name();

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService()
            .withProperties("httpVersion.properties")
            .withProperty("quarkus.rest-client.http-client-with-config-key.http2", "true")
            .withProperty("quarkus.rest-client.https-client-with-config-key.http2", "true");

    @Test
    public void testHttpSyncResponseWithSingleClientOnGlobalClient() {
        app.given()
                .get("/http2/http-synchronous")
                .then()
                .statusCode(200)
                .body(containsString(WRONG_HTTP_VERSION));
    }

    @Test
    public void testHttpsSyncResponseWithSingleClientOnGlobalClient() {
        app.given()
                .get("/http2/https-synchronous")
                .then()
                .statusCode(200)
                .body(containsString(WRONG_HTTP_VERSION));
    }

    @Test
    public void testHttpsAsyncResponseWithSingleClientOnGlobalClient() {
        app.given()
                .get("/http2/http-asynchronous")
                .then()
                .statusCode(200)
                .body(containsString(WRONG_HTTP_VERSION));
    }

    @Test
    public void testHttpAsyncResponseWithSingleClientOnGlobalClient() {
        app.given()
                .get("/http2/https-asynchronous")
                .then()
                .statusCode(200)
                .body(containsString(WRONG_HTTP_VERSION));
    }

    @Test
    public void testHttpSyncResponseWithSingleClientOnSingleClient() {
        app.given()
                .get("/http2/http-synchronous-for-client-with-key")
                .then()
                .statusCode(200)
                .body(containsString(SUCCESSFUL_RESPONSE_STRING));
    }

    @Test
    public void testHttpsSyncResponseWithSingleClientOnSingleClient() {
        app.given()
                .get("/http2/https-synchronous-for-client-with-key")
                .then()
                .statusCode(200)
                .body(containsString(SUCCESSFUL_RESPONSE_STRING));
    }

    @Test
    public void testHttpAsyncResponseWithSingleClientOnSingleClient() {
        app.given()
                .get("/http2/http-asynchronous-for-client-with-key")
                .then()
                .statusCode(200)
                .body(containsString(SUCCESSFUL_RESPONSE_STRING));
    }

    @Test
    public void testHttpsAsyncResponseWithSingleClientOnSingleClient() {
        app.given()
                .get("/http2/https-asynchronous-for-client-with-key")
                .then()
                .statusCode(200)
                .body(containsString(SUCCESSFUL_RESPONSE_STRING));
    }
}
