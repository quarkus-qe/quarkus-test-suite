package io.quarkus.ts.mcp.app;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.inject.Singleton;

import io.quarkiverse.mcp.server.McpConnection;
import io.quarkiverse.mcp.server.McpTrafficListener;
import io.quarkiverse.mcp.server.RawMessage;
import io.vertx.core.json.JsonObject;

// Capture actual protocol facts through the public traffic API instead of enabling full client wire logs.
@Singleton
public class ProtocolObserver implements McpTrafficListener {
    public static final String STDERR_PREFIX = "QE_MCP_PROTOCOL ";

    private final Set<String> clients = ConcurrentHashMap.newKeySet();
    private final List<JsonObject> observations = new CopyOnWriteArrayList<>();

    @Override
    public void onMessageReceived(RawMessage raw, McpConnection connection) {
        JsonObject message = raw.asJsonObject();
        JsonObject params = message.getJsonObject("params", new JsonObject());
        JsonObject meta = params.getJsonObject("_meta", new JsonObject());
        JsonObject identity = params.getJsonObject("clientInfo", meta.getJsonObject("io.modelcontextprotocol/clientInfo",
                new JsonObject()));
        // Observe the integration client, so direct McpAssured/SDK negative tests cannot satisfy or pollute these checks.
        if ("langchain4j".equals(identity.getString("name"))) {
            clients.add(connection.id());
        }
        if (!clients.contains(connection.id()) || !message.containsKey("method") || !message.containsKey("id")) {
            return;
        }
        record(new JsonObject().put("direction", "received").put("connection", connection.id())
                .put("method", message.getString("method"))
                .put("revision", "initialize".equals(message.getString("method")) ? params.getString("protocolVersion")
                        : meta.getString("io.modelcontextprotocol/protocolVersion"))
                .put("capabilities", meta.getJsonObject("io.modelcontextprotocol/clientCapabilities") != null));
    }

    @Override
    public void onMessageSent(RawMessage raw, McpConnection connection) {
        JsonObject result = raw.asJsonObject().getJsonObject("result", new JsonObject());
        if (clients.contains(connection.id()) && result.containsKey("protocolVersion")) {
            record(new JsonObject().put("direction", "sent").put("connection", connection.id())
                    .put("method", "initialize").put("revision", result.getString("protocolVersion")));
        }
    }

    private void record(JsonObject observation) {
        observations.add(observation);
        // A stdio child has no REST endpoint; forward only these small facts on stderr, leaving stdout for MCP.
        if (Boolean.getBoolean("qe.mcp.protocol-observer.stderr")) {
            System.err.println(STDERR_PREFIX + observation.encode());
        }
    }

    public List<JsonObject> observations() {
        return List.copyOf(observations);
    }
}
