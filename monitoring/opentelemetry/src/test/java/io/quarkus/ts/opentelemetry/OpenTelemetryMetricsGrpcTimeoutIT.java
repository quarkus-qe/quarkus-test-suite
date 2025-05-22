package io.quarkus.ts.opentelemetry;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.junit5.VertxExtension;

@Tag("https://github.com/quarkusio/quarkus/pull/47245")
@QuarkusScenario
@ExtendWith(VertxExtension.class)
public class OpenTelemetryMetricsGrpcTimeoutIT {

    private static final Logger LOG = Logger.getLogger(OpenTelemetryMetricsGrpcTimeoutIT.class);
    private static final long SLOW_RESPONSE_DELAY_MS = 5000L;
    private static final int METRICS_COLLECTOR_PORT = 9998;

    private static final AtomicBoolean SIMULATE_SLOW_NETWORK = new AtomicBoolean(false);
    private static final AtomicInteger SUCCESSFUL_EXPORTS = new AtomicInteger(0);
    private static final AtomicInteger DELAYED_RESPONSES = new AtomicInteger(0);

    @QuarkusApplication(classes = { MetricResource.class })
    static final RestService app = new RestService()
            .withProperty("quarkus.application.name", "metrics-timeout-test")
            .withProperty("quarkus.otel.metrics.enabled", "true")
            .withProperty("quarkus.otel.exporter.otlp.metrics.endpoint", "http://localhost:" + METRICS_COLLECTOR_PORT)
            .withProperty("quarkus.otel.exporter.otlp.metrics.protocol", "grpc")
            .withProperty("quarkus.otel.metric.export.interval", "5s")
            .withProperty("quarkus.otel.exporter.otlp.metrics.timeout", "3s")
            .withProperty("quarkus.otel.traces.enabled", "false")
            .withProperty("quarkus.otel.logs.enabled", "false");

    @Test
    public void testMetricsExporterRecoversFromSlowNetwork(Vertx vertx) throws Exception {
        resetCounters();

        try (AutoCloseable collector = createFakeOtlpCollector(vertx)) {
            generateMetrics(5);
            await().atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertTrue(SUCCESSFUL_EXPORTS.get() > 0, "Normal metrics export should work"));

            int baselineExports = SUCCESSFUL_EXPORTS.get();

            // Simulate slow network
            SIMULATE_SLOW_NETWORK.set(true);
            generateMetrics(5);
            Thread.sleep(8000);
            assertTrue(DELAYED_RESPONSES.get() > 0, "Some responses should have been delayed");

            // Verify recovery - for validates the timeout fix
            SIMULATE_SLOW_NETWORK.set(false);
            generateMetrics(5);

            await().atMost(60, TimeUnit.SECONDS).untilAsserted(() -> {
                int currentExports = SUCCESSFUL_EXPORTS.get();
                assertTrue(currentExports > baselineExports,
                        "Metrics export should recover after network issues");
            });

            LOG.info("Test PASSED! Metrics exporter recovered successfully.");
        }
    }

    private static AutoCloseable createFakeOtlpCollector(Vertx vertx) {
        GrpcServer grpcServer = GrpcServer.server(vertx);
        HttpServer httpServer = vertx.createHttpServer(new HttpServerOptions().setPort(METRICS_COLLECTOR_PORT));

        httpServer.requestHandler(grpcServer).listen(result -> {
            if (result.succeeded()) {
                LOG.infof("Mock OTLP Collector started on port %d", METRICS_COLLECTOR_PORT);
            }
        });

        grpcServer.callHandler(request -> {
            request.messageHandler(message -> {
                if (SIMULATE_SLOW_NETWORK.get()) {
                    DELAYED_RESPONSES.incrementAndGet();
                    vertx.setTimer(SLOW_RESPONSE_DELAY_MS, timerId -> sendResponseSafely(request, false));
                } else {
                    sendResponseSafely(request, true);
                }
            });
        });

        return () -> {
            CompletableFuture<Void> closeFuture = new CompletableFuture<>();
            httpServer.close(ar -> closeFuture.complete(null));
            closeFuture.get(5, TimeUnit.SECONDS);
        };
    }

    private static void sendResponseSafely(GrpcServerRequest request, boolean countSuccess) {
        request.response().endMessage(createValidResponse());
        if (countSuccess) {
            SUCCESSFUL_EXPORTS.incrementAndGet();
        }
    }

    /**
     * Creates a valid empty OTLP response indicating successful processing.
     */
    private static GrpcMessage createValidResponse() {
        return GrpcMessage.message("identity", Buffer.buffer());
    }

    private void generateMetrics(int count) {
        for (int i = 0; i < count; i++) {
            app.given().get("/test-metrics").then().statusCode(HttpStatus.SC_OK);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void resetCounters() {
        SIMULATE_SLOW_NETWORK.set(false);
        SUCCESSFUL_EXPORTS.set(0);
        DELAYED_RESPONSES.set(0);
    }
}
