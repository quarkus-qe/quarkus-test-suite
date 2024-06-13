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
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.URILike;
import io.quarkus.test.utils.AwaitilityUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;
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

    @JaegerContainer
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.enabled", "true")
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.username", PROXY_USERNAME)
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.password", PROXY_PASSWORD)
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.port", OpenTelemetryProxyIT::getProxyPortAsString)
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.host", "localhost")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", "http://" + JAEGER_MISSING_COLLECTOR);

    @Test
    public void testProxyWithPassword(Vertx vertx) throws IOException {
        try (var ignored = createGrpcProxy(vertx)) {
            testTraces();
        }
    }

    private static Closeable createGrpcProxy(Vertx vertx) {
        GrpcServer grpcProxy = GrpcServer.server(vertx);

        HttpServer proxyHttpServer = vertx.createHttpServer(new HttpServerOptions().setPort(getProxyPort()));
        proxyHttpServer
                .requestHandler(httpServerRequest -> {
                    assertEquals(JAEGER_MISSING_COLLECTOR, httpServerRequest.authority().host());
                    grpcProxy.handle(httpServerRequest);
                })
                .listen();

        GrpcClient proxyClient = GrpcClient.client(vertx);

        // create proxy server
        // on request use gRPC client and pass the message to the Jaeger
        grpcProxy.callHandler(reqFromQuarkus -> reqFromQuarkus.messageHandler(msgFromQuarkus -> proxyClient
                .request(getJaegerSocketAddress())
                .onSuccess(requestToJaeger -> {
                    assertProxyUsernameAndPassword(reqFromQuarkus);
                    requestToJaeger
                            .methodName(reqFromQuarkus.methodName())
                            .serviceName(reqFromQuarkus.serviceName())
                            .end(msgFromQuarkus.payload());
                    // send Jaeger response back to the Quarkus application
                    requestToJaeger.response()
                            .onSuccess(h -> h.messageHandler(msg -> reqFromQuarkus.response().endMessage(msg)))
                            .onFailure(err -> reqFromQuarkus.response().status(GrpcStatus.ABORTED).end());
                })));
        return () -> proxyHttpServer
                .close()
                .eventually(proxyClient::close)
                .toCompletionStage()
                .toCompletableFuture()
                .join();
    }

    private static void assertProxyUsernameAndPassword(GrpcServerRequest<Buffer, Buffer> reqFromQuarkus) {
        var proxyAuthZ = reqFromQuarkus.headers().get(PROXY_AUTHORIZATION);
        if (proxyAuthZ != null && proxyAuthZ.startsWith(BASIC_AUTH_PREFIX)) {
            var basicCredentials = new String(Base64.getDecoder().decode(proxyAuthZ.substring(BASIC_AUTH_PREFIX.length())));
            assertEquals(PROXY_USERNAME + ":" + PROXY_PASSWORD, basicCredentials);
            return;
        }
        Assertions.fail(
                "OpenTelemetry OTLP exporter is not passing proxy authorization, found headers: " + reqFromQuarkus.headers());
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
        return getJaegerUri().getPort() + 1;
    }

    private static String getProxyPortAsString() {
        return Integer.toString(getProxyPort());
    }

    private static SocketAddress getJaegerSocketAddress() {
        return SocketAddress.inetSocketAddress(getJaegerUri().getPort(), getJaegerUri().getHost());
    }
}
