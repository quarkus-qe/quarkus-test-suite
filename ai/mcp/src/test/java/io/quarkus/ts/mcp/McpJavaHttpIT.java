package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.app.Utils.getFileFolder;

import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.McpJavaFileServer;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolObservationsResource;
import io.quarkus.ts.mcp.app.ProtocolObserver;
import io.restassured.specification.RequestSpecification;

/**
 * Parity of the {@code org.mcpjava.server} annotations with the Quarkus-native ones, over Streamable HTTP.
 */
@QuarkusScenario
public class McpJavaHttpIT extends LocalStreamingHTTPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { McpJavaFileServer.class, ProtocolObserver.class, ProtocolObservationsResource.class, MyResources.class })
    static final RestService mcpJavaServer = new RestService()
            .withProperty("working.folder", () -> getFileFolder(McpJavaHttpIT.class, "mcpJavaServer"));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class }, properties = "http-client.properties")
    static final RestService mcpJavaClient = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.protocol-version", STATEFUL_PROTOCOL_VERSION)
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> mcpJavaServer.getURI(Protocol.HTTP).withPath("/mcp").toString());

    @Override
    public RequestSpecification client() {
        return mcpJavaClient.given();
    }

    @Override
    public RestService app() {
        return mcpJavaClient;
    }

    @Override
    protected RestService serverApp() {
        return mcpJavaServer;
    }

    // QUARKUS-8957 - Standard Annotations Scenario: the LangChain4j client lists and calls standard-annotated prompts
    @Test
    public void standardAnnotatedPrompt() {
        client().given().get("/mcp/prompts").then().statusCode(200)
                .body(Matchers.containsString("foodOrder"));

        try (McpSyncClient thirdPartyClient = sdkClient()) {
            thirdPartyClient.initialize();
            McpSchema.Prompt foodOrder = thirdPartyClient.listPrompts().prompts().stream()
                    .filter(prompt -> prompt.name().equals("foodOrder"))
                    .findFirst().orElseThrow();
            Assertions.assertEquals("Food ordering", foodOrder.description());
            Assertions.assertEquals(List.of("food"),
                    foodOrder.arguments().stream().map(McpSchema.PromptArgument::name).toList());

            McpSchema.GetPromptResult result = thirdPartyClient.getPrompt(
                    new McpSchema.GetPromptRequest("foodOrder", Map.of("food", "pizza")));
            McpSchema.TextContent text = (McpSchema.TextContent) result.messages().get(0).content();
            Assertions.assertEquals("Here is your pizza!", text.text());
        }
    }
}
