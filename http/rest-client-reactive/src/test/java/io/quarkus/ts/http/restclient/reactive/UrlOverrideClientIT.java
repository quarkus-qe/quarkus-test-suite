package io.quarkus.ts.http.restclient.reactive;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
@Tag("QUARKUS-5612")
public class UrlOverrideClientIT {
    private static final String MAIN_SERVICE_NAME = "overridingService";

    // used for jaeger filtering, to only query traces relevant for current test
    private long testStartTime = 0;

    /*
     * Start two application. By default, restClient in app will do HTTP requests against app.
     * But if we override the URL, it will do requests against app2.
     * Both apps will be distinguished by value of "ts.quarkus.urlOverride.response" which they return as HTTP response.
     * The URL override feature overrides base URL, but not HTTP path. So we cannot easily test it using just one app.
     */
    @JaegerContainer(expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication()
    static RestService app = new RestService()
            .withProperties("urlOverride.properties")
            .withProperty("quarkus.otel.enabled", "true")
            .withProperty("quarkus.otel.simple", "true")
            .withProperty("quarkus.application.name", MAIN_SERVICE_NAME)
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    @QuarkusApplication()
    static RestService app2 = new RestService()
            .withProperties("urlOverride.properties")
            .withProperty("quarkus.http.ssl-port", "8444")
            .withProperty("ts.quarkus.urlOverride.response", "overridden");

    @BeforeEach
    public void setTestStartTime() {
        // store time in same format as jaeger
        testStartTime = System.currentTimeMillis() * 1000;
    }

    @Test
    public void shouldOverrideUrlByString() {
        testEndpoints("defaultString", "overrideString");
    }

    @Test
    public void shouldOverrideUrlByUrl() {
        testEndpoints("defaultUrl", "overrideUrl");
    }

    @Test
    public void shouldOverrideUrlByUri() {
        testEndpoints("defaultUri", "overrideUri");
    }

    @Test
    public void shouldNotOverride() {
        String response = app.given().get("/testUrlOverride/notOverridden").asString();
        assertEquals("default", response);
    }

    @Test
    public void shouldFail() {
        // redirect the request to non-existing host which should result in failure to execute the erquest
        Response response = app.given().get("/testUrlOverride/overrideHost/?host=nonExistingHost");
        assertEquals(500, response.statusCode(), "Request should fail with code 500");
        app.logs().assertContains("UnknownHostException: nonExistingHost");
    }

    private void testEndpoints(String defaultEndpoint, String overrideEndpoint) {
        int secondAppPort = app2.getURI(Protocol.HTTP).getPort();

        String defaultResponse = app.given().get("/testUrlOverride/" + defaultEndpoint).asString();
        assertEquals("default", defaultResponse);

        String overriddenResponse = app.given()
                .get("/testUrlOverride/" + overrideEndpoint + "/?port=" + secondAppPort)
                .body().asString();
        assertEquals("overridden", overriddenResponse);

        verifyOtelTraces();
    }

    private void verifyOtelTraces() {
        Response traces = getOtelTraces();

        assertEquals(2, traces.jsonPath().getList("data").size(), "There should be two spans, after two requests");

        List<String> spanTargetHosts = traces.jsonPath()
                .getList("data.flatten().spans.flatten().tags.flatten().findAll{it.key == 'url.full'}.value");

        // There should be requests against both services
        List<String> expectedHosts = new ArrayList<>();
        expectedHosts.add(app.getURI(Protocol.HTTP).toString());
        expectedHosts.add(app2.getURI(Protocol.HTTP).toString());

        for (String host : expectedHosts) {
            assertTrue(spanTargetHosts.stream().anyMatch(spanTargetHost -> spanTargetHost.contains(host)),
                    "There should be a request against host " + host + " but not found");
        }
    }

    private Response getOtelTraces() {
        return given().when()
                .queryParam("service", MAIN_SERVICE_NAME)
                .queryParam("start", testStartTime)
                .get(jaeger.getTraceUrl());
    }
}
