package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.app.Utils.getFileFolder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkiverse.mcp.server.ClientCapability;
import io.quarkiverse.mcp.server.McpProtocolVersion;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.AdvancedServer;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MyResources;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;

@QuarkusScenario
public class WebSocketIT extends StatefulMCPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-websocket")
    }, classes = { FileServer.class, MyResources.class, AdvancedServer.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(WebSocketIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.protocol-version", "2025-11-25")
            .withProperty("quarkus.langchain4j.mcp.filesystem.log-responses", "true")
            .withProperty("quarkus.langchain4j.mcp.filesystem.log-requests", "true")
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "websocket")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> getUrl(server));
    private static final int NUMBER_OF_TOOLS = 7; // 4 declared in FileServer class, 3 in AdvancedServer

    protected static String getUrl(RestService service) {
        return service.getURI(Protocol.WS).withPath("/mcp/ws").toString();
    }

    @Override
    public RequestSpecification client() {
        return client.given();
    }

    @Override
    public RestService app() {
        return client;
    }

    @Test
    @Override // This test has access to more tools due to AdvancedServer class. They also may be in a different order
    public void toolChanges() {
        Response before = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, before.statusCode());
        List<String> initialTools = before.body().jsonPath().getList(".");
        Assertions.assertEquals(NUMBER_OF_TOOLS, initialTools.size());
        Assertions.assertFalse(initialTools.contains("greeter"));

        Response create = client().given().post("/mcp/meta/greeter");
        Assertions.assertEquals(200, create.statusCode());

        Response after = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, after.statusCode());
        List<String> changedTools = after.body().jsonPath().getList(".");
        Assertions.assertEquals(NUMBER_OF_TOOLS + 1, changedTools.size());
        Assertions.assertTrue(changedTools.contains("greeter"));

        Response delete = client().given().delete("/mcp/meta/greeter");
        Assertions.assertEquals(200, delete.statusCode());

        Response last = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, last.statusCode());
        List<String> finalTools = last.body().jsonPath().getList(".");
        Assertions.assertEquals(NUMBER_OF_TOOLS, finalTools.size());
        Assertions.assertFalse(finalTools.contains("greeter"));
    }

    protected RestService serverApp() {
        return server;
    }

    protected McpAssured.McpWebSocketTestClient protocolClient() {
        return McpAssured.newWebSocketClient()
                .setBaseUri(URI.create(serverApp().getURI(Protocol.HTTP).toString()))
                .setEndpointPath("/mcp/ws")
                .setProtocolVersion(McpProtocolVersion.V_2025_11_25)
                .setClientCapabilities(new ClientCapability("sampling", Map.of("tools", Map.of())),
                        new ClientCapability("elicitation", Map.of("form", Map.of())))
                .build().connect(init -> {
                    Assertions.assertEquals("2025-11-25", init.protocolVersion());
                    Assertions.assertEquals("mcp", init.serverName());
                });
    }

    @Test
    void iconCheck() {
        try (var protocol = protocolClient()) {
            List<JsonObject> tools = new ArrayList<>();
            String cursor = null;
            do {
                JsonObject params = new JsonObject();
                if (cursor != null) {
                    params.put("cursor", cursor);
                }
                JsonObject result = request(protocol, "tools/list", params).getJsonObject("result");
                Assertions.assertTrue(result.getJsonArray("tools").size() <= 2);
                if (tools.isEmpty()) {
                    Assertions.assertEquals(2, result.getJsonArray("tools").size());
                    Assertions.assertNotNull(result.getString("nextCursor"));
                }
                result.getJsonArray("tools").forEach(tool -> tools.add((JsonObject) tool));
                cursor = result.getString("nextCursor");
            } while (cursor != null);
            Assertions.assertEquals(NUMBER_OF_TOOLS, tools.size());
            JsonObject rootchecker = tools.stream().filter(tool -> "rootchecker".equals(tool.getString("name")))
                    .findFirst().orElseThrow();
            Assertions.assertEquals("image/svg", rootchecker.getJsonArray("icons").getJsonObject(0).getString("mimeType"));
        }
    }

    @Test
    void resourceNotifications() {
        client().get("/mcp/resources/").then().statusCode(200)
                .body(Matchers.equalTo("[file:///hello, file:///number, file:///separate]"));
        try (var protocol = protocolClient()) {
            client().get("/mcp/resources/readFile/number").then().statusCode(200).body(Matchers.equalTo("1"));
            JsonObject subscribed = request(protocol, "resources/subscribe", new JsonObject().put("uri", "file:///number"));
            Assertions.assertTrue(subscribed.getJsonObject("result").isEmpty());
            try {
                int count = protocol.snapshot().notifications().size();
                client().put("/mcp/meta/resources/number").then().statusCode(HttpStatus.SC_NO_CONTENT);
                JsonObject notification = protocol.waitForNotifications(count + 1).notifications().get(count);
                Assertions.assertEquals("notifications/resources/updated", notification.getString("method"));
                Assertions.assertEquals("file:///number", notification.getJsonObject("params").getString("uri"));
                client().get("/mcp/resources/readFile/number").then().statusCode(200).body(Matchers.equalTo("2"));
            } finally {
                request(protocol, "resources/unsubscribe", new JsonObject().put("uri", "file:///number"));
            }
        }
    }

    @Test
    void promptNotifications() {
        try (var protocol = protocolClient()) {
            assertPrompts(protocol, List.of("foodOrder", "meta"));
            int count = protocol.snapshot().notifications().size();
            client().post("/mcp/meta/prompt/about-greeter").then().statusCode(200);
            try {
                Assertions.assertEquals("notifications/prompts/list_changed",
                        protocol.waitForNotifications(count + 1).notifications().get(count).getString("method"));
                assertPrompts(protocol, List.of("foodOrder", "meta", "about-greeter"));
            } finally {
                client().delete("/mcp/meta/prompt/about-greeter").then().statusCode(200);
            }
            Assertions.assertEquals("notifications/prompts/list_changed",
                    protocol.waitForNotifications(count + 2).notifications().get(count + 1).getString("method"));
            assertPrompts(protocol, List.of("foodOrder", "meta"));
        }
    }

    private void assertPrompts(McpAssured.McpWebSocketTestClient protocol, List<String> names) {
        var prompts = request(protocol, "prompts/list", new JsonObject()).getJsonObject("result").getJsonArray("prompts");
        Assertions.assertEquals(names, prompts.stream().map(p -> ((JsonObject) p).getString("name")).toList());
    }

    @Test
    void sampling() {
        String question = "What is the airspeed velocity of unladen swallow?";
        try (var protocol = protocolClient()) {
            assertText(request(protocol, "tools/call", call("unsampled", new JsonObject().put("question", question))),
                    "Answer to \"" + question + "\" is 42");
            JsonObject operation = protocol.newRequest("tools/call").put("params",
                    call("sampled", new JsonObject().put("question", question)));
            int count = protocol.snapshot().requests().size();
            protocol.sendAndForget(operation);
            JsonObject sampling = protocol.waitForRequests(count + 1).requests().get(count);
            Assertions.assertEquals("sampling/createMessage", sampling.getString("method"));
            Assertions.assertEquals("An African or European swallow?", sampling.getJsonObject("params")
                    .getJsonArray("messages").getJsonObject(0).getJsonObject("content").getString("text"));
            protocol.sendAndForget(new JsonObject().put("jsonrpc", "2.0").put("id", sampling.getValue("id"))
                    .put("result", new JsonObject().put("role", "assistant").put("model", "shmodel")
                            .put("stopReason", "endTurn")
                            .put("content", new JsonObject().put("type", "text").put("text", "I do not know"))));
            assertText(protocol.waitForResponse(operation), "The answer to \"" + question + "\" is \"I do not know\"");
        }
    }

    @Test
    void elicitation() {
        try (var protocol = protocolClient()) {
            JsonObject operation = protocol.newRequest("tools/call").put("params", call("elicitation", new JsonObject()));
            int count = protocol.snapshot().requests().size();
            protocol.sendAndForget(operation);
            JsonObject elicitation = protocol.waitForRequests(count + 1).requests().get(count);
            Assertions.assertEquals("elicitation/create", elicitation.getString("method"));
            Assertions.assertEquals("form", elicitation.getJsonObject("params").getString("mode"));
            Assertions.assertEquals("Please provide your information:",
                    elicitation.getJsonObject("params").getString("message"));
            protocol.sendAndForget(new JsonObject().put("jsonrpc", "2.0").put("id", elicitation.getValue("id"))
                    .put("result", new JsonObject().put("action", "accept").put("content",
                            new JsonObject().put("name", "QE client"))));
            assertText(protocol.waitForResponse(operation), "Hello QE client!");
        }
    }

    @Test
    void completion() {
        try (var protocol = protocolClient()) {
            for (var entry : Map.of("", List.of("broccoli", "pizza", "salad", "pie"),
                    "pi", List.of("pizza", "pie"), "piz", List.of("pizza")).entrySet()) {
                JsonObject params = new JsonObject()
                        .put("ref", new JsonObject().put("type", "ref/prompt").put("name", "foodOrder"))
                        .put("argument", new JsonObject().put("name", "food").put("value", entry.getKey()));
                Assertions.assertEquals(entry.getValue(),
                        request(protocol, "completion/complete", params).getJsonObject("result")
                                .getJsonObject("completion").getJsonArray("values").getList());
            }
        }
    }

    @Test
    void automaticProgress() {
        progress(true, "autoToken256", "Processing item #");
    }

    @Test
    void manualProgress() {
        progress(false, "manualToken512", "Processing step ");
    }

    private void progress(boolean automatic, String token, String prefix) {
        try (var protocol = protocolClient()) {
            int count = protocol.snapshot().notifications().size();
            JsonObject response = request(protocol, "tools/call",
                    call("wait", new JsonObject().put("input", "this is it").put("autoLog", automatic))
                            .put("_meta", new JsonObject().put("progressToken", token)));
            List<JsonObject> notifications = protocol.waitForNotifications(count + 3).notifications().subList(count, count + 3);
            Assertions.assertEquals(count + 3, protocol.snapshot().notifications().size());
            for (int i = 0; i < 3; i++) {
                JsonObject notification = notifications.get(i);
                Assertions.assertEquals("notifications/progress", notification.getString("method"));
                JsonObject params = notification.getJsonObject("params");
                Assertions.assertEquals(token, params.getString("progressToken"));
                Assertions.assertEquals(i + 1, params.getInteger("progress"));
                Assertions.assertEquals(3, params.getInteger("total"));
                Assertions.assertEquals(prefix + (i + 1) + (automatic ? "" : " of 3"), params.getString("message"));
            }
            assertText(response, "this is it: 123");
        }
    }

    @Test
    public void logging() throws InterruptedException {
        try (var protocol = protocolClient()) {
            Assertions.assertTrue(
                    protocol.initResult().capabilities().stream().anyMatch(capability -> "logging".equals(capability.name())));
            int count = protocol.snapshot().notifications().size();
            JsonObject response = request(protocol, "resources/read", new JsonObject().put("uri", "file:///another-file.txt"));
            Assertions.assertEquals("There is some content there, too!",
                    response.getJsonObject("result").getJsonArray("contents").getJsonObject(0).getString("text"));
            JsonObject log = protocol.waitForNotifications(count + 1).notifications().get(count);
            Assertions.assertEquals(count + 1, protocol.snapshot().notifications().size());
            Assertions.assertEquals("notifications/message", log.getString("method"));
            Assertions.assertEquals("info", log.getJsonObject("params").getString("level"));
            Assertions.assertEquals("resource_template:fileresource", log.getJsonObject("params").getString("logger"));
            String message = log.getJsonObject("params").getString("data");
            Assertions.assertTrue(message.startsWith("Reading provided file"));
            Assertions.assertTrue(message.endsWith("another-file.txt"));
            List<String> logs = List.of();
            for (int attempt = 0; attempt < 10; attempt++) {
                logs = serverApp().getLogs().stream().filter(line -> line.endsWith("another-file.txt")).toList();
                if (!logs.isEmpty()) {
                    break;
                }
                Thread.sleep(1000);
            }
            Assertions.assertEquals(1, logs.size(), "Expected the server log: " + logs);
            Assertions.assertTrue(logs.get(0).matches("\\d{2}:\\d{2}:\\d{2},\\d{3} Reading provided file: .*another-file.txt"));
        }
    }

    static JsonObject request(McpAssured.McpTestClient<?, ?> protocol, String method, JsonObject params) {
        JsonObject request = protocol.newRequest(method).put("params", params);
        protocol.sendAndForget(request);
        JsonObject response = protocol.waitForResponse(request);
        Assertions.assertEquals("2.0", response.getString("jsonrpc"));
        Assertions.assertEquals(request.getValue("id"), response.getValue("id"));
        Assertions.assertNull(response.getJsonObject("error"), response::encode);
        return response;
    }

    static JsonObject call(String name, JsonObject arguments) {
        return new JsonObject().put("name", name).put("arguments", arguments);
    }

    static void assertText(JsonObject response, String text) {
        JsonObject result = response.getJsonObject("result");
        Assertions.assertFalse(result.getBoolean("isError"));
        Assertions.assertEquals(1, result.getJsonArray("content").size());
        Assertions.assertEquals("text", result.getJsonArray("content").getJsonObject(0).getString("type"));
        Assertions.assertEquals(text, result.getJsonArray("content").getJsonObject(0).getString("text"));
    }
}
