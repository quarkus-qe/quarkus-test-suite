package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.WebSocketIT.assertText;
import static io.quarkus.ts.mcp.WebSocketIT.call;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkiverse.mcp.server.ClientCapability;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Runs the shared LangChain4j tests with MCP 2026-07-28 and checks basic server
 * operations directly with McpAssured across HTTP, WebSocket and stdio.
 */
public abstract class StatelessMCPIT extends BasicMCPIT {
    static final String REVISION = "2026-07-28";

    static final List<String> ADVERTISED_REVISIONS = List.of("2026-07-28", "2025-11-25", "2025-06-18", "2025-03-26",
            "2024-11-05");
    static final JsonObject CAPABILITIES = new JsonObject()
            .put("sampling", new JsonObject().put("tools", new JsonObject()))
            .put("elicitation", new JsonObject().put("form", new JsonObject()));
    static final ClientCapability[] CLIENT_CAPABILITIES = {
            new ClientCapability("sampling", Map.of("tools", Map.of())),
            new ClientCapability("elicitation", Map.of("form", Map.of())) };

    @Override
    @Test
    public void protocolRevision() {
        client().get("/mcp/tools").then().statusCode(200);
        ProtocolAssertions.assertStateless(app(), REVISION);
    }

    @Override
    @Test
    public void readTemplatedResource() {
        client().get("/mcp/resources/readFile/robot-readable.txt").then().statusCode(200)
                .body(Matchers.equalTo("Hello, AI!"));
        ProtocolAssertions.assertStateless(app(), REVISION);
        // Logging opt-in and delivery are verified per request by the direct transport suite.
    }

    protected abstract McpAssured.McpTestClient<?, ?> protocolClient();

    static JsonObject message(McpAssured.McpTestClient<?, ?> protocol, String method, JsonObject params) {
        JsonObject message = protocol.newRequest(method).put("params", params.copy());
        McpAssured.injectStatelessMeta(message, CAPABILITIES.copy());
        return message;
    }

    static JsonObject exchange(McpAssured.McpTestClient<?, ?> protocol, JsonObject message) {
        protocol.sendAndForget(message);
        JsonObject response = protocol.waitForResponse(message);
        Assertions.assertEquals("2.0", response.getString("jsonrpc"));
        Assertions.assertEquals(message.getValue("id"), response.getValue("id"));
        return response;
    }

    static JsonObject result(McpAssured.McpTestClient<?, ?> protocol, String method, JsonObject params) {
        JsonObject response = exchange(protocol, message(protocol, method, params));
        Assertions.assertNull(response.getJsonObject("error"), response::encode);
        return response.getJsonObject("result");
    }

    static void assertComplete(JsonObject result, boolean cacheable) {
        Assertions.assertEquals("complete", result.getString("resultType"));
        JsonObject identity = result.getJsonObject("_meta").getJsonObject("io.modelcontextprotocol/serverInfo");
        Assertions.assertEquals("mcp", identity.getString("name"));
        Assertions.assertFalse(identity.getString("version").isBlank());
        if (cacheable) {
            Assertions.assertEquals(0L, result.getLong("ttlMs"));
            Assertions.assertEquals("public", result.getString("cacheScope"));
        }
    }

    @Test
    public void discovery() {
        try (var protocol = protocolClient()) {
            Assertions.assertNull(protocol.initResult().protocolVersion());
            Assertions.assertEquals("mcp", protocol.initResult().serverName());
            JsonObject discovery = result(protocol, "server/discover", new JsonObject());
            Assertions.assertEquals(ADVERTISED_REVISIONS, discovery.getJsonArray("supportedVersions").getList());
            Assertions.assertFalse(discovery.containsKey("protocolVersion"));
            Assertions.assertFalse(discovery.containsKey("serverInfo"));
            JsonObject capabilities = discovery.getJsonObject("capabilities");
            for (String capability : List.of("tools", "resources", "prompts", "logging", "completions")) {
                Assertions.assertTrue(capabilities.containsKey(capability), discovery::encode);
            }
            assertComplete(discovery, true);
        }
    }

