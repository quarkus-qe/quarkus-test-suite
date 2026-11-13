package io.quarkus.ts.opentelemetry;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.junit5.VertxExtension;

/**
 * {@link OpenTelemetryMetricsGrpcExportRecoveryIT} proves that a collector which goes completely
 * silent recovers: the connection eventually looks idle, {@code setReadIdleTimeout} trips, the
 * stalled export fails, and the exporter retries on the next cycle.
 * <p>
 * This test covers a different, previously unfixed case: a connection that keeps producing
 * unrelated traffic - here an HTTP/2 PING, exactly what a load balancer, sidecar proxy or NAT
 * keep-alive would send - while the specific Export stream itself never gets a response. Because
 * that traffic resets {@code setReadIdleTimeout}, the stalled export never times out, and
 * {@code PeriodicMetricReader} logs "Exporter busy. Dropping metrics." forever, with no retry and
 * no recovery. See https://github.com/quarkusio/quarkus/issues/57904.
 */
@Tag("https://github.com/quarkusio/quarkus/issues/57904")
@QuarkusScenario
@ExtendWith(VertxExtension.class)
public class OpenTelemetryMetricsGrpcExportDeadlockIT {

    private static final int METRICS_COLLECTOR_PORT = 9999;
    private static final int CONFIGURED_TIMEOUT_MS = 3000;
    private static final int EXPORT_INTERVAL_MS = 5000;
    private static final int PING_INTERVAL_MS = 500;

    private static final AtomicBoolean HANG_REQUESTS = new AtomicBoolean(false);
    private static final AtomicInteger SUCCESSFUL_EXPORTS = new AtomicInteger(0);
    private static final AtomicInteger HANGING_REQUESTS = new AtomicInteger(0);
    private static final Set<HttpConnection> CONNECTIONS = ConcurrentHashMap.newKeySet();

    @QuarkusApplication(classes = { MetricResource.class })
    static final RestService app = new RestService()
            .withProperty("quarkus.application.name", "metrics-deadlock-test")
            .withProperty("quarkus.otel.metrics.enabled", "true")
            .withProperty("quarkus.otel.exporter.otlp.metrics.endpoint", "http://localhost:" + METRICS_COLLECTOR_PORT)
            .withProperty("quarkus.otel.exporter.otlp.metrics.protocol", "grpc")
            .withProperty("quarkus.otel.metric.export.interval", EXPORT_INTERVAL_MS + "ms")
            .withProperty("quarkus.otel.exporter.otlp.metrics.timeout", CONFIGURED_TIMEOUT_MS + "ms")
            .withProperty("quarkus.otel.traces.enabled", "true")
            .withProperty("quarkus.otel.logs.enabled", "false");

    @Test
    public void testMetricsExportRecoversFromAConnectionKeptAliveWithoutARealResponse(Vertx vertx) throws Exception {
        resetState();

        try (AutoCloseable collector = createFakeOtlpCollector(vertx)) {
            generateMetrics(2);

            await().atMost(20, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertTrue(SUCCESSFUL_EXPORTS.get() > 0,
                            "Should have at least one successful export"));

            int initialExports = SUCCESSFUL_EXPORTS.get();
            Log.info("Initial successful exports: %d", initialExports);

            HANG_REQUESTS.set(true);
            generateMetrics(1);

            await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertTrue(HANGING_REQUESTS.get() > 0,
                            "Should have at least one hanging request"));

            Log.info("Hanging requests initiated: %d", HANGING_REQUESTS.get());
            // Stay "stuck" for well over one export interval, with the connection kept busy by the
            // periodic PING the whole time - this is exactly the window where a request-level
            // deadline (as opposed to a connection-level idle timeout) is required to recover.
            Thread.sleep((long) EXPORT_INTERVAL_MS * 3);

            HANG_REQUESTS.set(false);
            generateMetrics(2);

            await().atMost(2, TimeUnit.MINUTES)
                    .untilAsserted(() -> {
                        int totalExports = SUCCESSFUL_EXPORTS.get();
                        assertEquals(5, totalExports,
                                "All 5 generated metrics should be successfully exported once the exporter "
                                        + "gives up on the stalled stream and retries - without the fix for "
                                        + "https://github.com/quarkusio/quarkus/issues/57904 this never happens");
                    });

            Log.info("Test completed successfully - metrics export recovered despite the connection being kept "
                    + "busy by unrelated traffic the whole time");
        }
    }

    private static AutoCloseable createFakeOtlpCollector(Vertx vertx) {
        GrpcServer grpcServer = GrpcServer.server(vertx);
        HttpServer httpServer = vertx.createHttpServer(new HttpServerOptions().setPort(METRICS_COLLECTOR_PORT));

        httpServer.connectionHandler(connection -> {
            CONNECTIONS.add(connection);
            connection.closeHandler(v -> CONNECTIONS.remove(connection));
        });

        httpServer.requestHandler(grpcServer).listen(result -> {
            if (result.succeeded()) {
                Log.info("Mock OTLP Collector started on port %d", METRICS_COLLECTOR_PORT);
            }
        });

        grpcServer.callHandler(request -> {
            request.messageHandler(message -> {
                if (HANG_REQUESTS.get()) {
                    int hangingId = HANGING_REQUESTS.incrementAndGet();
                    Log.debug("Request %d is hanging (will never respond)", hangingId);
                } else {
                    request.response().endMessage(createValidResponse());
                    SUCCESSFUL_EXPORTS.incrementAndGet();
                    Log.debug("Request processed successfully. Total exports: %d", SUCCESSFUL_EXPORTS.get());
                }
            });
        });

        long pingTimerId = vertx.setPeriodic(PING_INTERVAL_MS, id -> {
            Buffer pingData = Buffer.buffer("keepalive");
            for (HttpConnection connection : CONNECTIONS) {
                try {
                    connection.ping(pingData);
                } catch (Exception ignored) {
                    // the connection may have closed between the snapshot and the ping.
                }
            }
        });

        return () -> {
            vertx.cancelTimer(pingTimerId);
            CONNECTIONS.clear();
            CompletableFuture<Void> closeFuture = new CompletableFuture<>();
            httpServer.close(ar -> closeFuture.complete(null));
            closeFuture.get(5, TimeUnit.SECONDS);
        };
    }

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

    private void resetState() {
        HANG_REQUESTS.set(false);
        HANGING_REQUESTS.set(0);
        SUCCESSFUL_EXPORTS.set(0);
        CONNECTIONS.clear();
    }
}
