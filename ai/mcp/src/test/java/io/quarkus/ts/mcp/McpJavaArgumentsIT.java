package io.quarkus.ts.mcp;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.McpJavaArgumentsServer;

@QuarkusScenario
public class McpJavaArgumentsIT {
    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
            }, classes = { McpJavaArgumentsServer.class })
    static final RestService server = new RestService();

    private static McpSyncClient sdkClient() {
        McpSyncClient client = McpClient.sync(HttpClientStreamableHttpTransport
                .builder(server.getURI(Protocol.HTTP).toString())
                .endpoint("/mcp")
                .supportedProtocolVersions(List.of("2025-11-25"))
                .build())
                .requestTimeout(Duration.ofSeconds(10))
                .capabilities(McpSchema.ClientCapabilities.builder().build())
                .build();
        client.initialize();
        return client;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> inputProperties(McpSchema.Tool tool) {
        return (Map<String, Object>) tool.inputSchema().get("properties");
    }

    @SuppressWarnings("unchecked")
    private static List<String> required(McpSchema.Tool tool) {
        Object required = tool.inputSchema().get("required");
        return required == null ? List.of() : (List<String>) required;
    }

    private static McpSchema.Tool tool(McpSyncClient client, String name) {
        return client.listTools().tools().stream()
                .filter(tool -> tool.name().equals(name))
                .findFirst().orElseThrow(() -> new AssertionError("Tool not listed: " + name));
    }

    private static String callText(McpSyncClient client, String name, Map<String, Object> arguments) {
        McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(name, arguments));
        Assertions.assertNotEquals(Boolean.TRUE, result.isError(), () -> "Tool call failed: " + result.content());
        return ((McpSchema.TextContent) result.content().get(0)).text();
    }

    // QUARKUS-8957 - Standard Annotations Scenario: a renamed argument
    @Test
    public void renamedArgument() {
        try (McpSyncClient client = sdkClient()) {
            Map<String, Object> properties = inputProperties(tool(client, "renamedArgument"));
            Assertions.assertTrue(properties.containsKey("city"), () -> "Expected the declared name: " + properties);
            Assertions.assertFalse(properties.containsKey("someParameterName"),
                    () -> "The Java parameter name must not leak into the schema: " + properties);

            Assertions.assertEquals("city=Prague", callText(client, "renamedArgument", Map.of("city", "Prague")));
        }
    }

    // QUARKUS-8957 - Standard Annotations Scenario: an optional argument
    @Test
    public void optionalArgument() {
        try (McpSyncClient client = sdkClient()) {
            Assertions.assertEquals(List.of(), required(tool(client, "optionalArgument")),
                    "An optional argument must not be listed as required");

            Assertions.assertEquals("stranger", callText(client, "optionalArgument", Map.of()));
            Assertions.assertEquals("Dukey", callText(client, "optionalArgument", Map.of("nickname", "Dukey")));
        }
    }

    // QUARKUS-8957 - Standard Annotations Scenario: an argument with a default value
    @Test
    @SuppressWarnings("unchecked")
    public void defaultValueArgument() {
        try (McpSyncClient client = sdkClient()) {
            McpSchema.Tool tool = tool(client, "defaultValueArgument");
            Assertions.assertEquals(List.of("name"), required(tool),
                    "Only the argument without a default value is required");
            Map<String, Object> greeting = (Map<String, Object>) inputProperties(tool).get("greeting");
            Assertions.assertEquals("Hello", greeting.get("default"), () -> "Missing default in the schema: " + greeting);

            Assertions.assertEquals("Hello, Duke!", callText(client, "defaultValueArgument", Map.of("name", "Duke")));
            Assertions.assertEquals("Hi, Duke!",
                    callText(client, "defaultValueArgument", Map.of("greeting", "Hi", "name", "Duke")));
        }
    }
}
