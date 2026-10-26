package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.app.Utils.getFileFolder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolObservationsResource;
import io.quarkus.ts.mcp.app.ProtocolObserver;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;

@QuarkusScenario
public class LocalStreamingHTTPIT extends StatefulMCPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { FileServer.class, ProtocolObserver.class, ProtocolObservationsResource.class, MyResources.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(LocalStreamingHTTPIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.protocol-version", "2025-11-25")
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "streamable-http")
            // todo remove the line below when https://github.com/quarkiverse/quarkus-langchain4j/issues/2159 is fixed
            .withProperty("quarkus.langchain4j.mcp.filesystem.cache-tool-list", "false")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server.getURI(Protocol.HTTP).withPath("/mcp").toString());
    private static final int NUMBER_OF_TOOLS = 4; // tools, declared or created in FileServer class

    protected RestService serverApp() {
        return server;
    }

    // Java SDK coverage stays on MCP 2025-11-25: SDK 2.0.1 does not support 2026-07-28.
    // Extend these independent checks once a released SDK supports the newer protocol.
    private McpSyncClient sdkClient() {
        McpClientTransport transport = HttpClientStreamableHttpTransport
                .builder(serverApp().getURI(Protocol.HTTP).toString())
                .endpoint("/mcp")
                .supportedProtocolVersions(List.of("2025-11-25"))
                .build();
        return McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .capabilities(McpSchema.ClientCapabilities.builder().build())
                .build();
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
    public void toolList() {
        try (McpSyncClient thirdPartyClient = sdkClient()) {
            Assertions.assertEquals("2025-11-25", thirdPartyClient.initialize().protocolVersion());

            McpSchema.ListToolsResult toolsResult = thirdPartyClient.listTools();
            List<McpSchema.Tool> tools = new ArrayList<>(toolsResult.tools());

            String cursor = toolsResult.nextCursor();
            for (int page = 1; cursor != null && page < NUMBER_OF_TOOLS; page++) {
                toolsResult = thirdPartyClient.listTools(cursor);
                tools.addAll(toolsResult.tools());
                cursor = toolsResult.nextCursor();
            }

            Assertions.assertNull(cursor, "Fixture tool listing did not terminate");
            Assertions.assertEquals(NUMBER_OF_TOOLS, tools.size());
        }
    }

    @Test
    public void toolCall() {
        try (McpSyncClient thirdPartyClient = sdkClient()) {
            Assertions.assertEquals("2025-11-25", thirdPartyClient.initialize().protocolVersion());
            McpSchema.CallToolResult result = thirdPartyClient.callTool(
                    new McpSchema.CallToolRequest("filereader",
                            Map.of("file", "robot-readable.txt")));
            Assertions.assertFalse(Boolean.TRUE.equals(result.isError()));
            Assertions.assertEquals(1, result.content().size());
            McpSchema.Content actual = result.content().get(0);
            Assertions.assertEquals("text", actual.type());
            McpSchema.TextContent text = (McpSchema.TextContent) actual;
            Assertions.assertEquals("Hello, AI!", text.text());
        }
    }

    @Override
    protected List<JsonObject> protocolObservations() {
        return ProtocolAssertions.observations(serverApp());
    }

}
