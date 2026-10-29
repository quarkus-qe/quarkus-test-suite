package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.WebSocketIT.assertText;
import static io.quarkus.ts.mcp.WebSocketIT.call;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkiverse.mcp.server.ClientCapability;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpTestClient;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;

/**
 * Runs the shared LangChain4j tests with MCP 2026-07-28 and checks basic server
 * operations directly with McpAssured across HTTP, WebSocket and stdio.
 */
public abstract class StatelessMCPIT extends BasicMCPIT {
    static final String STATELESS_PROTOCOL_VERSION = "2026-07-28";

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
        ProtocolAssertions.assertStateless(this::protocolObservations, STATELESS_PROTOCOL_VERSION);
    }

    @Override
    @Test
    public void readTemplatedResource() {
        client().get("/mcp/resources/readFile/robot-readable.txt").then().statusCode(200)
                .body(Matchers.equalTo("Hello, AI!"));
        ProtocolAssertions.assertStateless(this::protocolObservations, STATELESS_PROTOCOL_VERSION);
        // Logging opt-in and delivery are verified per request by the direct transport suite.
    }

    protected abstract McpTestClient<?, ?> protocolClient();

    static JsonObject message(McpTestClient<?, ?> protocol, String method, JsonObject params) {
        JsonObject message = protocol.newRequest(method).put("params", params.copy());
        McpAssured.injectStatelessMeta(message, CAPABILITIES.copy());
        return message;
    }

    static JsonObject exchange(McpTestClient<?, ?> protocol, JsonObject request) {
        protocol.sendAndForget(request);
        return protocol.waitForResponse(request);
    }

    @Test
    public void discovery() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            Assertions.assertNull(protocol.initResult().protocolVersion());
            JsonObject request = message(protocol, "server/discover", new JsonObject());
            JsonObject response = exchange(protocol, request);
            // Validate the common response envelope and server identity here, rather than on every operation.
            Assertions.assertEquals("2.0", response.getString("jsonrpc"));
            Assertions.assertEquals(request.getValue("id"), response.getValue("id"));
            Assertions.assertNull(response.getJsonObject("error"), response::encode);
            JsonObject discovery = response.getJsonObject("result");
            Assertions.assertNotNull(discovery, response::encode);
            Assertions.assertEquals("complete", discovery.getString("resultType"));
            JsonObject identity = discovery.getJsonObject("_meta").getJsonObject("io.modelcontextprotocol/serverInfo");
            Assertions.assertEquals("mcp", identity.getString("name"));
            Assertions.assertFalse(identity.getString("version").isBlank());
            Assertions.assertEquals(ADVERTISED_REVISIONS, discovery.getJsonArray("supportedVersions").getList());
            Assertions.assertFalse(discovery.containsKey("protocolVersion"));
            Assertions.assertFalse(discovery.containsKey("serverInfo"));
            JsonObject capabilities = discovery.getJsonObject("capabilities");
            for (String capability : List.of("tools", "resources", "prompts", "logging", "completions")) {
                Assertions.assertTrue(capabilities.containsKey(capability), discovery::encode);
            }
            Assertions.assertEquals(0L, discovery.getLong("ttlMs"));
            Assertions.assertEquals("public", discovery.getString("cacheScope"));
        }
    }

    @Test
    public void toolsPagination() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            List<JsonObject> pages = toolPages(protocol);
            // Seven fixture tools and a page size of two produce 2 + 2 + 2 + 1 entries.
            Assertions.assertEquals(List.of(2, 2, 2, 1),
                    pages.stream().map(page -> page.getJsonArray("tools").size()).toList());
            for (JsonObject page : pages) {
                Assertions.assertEquals("complete", page.getString("resultType"));
                Assertions.assertEquals(0L, page.getLong("ttlMs"));
                Assertions.assertEquals("public", page.getString("cacheScope"));
            }
            List<String> names = pages.stream().flatMap(page -> page.getJsonArray("tools").stream())
                    .map(tool -> ((JsonObject) tool).getString("name")).sorted().toList();
            // List equality checks both the inventory and duplicates, which a set would discard.
            Assertions.assertEquals(List.of("elicitation", "filereader", "meta", "rootchecker", "sampled", "unsampled", "wait"),
                    names);
        }
    }

    @Test
    public void fileReaderSchema() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject file = toolPages(protocol).stream().flatMap(page -> page.getJsonArray("tools").stream())
                    .map(JsonObject.class::cast).filter(tool -> "filereader".equals(tool.getString("name")))
                    .findFirst().orElseThrow();
            JsonObject schema = file.getJsonObject("inputSchema");
            Assertions.assertEquals("object", schema.getString("type"));
            Assertions.assertEquals("string", schema.getJsonObject("properties").getJsonObject("file").getString("type"));
            Assertions.assertEquals(List.of("file"), schema.getJsonArray("required").getList());
        }
    }

    @Test
    public void toolIcons() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject roots = toolPages(protocol).stream().flatMap(page -> page.getJsonArray("tools").stream())
                    .map(JsonObject.class::cast).filter(tool -> "rootchecker".equals(tool.getString("name")))
                    .findFirst().orElseThrow();
            JsonObject icon = roots.getJsonArray("icons").getJsonObject(0);
            Assertions.assertEquals("image/svg", icon.getString("mimeType"));
            Assertions.assertEquals("file://quarkus_icon_black.svg", icon.getString("src"));
        }
    }

    // The fixture has seven tools, plus at most one dynamically created tool: at most four two-entry pages.
    static List<JsonObject> toolPages(McpTestClient<?, ?> protocol) {
        List<JsonObject> pages = new ArrayList<>();
        JsonObject params = new JsonObject();
        for (int page = 0; page < 4; page++) {
            JsonObject response = exchange(protocol, message(protocol, "tools/list", params));
            JsonObject result = response.getJsonObject("result");
            Assertions.assertNotNull(result, response::encode);
            pages.add(result);
            String cursor = result.getString("nextCursor");
            if (cursor == null) {
                return pages;
            }
            params.put("cursor", cursor);
        }
        throw new AssertionError("Fixture tools/list did not terminate within four pages");
    }

    @Test
    public void callFileTool() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject response = exchange(protocol,
                    message(protocol, "tools/call", call("filereader", new JsonObject().put("file", "robot-readable.txt"))));
            assertText(response, "Hello, AI!");
            Assertions.assertEquals("complete", response.getJsonObject("result").getString("resultType"));
        }
    }

    @Test
    public void resourceList() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject response = exchange(protocol, message(protocol, "resources/list", new JsonObject()));
            JsonObject list = response.getJsonObject("result");
            Assertions.assertNotNull(list, response::encode);
            Assertions.assertEquals(List.of("file:///hello", "file:///number", "file:///separate"),
                    list.getJsonArray("resources").stream().map(resource -> ((JsonObject) resource).getString("uri")).toList());
            Assertions.assertEquals("complete", list.getString("resultType"));
            Assertions.assertEquals(0L, list.getLong("ttlMs"));
            Assertions.assertEquals("public", list.getString("cacheScope"));
        }
    }

    @Test
    public void readAnnotatedResource() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject response = exchange(protocol,
                    message(protocol, "resources/read", new JsonObject().put("uri", "file:///hello")));
            JsonObject read = response.getJsonObject("result");
            Assertions.assertNotNull(read, response::encode);
            Assertions.assertEquals(1, read.getJsonArray("contents").size());
            JsonObject content = read.getJsonArray("contents").getJsonObject(0);
            Assertions.assertEquals("file:///hello", content.getString("uri"));
            Assertions.assertEquals("Hello, AI!", content.getString("text"));
            Assertions.assertEquals("complete", read.getString("resultType"));
            Assertions.assertEquals(0L, read.getLong("ttlMs"));
            Assertions.assertEquals("public", read.getString("cacheScope"));
        }
    }

    @Test
    public void readProgrammaticResourceWithCacheHints() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject response = exchange(protocol,
                    message(protocol, "resources/read", new JsonObject().put("uri", "file:///separate")));
            JsonObject read = response.getJsonObject("result");
            Assertions.assertNotNull(read, response::encode);
            Assertions.assertEquals(1, read.getJsonArray("contents").size());
            JsonObject content = read.getJsonArray("contents").getJsonObject(0);
            Assertions.assertEquals("file:///separate", content.getString("uri"));
            Assertions.assertEquals("Hello from other resource!", content.getString("text"));
            Assertions.assertEquals("complete", read.getString("resultType"));
            Assertions.assertEquals(5000L, read.getLong("ttlMs"));
            Assertions.assertEquals("private", read.getString("cacheScope"));
        }
    }

    @Test
    public void readTemplatedResourceDirectly() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject response = exchange(protocol,
                    message(protocol, "resources/read", new JsonObject().put("uri", "file:///robot-readable.txt")));
            JsonObject read = response.getJsonObject("result");
            Assertions.assertNotNull(read, response::encode);
            Assertions.assertEquals(1, read.getJsonArray("contents").size());
            JsonObject content = read.getJsonArray("contents").getJsonObject(0);
            Assertions.assertEquals("file:///robot-readable.txt", content.getString("uri"));
            Assertions.assertEquals("Hello, AI!", content.getString("text"));
            Assertions.assertEquals("complete", read.getString("resultType"));
            Assertions.assertEquals(0L, read.getLong("ttlMs"));
            Assertions.assertEquals("public", read.getString("cacheScope"));
        }
    }

    @Test
    public void resourceTemplates() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject response = exchange(protocol, message(protocol, "resources/templates/list", new JsonObject()));
            JsonObject templates = response.getJsonObject("result");
            Assertions.assertNotNull(templates, response::encode);
            Assertions.assertEquals("file:///{name}",
                    templates.getJsonArray("resourceTemplates").getJsonObject(0).getString("uriTemplate"));
            Assertions.assertEquals("complete", templates.getString("resultType"));
            Assertions.assertEquals(0L, templates.getLong("ttlMs"));
            Assertions.assertEquals("public", templates.getString("cacheScope"));
        }
    }

    @Test
    public void promptList() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject response = exchange(protocol, message(protocol, "prompts/list", new JsonObject()));
            JsonObject list = response.getJsonObject("result");
            Assertions.assertNotNull(list, response::encode);
            Assertions.assertEquals(List.of("foodOrder", "meta"), list.getJsonArray("prompts").stream()
                    .map(prompt -> ((JsonObject) prompt).getString("name")).toList());
            Assertions.assertEquals("complete", list.getString("resultType"));
            Assertions.assertEquals(0L, list.getLong("ttlMs"));
            Assertions.assertEquals("public", list.getString("cacheScope"));
        }
    }

    @Test
    public void promptRendering() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject response = exchange(protocol, message(protocol, "prompts/get", new JsonObject().put("name", "meta")
                    .put("arguments", new JsonObject().put("type", "tool").put("name", "hammer").put("operation", "create"))));
            JsonObject prompt = response.getJsonObject("result");
            Assertions.assertNotNull(prompt, response::encode);
            Assertions.assertEquals("complete", prompt.getString("resultType"));
            Assertions.assertEquals(1, prompt.getJsonArray("messages").size());
            JsonObject user = prompt.getJsonArray("messages").getJsonObject(0);
            Assertions.assertEquals("user", user.getString("role"));
            Assertions.assertEquals("text", user.getJsonObject("content").getString("type"));
            Assertions.assertEquals("This tool will create a new tool named 'hammer'",
                    user.getJsonObject("content").getString("text"));
        }
    }

    @Test
    public void completion() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            for (Entry<String, List<String>> entry : Map.of("", List.of("broccoli", "pizza", "salad", "pie"),
                    "pi", List.of("pizza", "pie"), "piz", List.of("pizza"), "unknown", List.<String> of()).entrySet()) {
                JsonObject response = exchange(protocol, message(protocol, "completion/complete", new JsonObject()
                        .put("ref", new JsonObject().put("type", "ref/prompt").put("name", "foodOrder"))
                        .put("argument", new JsonObject().put("name", "food").put("value", entry.getKey()))));
                JsonObject completed = response.getJsonObject("result");
                Assertions.assertNotNull(completed, response::encode);
                Assertions.assertEquals("complete", completed.getString("resultType"));
                Assertions.assertEquals(entry.getValue(),
                        completed.getJsonObject("completion").getJsonArray("values").getList(),
                        "Completion prefix: " + entry.getKey());
            }
        }
    }

    @Test
    public void missingCapabilitiesMetadata() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject request = message(protocol, "tools/list", new JsonObject());
            request.getJsonObject("params").getJsonObject("_meta").remove("io.modelcontextprotocol/clientCapabilities");
            assertError(exchange(protocol, request), -32602, "clientCapabilities");
        }
    }

    @Test
    public void removedMethods() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            for (String method : List.of("ping", "logging/setLevel", "resources/subscribe", "resources/unsubscribe")) {
                assertError(exchange(protocol,
                        message(protocol, method, new JsonObject().put("uri", "file:///number").put("level", "info"))),
                        -32601, method);
            }
        }
    }

    @Test
    public void unknownResource() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            assertError(exchange(protocol,
                    message(protocol, "resources/read", new JsonObject().put("uri", "missing:///resource"))),
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
        Response before = client().get("/mcp/tools");
        before.then().statusCode(200);
        List<String> initial = before.jsonPath().getList(".");
        Assertions.assertEquals(7, initial.size());
        Assertions.assertFalse(initial.contains("greeter"));
        client().post("/mcp/meta/greeter").then().statusCode(200);
        try {
            Response after = client().get("/mcp/tools");
            after.then().statusCode(200);
            List<String> changed = after.jsonPath().getList(".");
            Assertions.assertEquals(8, changed.size());
            Assertions.assertTrue(changed.containsAll(initial));
            Assertions.assertTrue(changed.contains("greeter"));
        } finally {
            client().delete("/mcp/meta/greeter").then().statusCode(200);
        }
        Response last = client().get("/mcp/tools");
        last.then().statusCode(200);
        Assertions.assertEquals(initial, last.jsonPath().getList("."));
    }
}
