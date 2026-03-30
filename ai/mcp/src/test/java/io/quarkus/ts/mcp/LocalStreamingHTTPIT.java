package io.quarkus.ts.mcp;

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

@QuarkusScenario
public class LocalStreamingHTTPIT extends BasicMCPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { FileServer.class, MyResources.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(LocalStreamingHTTPIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "streamable-http")
            // todo remove the line below when https://github.com/quarkiverse/quarkus-langchain4j/issues/2159 is fixed
            .withProperty("quarkus.langchain4j.mcp.filesystem.cache-tool-list", "false")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server.getURI(Protocol.HTTP).withPath("/mcp").toString());
    private static final int NUMBER_OF_TOOLS = 4; // tools, declared or created in FileServer class
    private final McpSyncClient thirdPartyClient;

    public LocalStreamingHTTPIT() {
        McpClientTransport transport = HttpClientStreamableHttpTransport
                .builder(server.getURI(Protocol.HTTP).toString())
                .endpoint("/mcp")
                .build();
        this.thirdPartyClient = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .capabilities(McpSchema.ClientCapabilities.builder()
                        .build())
                .build();
    }

    @Override
    public RestService client() {
        return client;
    }

    @Test
    public void toolList() {
        thirdPartyClient.initialize();

        McpSchema.ListToolsResult toolsResult = thirdPartyClient.listTools();
        List<McpSchema.Tool> tools = new ArrayList<>(toolsResult.tools());

        String cursor = toolsResult.nextCursor();
        while (cursor != null) {
            toolsResult = thirdPartyClient.listTools(cursor);
            tools.addAll(toolsResult.tools());
            cursor = toolsResult.nextCursor();
        }

        Assertions.assertEquals(NUMBER_OF_TOOLS, tools.size());
    }

    @Test
    public void toolCall() {
        thirdPartyClient.initialize();
        McpSchema.CallToolResult result = thirdPartyClient.callTool(
                new McpSchema.CallToolRequest("filereader",
                        Map.of("file", "robot-readable.txt")));
        Assertions.assertEquals(1, result.content().size());
        McpSchema.Content actual = result.content().get(0);
        Assertions.assertEquals("text", actual.type());
        McpSchema.TextContent text = (McpSchema.TextContent) actual;
        Assertions.assertEquals("Hello, AI!", text.text());
    }
}