    @Test
    public void toolsPaginationAndSchemas() {
        try (var protocol = protocolClient()) {
            List<JsonObject> tools = new ArrayList<>();
            String cursor = null;
            Set<String> seenCursors = new HashSet<>();
            do {
                JsonObject params = new JsonObject();
                if (cursor != null) {
                    params.put("cursor", cursor);
                }
                JsonObject page = result(protocol, "tools/list", params);
                assertComplete(page, true);
                JsonArray entries = page.getJsonArray("tools");
                Assertions.assertEquals(tools.size() < 6 ? 2 : 1, entries.size());
                entries.forEach(tool -> tools.add((JsonObject) tool));
                cursor = page.getString("nextCursor");
                if (cursor != null) {
                    Assertions.assertTrue(seenCursors.add(cursor), "Pagination repeated a cursor");
                }
            } while (cursor != null);
            Assertions.assertEquals(Set.of("filereader", "rootchecker", "wait", "meta", "sampled", "unsampled", "elicitation"),
                    tools.stream().map(tool -> tool.getString("name")).collect(Collectors.toSet()));
            Assertions.assertEquals(7, tools.size());
            JsonObject file = tools.stream().filter(tool -> "filereader".equals(tool.getString("name"))).findFirst()
                    .orElseThrow();
            JsonObject schema = file.getJsonObject("inputSchema");
            Assertions.assertEquals("object", schema.getString("type"));
            Assertions.assertEquals("string", schema.getJsonObject("properties").getJsonObject("file").getString("type"));
            Assertions.assertEquals(List.of("file"), schema.getJsonArray("required").getList());
            JsonObject roots = tools.stream().filter(tool -> "rootchecker".equals(tool.getString("name"))).findFirst()
                    .orElseThrow();
            JsonObject icon = roots.getJsonArray("icons").getJsonObject(0);
            Assertions.assertEquals("image/svg", icon.getString("mimeType"));
            Assertions.assertEquals("file://quarkus_icon_black.svg", icon.getString("src"));
            JsonObject response = exchange(protocol,
                    message(protocol, "tools/call", call("filereader", new JsonObject().put("file", "robot-readable.txt"))));
            assertText(response, "Hello, AI!");
            assertComplete(response.getJsonObject("result"), false);
        }
    }

    @Test
    public void resourcesAndCacheHints() {
        try (var protocol = protocolClient()) {
            JsonObject list = result(protocol, "resources/list", new JsonObject());
            assertComplete(list, true);
            Assertions.assertEquals(List.of("file:///hello", "file:///number", "file:///separate"),
                    list.getJsonArray("resources").stream()
                            .map(resource -> ((JsonObject) resource).getString("uri")).toList());
            for (var entry : Map.of("file:///hello", "Hello, AI!", "file:///separate", "Hello from other resource!",
                    "file:///robot-readable.txt", "Hello, AI!").entrySet()) {
                JsonObject read = result(protocol, "resources/read", new JsonObject().put("uri", entry.getKey()));
                Assertions.assertEquals(1, read.getJsonArray("contents").size());
                JsonObject content = read.getJsonArray("contents").getJsonObject(0);
                Assertions.assertEquals(entry.getKey(), content.getString("uri"));
                Assertions.assertEquals(entry.getValue(), content.getString("text"));
                if ("file:///separate".equals(entry.getKey())) {
                    assertComplete(read, false);
                    Assertions.assertEquals(5000L, read.getLong("ttlMs"));
                    Assertions.assertEquals("private", read.getString("cacheScope"));
                } else {
                    assertComplete(read, true);
                }
            }
            JsonObject templates = result(protocol, "resources/templates/list", new JsonObject());
            assertComplete(templates, true);
            Assertions.assertEquals("file:///{name}",
                    templates.getJsonArray("resourceTemplates").getJsonObject(0).getString("uriTemplate"));
        }
    }

