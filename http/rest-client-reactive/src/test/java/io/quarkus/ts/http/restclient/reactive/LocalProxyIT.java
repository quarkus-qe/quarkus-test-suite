package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class LocalProxyIT {

    @DevModeQuarkusApplication(properties = "modern.properties")
    static RestService proxyApp = new RestService()
            .withProperty("quarkus.rest-client.meta-client.enable-local-proxy", "true");

    @Test
    @Tag("https://github.com/quarkusio/quarkus/pull/45161")
    void sendDirectRequest() {
        Response proxied = proxyApp.given().with().get("/meta/headers");
        assertEquals(HttpStatus.SC_OK, proxied.statusCode());
        List<String> headers = proxied.jsonPath().getList(".");
        assertNotEquals(0, headers.size(),
                "This response should contain headers: " + proxied.body().asString());
        assertEquals(0,
                headers.stream().filter(line -> line.startsWith("x-forwarded-host")).count(),
                "There should be no x-forwarded-host header in the request!");
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/pull/45161")
    void sendRequestThroughProxy() {
        Response proxied = proxyApp.given().with().get("client/meta/headers");
        assertEquals(HttpStatus.SC_OK, proxied.statusCode());
        List<String> headers = proxied.jsonPath().getList(".");
        assertNotEquals(0, headers.size(),
                "This response should contain headers: " + proxied.body().asString());
        assertEquals(1,
                headers.stream().filter(line -> line.startsWith("x-forwarded-host: [localhost:")).count(),
                "There should be x-forwarded-host header in this request!");
    }
}
