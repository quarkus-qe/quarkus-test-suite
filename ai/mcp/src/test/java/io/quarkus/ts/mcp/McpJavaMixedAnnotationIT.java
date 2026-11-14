package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.WebSocketIT.assertText;
import static io.quarkus.ts.mcp.WebSocketIT.call;
import static io.quarkus.ts.mcp.WebSocketIT.request;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkiverse.mcp.server.ClientCapability;
import io.quarkiverse.mcp.server.McpProtocolVersion;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.McpJavaMixedAnnotationServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * An application mixing both {@code io.quarkiverse.mcp.server} and {@code org.mcpjava.server} annotations,
 * in a single bean class, over Streamable HTTP.
 */
@QuarkusScenario
public class McpJavaMixedAnnotationIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { McpJavaMixedAnnotationServer.class })
    static final RestService server = new RestService();

    private McpStreamableTestClient protocolClient() {
        return McpAssured.newStreamableClient()
                .setBaseUri(URI.create(server.getURI(Protocol.HTTP).toString()))
                .setProtocolVersion(McpProtocolVersion.V_2025_11_25)
                .setClientCapabilities(new ClientCapability("sampling", Map.of("tools", Map.of())),
                        new ClientCapability("elicitation", Map.of("form", Map.of())),
                        new ClientCapability("roots", Map.of()))
                .build().connect();
    }

    private static List<JsonObject> allTools(McpStreamableTestClient protocol) {
        List<JsonObject> tools = new ArrayList<>();
        JsonObject params = new JsonObject();
        for (int page = 0; page < 4; page++) {
            JsonObject result = request(protocol, "tools/list", params).getJsonObject("result");
            Assertions.assertNotNull(result, "Missing result in the tools/list response");
            result.getJsonArray("tools").forEach(tool -> tools.add((JsonObject) tool));
            String cursor = result.getString("nextCursor");
            if (cursor == null) {
                return tools;
            }
            params.put("cursor", cursor);
        }
        throw new AssertionError("tools/list did not terminate within four pages");
    }

    private static List<String> names(List<JsonObject> features) {
        return features.stream().map(feature -> feature.getString("name")).sorted().toList();
    }

    private static List<String> names(JsonObject response, String key) {
        JsonArray features = response.getJsonObject("result").getJsonArray(key);
        return features.stream().map(feature -> ((JsonObject) feature).getString("name")).sorted().toList();
    }

    // QUARKUS-8957 - Mixed Application Scenario: both annotation sets in one application and in one class
    @Test
    public void featureList() {
        try (McpStreamableTestClient protocol = protocolClient()) {
            Assertions.assertEquals(
                    List.of("quarkusGreeter", "standardElicitation", "standardGreeter", "standardRoots",
                            "standardSampling"),
                    names(allTools(protocol)));
            Assertions.assertEquals(List.of("quarkusOrder", "standardOrder"),
                    names(request(protocol, "prompts/list", new JsonObject()), "prompts"));
        }
    }

    // QUARKUS-8957 - Mixed Application Scenario: the two sets reach the same behaviour, for a tool and a prompt
    @Test
    public void annotationFamilyParity() {
        try (McpStreamableTestClient protocol = protocolClient()) {
            assertText(request(protocol, "tools/call", call("quarkusGreeter", new JsonObject().put("name", "Duke"))),
                    "Hello (quarkus), Duke!");
            assertText(request(protocol, "tools/call", call("standardGreeter", new JsonObject().put("name", "Duke"))),
                    "Hello (standard), Duke!");

            JsonObject arguments = new JsonObject().put("food", "pizza");
            for (Map.Entry<String, String> prompt : Map.of("quarkusOrder", "Here is your pizza! (quarkus)",
                    "standardOrder", "Here is your pizza! (standard)").entrySet()) {
                JsonObject response = request(protocol, "prompts/get",
                        new JsonObject().put("name", prompt.getKey()).put("arguments", arguments));
                Assertions.assertEquals(prompt.getValue(), response.getJsonObject("result").getJsonArray("messages")
                        .getJsonObject(0).getJsonObject("content").getString("text"));
            }
        }
    }

    // QUARKUS-8957 - Standard Annotations Scenario: a renamed argument, here on the mixed class
    @Test
    public void declaredArgumentName() {
        // standardGreeter's @ToolArg(name = "name") differs from the Java parameter name ("personName")
        try (McpStreamableTestClient protocol = protocolClient()) {
            JsonObject standardGreeter = allTools(protocol).stream()
                    .filter(tool -> "standardGreeter".equals(tool.getString("name")))
                    .findFirst().orElseThrow(() -> new AssertionError("standardGreeter is not listed"));
            JsonObject properties = standardGreeter.getJsonObject("inputSchema").getJsonObject("properties");
            Assertions.assertTrue(properties.containsKey("name"), () -> "Expected the declared name: " + properties);
            Assertions.assertFalse(properties.containsKey("personName"),
                    () -> "The Java parameter name must not leak into the schema: " + properties);
        }
    }

    // QUARKUS-8957 - Mixed Application Scenario: a standard-annotated tool using sampling
    @Test
    public void sampling() {
        try (McpStreamableTestClient protocol = protocolClient()) {
            JsonObject operation = protocol.newRequest("tools/call").put("params", call("standardSampling", new JsonObject()));
            int count = protocol.snapshot().requests().size();
            protocol.sendAndForget(operation);
            JsonObject sampling = protocol.waitForRequests(count + 1).requests().get(count);
            Assertions.assertEquals("sampling/createMessage", sampling.getString("method"));

            protocol.sendAndForget(new JsonObject().put("jsonrpc", "2.0").put("id", sampling.getValue("id"))
                    .put("result", new JsonObject().put("role", "assistant").put("model", "shmodel")
                            .put("stopReason", "endTurn")
                            .put("content", new JsonObject().put("type", "text").put("text", "pong"))));
            assertText(protocol.waitForResponse(operation), "standard-sampled: pong");
        }
    }

    // QUARKUS-8957 - Mixed Application Scenario: a standard-annotated tool using elicitation
    @Test
    public void elicitation() {
        try (McpStreamableTestClient protocol = protocolClient()) {
            JsonObject operation = protocol.newRequest("tools/call")
                    .put("params", call("standardElicitation", new JsonObject()));
            int count = protocol.snapshot().requests().size();
            protocol.sendAndForget(operation);
            JsonObject elicitation = protocol.waitForRequests(count + 1).requests().get(count);
            Assertions.assertEquals("elicitation/create", elicitation.getString("method"));
            Assertions.assertEquals("Please provide your information:",
                    elicitation.getJsonObject("params").getString("message"));

            protocol.sendAndForget(new JsonObject().put("jsonrpc", "2.0").put("id", elicitation.getValue("id"))
                    .put("result", new JsonObject().put("action", "accept")
                            .put("content", new JsonObject().put("name", "QE client"))));
            assertText(protocol.waitForResponse(operation), "standard-elicited: QE client");
        }
    }

    @Test
    public void completion() {
        try (McpStreamableTestClient protocol = protocolClient()) {
            for (Map.Entry<String, List<String>> entry : Map.of("", List.of("broccoli", "pizza", "salad", "pie"),
                    "pi", List.of("pizza", "pie"), "piz", List.of("pizza")).entrySet()) {
                JsonObject params = new JsonObject()
                        .put("ref", new JsonObject().put("type", "ref/prompt").put("name", "standardOrder"))
                        .put("argument", new JsonObject().put("name", "food").put("value", entry.getKey()));
                Assertions.assertEquals(entry.getValue(),
                        request(protocol, "completion/complete", params).getJsonObject("result")
                                .getJsonObject("completion").getJsonArray("values").getList());
            }
        }
    }

    // QUARKUS-8957 - Mixed Application Scenario: roots
    @Test
    public void roots() {
        try (McpStreamableTestClient protocol = protocolClient()) {
            JsonObject operation = protocol.newRequest("tools/call").put("params", call("standardRoots", new JsonObject()));
            int count = protocol.snapshot().requests().size();
            protocol.sendAndForget(operation);
            JsonObject roots = protocol.waitForRequests(count + 1).requests().get(count);
            Assertions.assertEquals("roots/list", roots.getString("method"));

            protocol.sendAndForget(new JsonObject().put("jsonrpc", "2.0").put("id", roots.getValue("id"))
                    .put("result", new JsonObject().put("roots", new JsonArray()
                            .add(new JsonObject().put("name", "first").put("uri", "file:///workspace")))));
            assertText(protocol.waitForResponse(operation), "first (file:///workspace)");
        }
    }

    // QUARKUS-8957 - Mixed Application Scenario: client logging
    @Test
    public void logging() {
        try (McpStreamableTestClient protocol = protocolClient()) {
            JsonObject operation = protocol.newRequest("tools/call").put("params", call("standardSampling", new JsonObject()));
            int notifications = protocol.snapshot().notifications().size();
            int requests = protocol.snapshot().requests().size();
            protocol.sendAndForget(operation);

            JsonObject log = protocol.waitForNotifications(notifications + 1).notifications().get(notifications);
            Assertions.assertEquals("notifications/message", log.getString("method"));
            Assertions.assertEquals("info", log.getJsonObject("params").getString("level"));
            Assertions.assertEquals("tool:standardSampling", log.getJsonObject("params").getString("logger"));
            Assertions.assertEquals("standardSampling invoked", log.getJsonObject("params").getString("data"));

            // the tool only completes once the sampling request it issued is answered
            JsonObject sampling = protocol.waitForRequests(requests + 1).requests().get(requests);
            protocol.sendAndForget(new JsonObject().put("jsonrpc", "2.0").put("id", sampling.getValue("id"))
                    .put("result", new JsonObject().put("role", "assistant").put("model", "shmodel")
                            .put("stopReason", "endTurn")
                            .put("content", new JsonObject().put("type", "text").put("text", "pong"))));
            protocol.waitForResponse(operation);
        }
    }
}