    @Test
    public void promptsAndCompletion() {
        try (var protocol = protocolClient()) {
            JsonObject list = result(protocol, "prompts/list", new JsonObject());
            assertComplete(list, true);
            Assertions.assertEquals(List.of("foodOrder", "meta"), list.getJsonArray("prompts").stream()
                    .map(prompt -> ((JsonObject) prompt).getString("name")).toList());
            JsonObject prompt = result(protocol, "prompts/get", new JsonObject().put("name", "meta")
                    .put("arguments", new JsonObject().put("type", "tool").put("name", "hammer").put("operation", "create")));
            assertComplete(prompt, false);
            Assertions.assertEquals(1, prompt.getJsonArray("messages").size());
            JsonObject user = prompt.getJsonArray("messages").getJsonObject(0);
            Assertions.assertEquals("user", user.getString("role"));
            Assertions.assertEquals("text", user.getJsonObject("content").getString("type"));
            Assertions.assertEquals("This tool will create a new tool named 'hammer'",
                    user.getJsonObject("content").getString("text"));
            for (var entry : Map.of("", List.of("broccoli", "pizza", "salad", "pie"), "pi", List.of("pizza", "pie"),
                    "piz", List.of("pizza"), "unknown", List.of()).entrySet()) {
                JsonObject completed = result(protocol, "completion/complete", new JsonObject()
                        .put("ref", new JsonObject().put("type", "ref/prompt").put("name", "foodOrder"))
                        .put("argument", new JsonObject().put("name", "food").put("value", entry.getKey())));
                assertComplete(completed, false);
                Assertions.assertEquals(entry.getValue(),
                        completed.getJsonObject("completion").getJsonArray("values").getList());
            }
        }
    }

    @Test
    public void requiredMetadataAndRemovedMethods() {
        try (var protocol = protocolClient()) {
            JsonObject missing = message(protocol, "tools/list", new JsonObject());
            missing.getJsonObject("params").getJsonObject("_meta").remove("io.modelcontextprotocol/clientCapabilities");
            assertError(exchange(protocol, missing), -32602, "clientCapabilities");
            for (String method : List.of("ping", "logging/setLevel", "resources/subscribe", "resources/unsubscribe")) {
                assertError(
                        exchange(protocol,
                                message(protocol, method, new JsonObject().put("uri", "file:///number").put("level", "info"))),
                        -32601, method);
            }
            assertError(
                    exchange(protocol, message(protocol, "resources/read", new JsonObject().put("uri", "missing:///resource"))),
                    -32602, "Resource");
        }
    }

    static void assertError(JsonObject response, int code, String message) {
        Assertions.assertFalse(response.containsKey("result"), response::encode);
        JsonObject error = response.getJsonObject("error");
        Assertions.assertNotNull(error, response::encode);
        Assertions.assertEquals(code, error.getInteger("code"), response::encode);
        Assertions.assertTrue(error.getString("message").contains(message), response::encode);
    }

    @Override
    @Test
    public void toolChanges() {
        var before = client().get("/mcp/tools");
        before.then().statusCode(200);
        List<String> initial = before.jsonPath().getList(".");
        Assertions.assertEquals(7, initial.size());
        Assertions.assertFalse(initial.contains("greeter"));
        client().post("/mcp/meta/greeter").then().statusCode(200);
        try {
            var after = client().get("/mcp/tools");
            after.then().statusCode(200);
            List<String> changed = after.jsonPath().getList(".");
            Assertions.assertEquals(8, changed.size());
            Assertions.assertTrue(changed.containsAll(initial));
            Assertions.assertTrue(changed.contains("greeter"));
        } finally {
            client().delete("/mcp/meta/greeter").then().statusCode(200);
        }
        var last = client().get("/mcp/tools");
        last.then().statusCode(200);
        Assertions.assertEquals(initial, last.jsonPath().getList("."));
    }
}
