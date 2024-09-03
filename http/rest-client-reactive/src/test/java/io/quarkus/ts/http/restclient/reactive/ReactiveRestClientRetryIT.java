package io.quarkus.ts.http.restclient.reactive;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.Options.ChunkedEncodingPolicy.NEVER;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;

/**
 * Verifies SmallRye fault-tolerance retry logic and header propagation between the client and server.
 */
@QuarkusScenario
public class ReactiveRestClientRetryIT {

    private static final WireMockServer mockServer;
    private final int headerId = 121;

    static {
        mockServer = new WireMockServer(WireMockConfiguration.options()
                .dynamicPort()
                .useChunkedTransferEncoding(NEVER));
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/client/async"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        mockServer.start();
    }

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("client.endpoint/mp-rest/url", mockServer::baseUrl);

    @Test
    @Tag("QUARKUS-4477")
    // Commit that fixes the issue
    // https://github.com/quarkusio/quarkus/pull/39988/commits/b9cc3c2dc65a6f61641c83a940e13c116ce6cd0c
    void shouldPerformRetryOfFailingBlockingClientCall() {
        app.given().header("REQUEST_ID", headerId)
                .get("/server/async")
                .then()
                .statusCode(500);

        // Check number of server events, one failing call plus 3 retries by default
        Assertions.assertEquals(4, mockServer.getServeEvents().getServeEvents().stream().count());

        List<Map<String, String>> headers = app.given()
                .get("/fault/headers")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .as(new TypeRef<>() {
                });

        // Check if REQUEST_ID header was propagated and stored
        Assertions.assertTrue(headers.stream().anyMatch(header -> header.containsKey("REQUEST_ID")
                && headerId == (Integer.parseInt(header.get("REQUEST_ID")))));
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }
}
