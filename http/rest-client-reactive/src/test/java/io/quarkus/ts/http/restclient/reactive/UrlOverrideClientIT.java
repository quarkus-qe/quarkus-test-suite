package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class UrlOverrideClientIT {
    /*
     * Start two application. By default, restClient in app will do HTTP requests against app.
     * But if we override the URL, it will do requests against app2.
     * Both apps will be distinguished by value of "ts.quarkus.urlOverride.response" which they return as HTTP response.
     * The URL override feature overrides base URL, but not HTTP path. So we cannot easily test it using just one app.
     */
    @QuarkusApplication()
    static RestService app = new RestService()
            .withProperties("urlOverride.properties");

    @QuarkusApplication()
    static RestService app2 = new RestService()
            .withProperties("urlOverride.properties")
            .withProperty("quarkus.http.ssl-port", "8444")
            .withProperty("ts.quarkus.urlOverride.response", "overridden");

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

    private void testEndpoints(String defaultEndpoint, String overrideEndpoint) {
        String defaultResponse = app.given().get("/testUrlOverride/" + defaultEndpoint).asString();
        assertEquals("default", defaultResponse);

        String overriddenResponse = app.given()
                .get("/testUrlOverride/" + overrideEndpoint + "/?port=" + app2.getURI(Protocol.HTTP).getPort())
                .body().asString();
        assertEquals("overridden", overriddenResponse);
    }
}
