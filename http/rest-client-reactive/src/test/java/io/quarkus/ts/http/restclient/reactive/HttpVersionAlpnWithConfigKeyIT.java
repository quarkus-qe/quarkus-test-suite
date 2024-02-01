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
public class HttpVersionAlpnWithConfigKeyIT {

    private static final String SUCCESSFUL_RESPONSE_STRING = "Hello " + NAME + " and your using http protocol in version "
            + HttpVersion.HTTP_2.name();

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService()
            .withProperties("httpVersion.properties")
            .withProperty("quarkus.rest-client.http-client-with-config-key.alpn", "true")
            .withProperty("quarkus.rest-client.https-client-with-config-key.alpn", "true");

    @Test
    public void testSyncResponseWithSingleClientOnGlobalClient() {
        app.given()
                .get("/http2/https-synchronous")
                .then()
                .statusCode(200)
                .body(containsString(WRONG_HTTP_VERSION));
    }

    @Test
    public void testAsyncResponseWithSingleClientOnGlobalClient() {
        app.given()
                .get("/http2/https-asynchronous")
                .then()
                .statusCode(200)
                .body(containsString(WRONG_HTTP_VERSION));
    }

    @Test
    public void testSyncResponseWithSingleClientOnSingleClient() {
        app.given()
                .get("/http2/https-synchronous-for-client-with-key")
                .then()
                .statusCode(200)
                .body(containsString(SUCCESSFUL_RESPONSE_STRING));
    }

    @Test
    public void testAsyncResponseWithSingleClientOnSingleClient() {
        app.given()
                .get("/http2/https-asynchronous-for-client-with-key")
                .then()
                .statusCode(200)
                .body(containsString(SUCCESSFUL_RESPONSE_STRING));
    }
}
