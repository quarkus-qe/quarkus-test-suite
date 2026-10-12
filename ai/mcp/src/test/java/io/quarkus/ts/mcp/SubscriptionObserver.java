package io.quarkus.ts.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;

import io.quarkiverse.mcp.server.test.McpAssured;
import io.vertx.core.json.JsonObject;

interface SubscriptionObserver extends AutoCloseable {
    Object id();

    List<JsonObject> notifications();

    default JsonObject await(int count) {
        Awaitility.await().atMost(Duration.ofSeconds(10)).until(() -> notifications().size() >= count);
        return notifications().get(count - 1);
    }

    @Override
    void close();

    static SubscriptionObserver open(McpAssured.McpTestClient<?, ?> client, JsonObject request) {
        client.sendAndForget(request);
        return new SubscriptionObserver() {
            @Override
            public Object id() {
                return request.getValue("id");
            }

            @Override
            public List<JsonObject> notifications() {
                return client.snapshot().notifications().stream().filter(notification -> {
                    JsonObject params = notification.getJsonObject("params", new JsonObject());
                    JsonObject meta = params.getJsonObject("_meta", new JsonObject());
                    return id().equals(meta.getValue("io.modelcontextprotocol/subscriptionId"));
                }).toList();
            }

            @Override
            public void close() {
                JsonObject cancelled = client.newMessage("notifications/cancelled")
                        .put("params", new JsonObject().put("requestId", id()).put("reason", "Test completed"));
                McpAssured.injectStatelessMeta(cancelled);
                client.sendAndForget(cancelled);
            }
        };
    }

    // McpAssured's installed HTTP disconnect API does not close a subscription stream.
    // Own the HTTP body explicitly so close() releases the server-side subscription.
    // https://github.com/quarkiverse/quarkus-mcp-server/issues/1082
    // Once fixed, replace HttpSubscriptionObserver with McpAssured stream handling,
    // preserving HTTP cancellation by stream closure rather than notifications/cancelled.
    static SubscriptionObserver openHttp(URI endpoint, JsonObject request) {
        return new HttpSubscriptionObserver(endpoint, request);
    }
}

final class HttpSubscriptionObserver implements SubscriptionObserver {
    private final HttpClient http = HttpClient.newHttpClient();
    private final ExecutorService reader = Executors.newSingleThreadExecutor();
    private final List<JsonObject> received = new CopyOnWriteArrayList<>();
    private final AtomicReference<Throwable> failure = new AtomicReference<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Object subscriptionId;
    private final InputStream body;

    HttpSubscriptionObserver(URI endpoint, JsonObject request) {
        subscriptionId = request.getValue("id");
        try {
            HttpResponse<InputStream> response = http.send(HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json,text/event-stream")
                    .header("MCP-Protocol-Version", StatelessMCPIT.REVISION)
                    .header("Mcp-Method", "subscriptions/listen")
                    .POST(HttpRequest.BodyPublishers.ofString(request.encode())).build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            body = response.body();
            if (response.statusCode() != 200
                    || !response.headers().firstValue("Content-Type").orElse("").startsWith("text/event-stream")) {
                String error = new String(body.readAllBytes(), StandardCharsets.UTF_8);
                body.close();
                throw new IllegalStateException("Subscription rejected: " + response.statusCode() + " " + error);
            }
        } catch (IOException | InterruptedException | RuntimeException e) {
            reader.shutdownNow();
            closeHttpClient();
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Could not open HTTP subscription", e);
        }
        reader.submit(() -> {
            try (var lines = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
                String line;
                while ((line = lines.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        received.add(new JsonObject(line.substring(5).trim()));
                    }
                }
            } catch (Throwable e) {
                if (!closed.get()) {
                    failure.set(e);
                }
            }
        });
    }

    @Override
    public Object id() {
        return subscriptionId;
    }

    @Override
    public List<JsonObject> notifications() {
        Assertions.assertNull(failure.get(), () -> "Subscription reader failed: " + failure.get());
        return List.copyOf(received);
    }

    @Override
    public void close() {
        closed.set(true);
        try {
            body.close();
        } catch (IOException e) {
            throw new IllegalStateException("Could not close HTTP subscription", e);
        } finally {
            reader.shutdownNow();
            closeHttpClient();
        }
    }

    private void closeHttpClient() {
        if (http instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new IllegalStateException("Could not close the HTTP client", e);
            }
        }
    }

}
