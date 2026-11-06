package io.quarkus.ts.mcp;

import java.time.Duration;
import java.util.List;

import org.awaitility.Awaitility;

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
        return open(client, request, () -> {
            JsonObject cancelled = client.newMessage("notifications/cancelled")
                    .put("params", new JsonObject().put("requestId", request.getValue("id")).put("reason", "Test completed"));
            McpAssured.injectStatelessMeta(cancelled);
            client.sendAndForget(cancelled);
        });
    }

    static SubscriptionObserver openHttp(McpAssured.McpTestClient<?, ?> client, JsonObject request) {
        try {
            return open(client, request, client::close);
        } catch (RuntimeException | Error e) {
            client.close();
            throw e;
        }
    }

    private static SubscriptionObserver open(McpAssured.McpTestClient<?, ?> client, JsonObject request, Runnable close) {
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
                close.run();
            }
        };
    }
}
