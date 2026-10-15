package io.quarkus.ts.opentelemetry;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Closeable;
import java.io.IOException;
import java.util.Base64;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.URILike;
import io.quarkus.test.utils.AwaitilityUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.junit5.VertxExtension;

@Tag("QUARKUS-4550")
@ExtendWith(VertxExtension.class)
@QuarkusScenario
public class OpenTelemetryProxyIT {

    private static final int PAGE_LIMIT = 10;
    private static final String OPERATION_NAME = "GET /hello";
    private static final String PROXY_AUTHORIZATION = "proxy-authorization";
    private static final String BASIC_AUTH_PREFIX = "Basic ";
    private static final String PROXY_USERNAME = "otel-user";
    private static final String PROXY_PASSWORD = "otel-pwd";
    private static final String JAEGER_MISSING_COLLECTOR = "missing.collector";
    private static final int RANDOM_PORT_INCREMENT = 156;

    @JaegerContainer
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.enabled", "true")
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.username", PROXY_USERNAME)
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.password", PROXY_PASSWORD)
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.port", OpenTelemetryProxyIT::getProxyPortAsString)
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.host", "localhost")
            .withProperty("quarkus.otel.exporter.otlp.endpoint", "http://" + JAEGER_MISSING_COLLECTOR)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d");

    @Test
    public void testProxyWithPassword(Vertx vertx) throws IOException {
        try (var ignored = createHttpProxy(vertx)) {
            testTraces();
        }
    }

    private static Closeable createHttpProxy(Vertx vertx) {
        HttpClient proxyClient = vertx.createHttpClient();

        HttpServer proxyHttpServer = vertx.createHttpServer(new HttpServerOptions().setPort(getProxyPort()));
        Log.info("Starting HTTP proxy on port %s for Jaeger collector %s:%s",
                getProxyPort(), getJaegerUri().getHost(), getJaegerUri().getPort());

        proxyHttpServer.requestHandler(proxyReq -> {
            assertEquals(JAEGER_MISSING_COLLECTOR, proxyReq.authority().host());
            assertProxyUsernameAndPassword(proxyReq.getHeader(PROXY_AUTHORIZATION));

            proxyReq.body().onSuccess(body -> proxyClient.request(proxyReq.method(),
                    getJaegerUri().getPort(), getJaegerUri().getHost(), proxyReq.uri())
                    .onSuccess(clientReq -> {
                        proxyReq.headers().forEach(h -> clientReq.putHeader(h.getKey(), h.getValue()));
                        clientReq.putHeader("Host", getJaegerUri().getHost() + ":" + getJaegerUri().getPort());
                        clientReq.send(body).onSuccess(clientResp -> {
                            proxyReq.response().setStatusCode(clientResp.statusCode());
                            clientResp.headers().forEach(h -> proxyReq.response().putHeader(h.getKey(), h.getValue()));
                            clientResp.body().onSuccess(respBody -> proxyReq.response().end(respBody));
                        }).onFailure(err -> proxyReq.response().setStatusCode(502).end(err.getMessage()));
                    }).onFailure(err -> proxyReq.response().setStatusCode(502).end(err.getMessage())));
        });

        proxyHttpServer.listen();

        return () -> proxyHttpServer.close().toCompletionStage().toCompletableFuture().join();
    }

    private static void assertProxyUsernameAndPassword(String proxyAuthZ) {
        if (proxyAuthZ != null && proxyAuthZ.startsWith(BASIC_AUTH_PREFIX)) {
            var basicCredentials = new String(Base64.getDecoder().decode(proxyAuthZ.substring(BASIC_AUTH_PREFIX.length())));
            assertEquals(PROXY_USERNAME + ":" + PROXY_PASSWORD, basicCredentials);
            return;
        }
        Assertions.fail(
                "OpenTelemetry OTLP exporter is not passing proxy authorization, found header: " + proxyAuthZ);
    }

    private static void testTraces() {
        doRequest();
        assertTraces();
    }

    private static void assertTraces() {
        AwaitilityUtils.untilAsserted(() -> thenRetrieveTraces()
                .then()
                .statusCode(200)
                .body("data.spans.flatten().findAll { it.operationName == '%s' }.size()".formatted(OPERATION_NAME),
                        greaterThan(0)));
    }

    private static Response thenRetrieveTraces() {
        return RestAssured
                .given()
                .queryParam("operation", OPERATION_NAME)
                .queryParam("lookback", "1h")
                .queryParam("limit", PAGE_LIMIT)
                .queryParam("service", "pingpong")
                .get(jaeger.getTraceUrl());
    }

    private static void doRequest() {
        app.given()
                .get("/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("pong"));
    }

    private static URILike getJaegerUri() {
        return jaeger.getURI(Protocol.NONE);
    }

    private static int getProxyPort() {
        return getJaegerUri().getPort() + RANDOM_PORT_INCREMENT;
    }

    private static String getProxyPortAsString() {
        return Integer.toString(getProxyPort());
    }
}
