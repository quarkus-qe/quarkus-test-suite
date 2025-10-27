package io.quarkus.ts.vertx;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClosedException;
import io.vertx.core.http.HttpMethod;

@Tag("https://github.com/quarkusio/quarkus/issues/50336")
@QuarkusScenario
public class StreamingErrorIT {

    @QuarkusApplication
    static final RestService app = new RestService();

    private static final int ITEMS_PER_EMIT = 100;
    private static final int TOTAL_ITEMS = ITEMS_PER_EMIT * 2;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final int BYTES_PER_CHUNK = "This is one chunk of data.\n".getBytes(StandardCharsets.UTF_8).length;
    private static final long EXPECTED_BYTES_FIRST_BATCH = (long) ITEMS_PER_EMIT * BYTES_PER_CHUNK;
    private static final long EXPECTED_BYTES_TOTAL = (long) TOTAL_ITEMS * BYTES_PER_CHUNK;

    private Vertx vertx;
    private HttpClient client;

    @BeforeEach
    public void setup() {
        vertx = Vertx.vertx();
        client = vertx.createHttpClient();
    }

    @AfterEach
    public void cleanup() throws Exception {
        if (client != null) {
            client.close().toCompletionStage().toCompletableFuture().get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        }
        if (vertx != null) {
            vertx.close().toCompletionStage().toCompletableFuture().get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        }
    }

    @Test
    @DisabledOnQuarkusVersion(version = "3.27.0.*", reason = "This issue affected was fixed in 3.27.1")
    public void testFailureMidStream() {
        AtomicLong byteCount = new AtomicLong();
        CompletableFuture<Void> latch = new CompletableFuture<>();

        sendRequest("/streaming-error?fail=true", latch, b -> byteCount.addAndGet(b.length()));

        Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
            ExecutionException ex = Assertions.assertThrows(ExecutionException.class,
                    latch::get,
                    "Client should have failed as the server reset the connection");

            Assertions.assertInstanceOf(HttpClosedException.class, ex.getCause(),
                    "Expected the connection to be closed abruptly");
        });

        Assertions.assertEquals(EXPECTED_BYTES_FIRST_BATCH, byteCount.get(),
                "Should have received first batch of bytes before failure");
    }

    @Test
    public void testNoFailure() {
        AtomicLong byteCount = new AtomicLong();
        CompletableFuture<Void> latch = new CompletableFuture<>();

        sendRequest("/streaming-error", latch, b -> byteCount.addAndGet(b.length()));

        Assertions.assertTimeoutPreemptively(TIMEOUT,
                () -> latch.get(),
                "The stream should have completed successfully within the timeout");

        Assertions.assertEquals(EXPECTED_BYTES_TOTAL, byteCount.get(),
                "Should have received all bytes in a successful stream");
    }

    @Disabled("https://github.com/quarkusio/quarkus/issues/50754")
    @DisabledOnQuarkusVersion(version = "3.27.0.*", reason = "This issue affected was fixed in 3.27.1")
    @Test
    public void testStreamingOutputFailureMidStream() {
        AtomicLong byteCount = new AtomicLong();
        CompletableFuture<Void> latch = new CompletableFuture<>();

        sendRequest("/streaming-output-error?fail=true", latch, b -> byteCount.addAndGet(b.length()));

        Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
            ExecutionException ex = Assertions.assertThrows(ExecutionException.class,
                    () -> latch.get(),
                    "Client should have failed as the server reset the connection (StreamingOutput)");

            Assertions.assertInstanceOf(HttpClosedException.class, ex.getCause(),
                    "Expected the connection to be closed abruptly (StreamingOutput)");
        });

        Assertions.assertEquals(EXPECTED_BYTES_FIRST_BATCH, byteCount.get(),
                "Should have received first batch of bytes before failure (StreamingOutput)");
    }

    @Test
    public void testStreamingOutputNoFailure() {
        AtomicLong byteCount = new AtomicLong();
        CompletableFuture<Void> latch = new CompletableFuture<>();

        sendRequest("/streaming-output-error", latch, b -> byteCount.addAndGet(b.length()));

        Assertions.assertTimeoutPreemptively(TIMEOUT,
                () -> latch.get(),
                "The stream should have completed successfully (StreamingOutput)");

        Assertions.assertEquals(EXPECTED_BYTES_TOTAL, byteCount.get(),
                "Should have received all bytes (StreamingOutput)");
    }

    private void sendRequest(String requestURI, CompletableFuture<Void> latch, Consumer<Buffer> bodyConsumer) {
        int port = app.getURI().getPort();
        String host = app.getURI().getHost();

        client.request(HttpMethod.GET, port, host, requestURI)
                .onFailure(latch::completeExceptionally)
                .onSuccess(request -> {
                    request.connect()
                            .onFailure(latch::completeExceptionally)
                            .onSuccess(response -> {
                                response.handler(buffer -> {
                                    if (buffer.length() > 0) {
                                        bodyConsumer.accept(buffer);
                                    }
                                });
                                response.exceptionHandler(latch::completeExceptionally);
                                response.endHandler(v -> latch.complete(null));
                            });
                });
    }

}
