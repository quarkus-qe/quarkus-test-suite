package io.quarkus.ts.http.restclient.reactive;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.restclient.reactive.exceptions.MyCheckedException;
import io.quarkus.ts.http.restclient.reactive.exceptions.MyClientExceptionMapper;
import io.quarkus.ts.http.restclient.reactive.fault.tolerance.FaultToleranceExceptionMappingClient;
import io.quarkus.ts.http.restclient.reactive.resources.FaultToleranceExceptionMappingResource;

@Tag("https://github.com/quarkusio/quarkus/issues/48286")
@QuarkusScenario
public class CheckedExceptionWithInterceptorIT {

    private static WireMockServer wireMockServer;

    static {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
    }

    @QuarkusApplication(classes = { MyCheckedException.class,
            MyClientExceptionMapper.class,
            FaultToleranceExceptionMappingClient.class,
            FaultToleranceExceptionMappingResource.class })
    static RestService app = new RestService()
            .withProperty("quarkus.rest-client.checked-exception-client.url", () -> wireMockServer.baseUrl());

    @BeforeAll
    static void setup() {
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/service-unavailable"))
                .willReturn(aResponse().withStatus(HttpStatus.SC_SERVICE_UNAVAILABLE)));
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testCheckedExceptionIsNotWrapped() {
        app.given()
                .get("/test-fault-tolerance/trigger-exception")
                .then()
                .statusCode(500);

        // The initial call fails and is mapped to MyCheckedException, then a second call is made due to @Retry(maxRetries = 1).
        assertEquals(2, wireMockServer.getServeEvents().getServeEvents().size());

        assertTrue(app.getLogs().stream()
                .anyMatch(log -> log.contains("MyCheckedException")),
                "Should see MyCheckedException in logs");

        assertTrue(app.getLogs().stream()
                .noneMatch(log -> log.contains("ArcUndeclaredThrowableException")),
                "Should NOT see ArcUndeclaredThrowableException in logs");
    }
}
