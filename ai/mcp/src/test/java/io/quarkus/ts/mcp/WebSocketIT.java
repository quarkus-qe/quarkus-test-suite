package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.app.Utils.getFileFolder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkiverse.mcp.server.ClientCapability;
import io.quarkiverse.mcp.server.McpProtocolVersion;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpTestClient;
import io.quarkiverse.mcp.server.test.McpAssured.McpWebSocketTestClient;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.AdvancedServer;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolObservationsResource;
import io.quarkus.ts.mcp.app.ProtocolObserver;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;

@QuarkusScenario
public class WebSocketIT extends StatefulMCPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-websocket")
    }, classes = { FileServer.class, ProtocolObserver.class, ProtocolObservationsResource.class, MyResources.class,
            AdvancedServer.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(WebSocketIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.protocol-version", "2025-11-25")
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

        try {
            Response after = client().given().get("/mcp/tools");
            Assertions.assertEquals(200, after.statusCode());
            List<String> changedTools = after.body().jsonPath().getList(".");
            Assertions.assertEquals(NUMBER_OF_TOOLS + 1, changedTools.size());
            Assertions.assertTrue(changedTools.contains("greeter"));
        } finally {
            Response delete = client().given().delete("/mcp/meta/greeter");
            Assertions.assertEquals(200, delete.statusCode());
        }

        Response last = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, last.statusCode());
        List<String> finalTools = last.body().jsonPath().getList(".");
        Assertions.assertEquals(NUMBER_OF_TOOLS, finalTools.size());
        Assertions.assertFalse(finalTools.contains("greeter"));
    }

    protected RestService serverApp() {
        return server;
    }

    protected McpWebSocketTestClient protocolClient() {
        return McpAssured.newWebSocketClient()
                .setBaseUri(URI.create(serverApp().getURI(Protocol.HTTP).toString()))
                .setEndpointPath("/mcp/ws")
                .setProtocolVersion(McpProtocolVersion.V_2025_11_25)
                .setClientCapabilities(new ClientCapability("sampling", Map.of("tools", Map.of())),
                        new ClientCapability("elicitation", Map.of("form", Map.of())))
                .build().connect();
    }

    @Test
    void initializationAndResponseEnvelope() {
        try (McpWebSocketTestClient protocol = protocolClient()) {
            Assertions.assertEquals("2025-11-25", protocol.initResult().protocolVersion());
            Assertions.assertEquals("mcp", protocol.initResult().serverName());
            Assertions.assertTrue(protocol.initResult().capabilities().stream()
                    .anyMatch(capability -> "logging".equals(capability.name())));
            JsonObject request = protocol.newRequest("tools/list").put("params", new JsonObject());
            protocol.sendAndForget(request);
            JsonObject response = protocol.waitForResponse(request);
            Assertions.assertEquals("2.0", response.getString("jsonrpc"));
            Assertions.assertEquals(request.getValue("id"), response.getValue("id"));
            Assertions.assertNull(response.getJsonObject("error"), response::encode);
            Assertions.assertNotNull(response.getJsonObject("result"), response::encode);
        }
    }

    @Test
    void toolsPagination() {
        try (McpWebSocketTestClient protocol = protocolClient()) {
            List<JsonObject> pages = toolPages(protocol);
            Assertions.assertEquals(List.of(2, 2, 2, 1),
                    pages.stream().map(page -> page.getJsonArray("tools").size()).toList());
            Assertions.assertEquals(NUMBER_OF_TOOLS, pages.stream().mapToInt(page -> page.getJsonArray("tools").size()).sum());
        }
    }

    @Test
    void iconCheck() {
        try (McpWebSocketTestClient protocol = protocolClient()) {
            JsonObject rootchecker = toolPages(protocol).stream().flatMap(page -> page.getJsonArray("tools").stream())
                    .map(JsonObject.class::cast).filter(tool -> "rootchecker".equals(tool.getString("name")))
                    .findFirst().orElseThrow();
            Assertions.assertEquals("image/svg", rootchecker.getJsonArray("icons").getJsonObject(0).getString("mimeType"));
        }
    }

    private List<JsonObject> toolPages(McpWebSocketTestClient protocol) {
        List<JsonObject> pages = new ArrayList<>();
        JsonObject params = new JsonObject();
        // Seven fixture tools, page size two: four pages suffice; a bad cursor cannot loop forever.
        for (int page = 0; page < 4; page++) {
            JsonObject response = request(protocol, "tools/list", params);
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
    void resourceList() {
        client().get("/mcp/resources/").then().statusCode(200)
                .body(Matchers.equalTo("[file:///hello, file:///number, file:///separate]"));
    }

    @Test
    void resourceNotifications() {
        try (McpWebSocketTestClient protocol = protocolClient()) {
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
                JsonObject unsubscribed = request(protocol, "resources/unsubscribe",
                        new JsonObject().put("uri", "file:///number"));
                Assertions.assertNotNull(unsubscribed.getJsonObject("result"), unsubscribed::encode);
                Assertions.assertTrue(unsubscribed.getJsonObject("result").isEmpty(), unsubscribed::encode);
            }
        }
    }

    @Test
    void promptNotifications() {
        try (McpWebSocketTestClient protocol = protocolClient()) {
            JsonObject before = request(protocol, "prompts/list", new JsonObject());
            Assertions.assertNotNull(before.getJsonObject("result"), before::encode);
            Assertions.assertEquals(List.of("foodOrder", "meta"),
                    before.getJsonObject("result").getJsonArray("prompts").stream()
                            .map(prompt -> ((JsonObject) prompt).getString("name")).toList());
            int count = protocol.snapshot().notifications().size();
            client().post("/mcp/meta/prompt/about-greeter").then().statusCode(200);
            try {
                Assertions.assertEquals("notifications/prompts/list_changed",
                        protocol.waitForNotifications(count + 1).notifications().get(count).getString("method"));
                JsonObject created = request(protocol, "prompts/list", new JsonObject());
                Assertions.assertNotNull(created.getJsonObject("result"), created::encode);
                Assertions.assertEquals(List.of("foodOrder", "meta", "about-greeter"),
                        created.getJsonObject("result").getJsonArray("prompts").stream()
                                .map(prompt -> ((JsonObject) prompt).getString("name")).toList());
            } finally {
                client().delete("/mcp/meta/prompt/about-greeter").then().statusCode(200);
            }
            Assertions.assertEquals("notifications/prompts/list_changed",
                    protocol.waitForNotifications(count + 2).notifications().get(count + 1).getString("method"));
            JsonObject deleted = request(protocol, "prompts/list", new JsonObject());
            Assertions.assertNotNull(deleted.getJsonObject("result"), deleted::encode);
            Assertions.assertEquals(List.of("foodOrder", "meta"),
                    deleted.getJsonObject("result").getJsonArray("prompts").stream()
                            .map(prompt -> ((JsonObject) prompt).getString("name")).toList());
        }
    }

    @Test
    void samplingWithoutClientInput() {
        try (McpWebSocketTestClient protocol = protocolClient()) {
            assertText(request(protocol, "tools/call", call("unsampled", new JsonObject().put("question", "Question"))),
                    "Answer to \"Question\" is 42");
        }
    }

    @Test
    void sampling() {
        String question = "What is the airspeed velocity of unladen swallow?";
        try (McpWebSocketTestClient protocol = protocolClient()) {
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
        try (McpWebSocketTestClient protocol = protocolClient()) {
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
        try (McpWebSocketTestClient protocol = protocolClient()) {
            for (Entry<String, List<String>> entry : Map.of("", List.of("broccoli", "pizza", "salad", "pie"),
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
        try (McpWebSocketTestClient protocol = protocolClient()) {
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
        try (McpWebSocketTestClient protocol = protocolClient()) {
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

    static JsonObject request(McpTestClient<?, ?> protocol, String method, JsonObject params) {
        JsonObject request = protocol.newRequest(method).put("params", params);
        protocol.sendAndForget(request);
        return protocol.waitForResponse(request);
    }

    static JsonObject call(String name, JsonObject arguments) {
        return new JsonObject().put("name", name).put("arguments", arguments);
    }

    static void assertText(JsonObject response, String text) {
        JsonObject result = response.getJsonObject("result");
        Assertions.assertNotNull(result, response::encode);
        Assertions.assertEquals(false, result.getBoolean("isError"), response::encode);
        Assertions.assertEquals(1, result.getJsonArray("content").size());
        Assertions.assertEquals("text", result.getJsonArray("content").getJsonObject(0).getString("type"));
        Assertions.assertEquals(text, result.getJsonArray("content").getJsonObject(0).getString("text"));
    }

    @Override
    protected List<JsonObject> protocolObservations() {
        return ProtocolAssertions.observations(serverApp());
    }

}
