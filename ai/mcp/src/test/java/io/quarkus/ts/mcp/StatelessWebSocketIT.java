package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.app.Utils.getFileFolder;

import java.net.URI;
import java.util.List;

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
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class StatelessWebSocketIT extends StatelessAdvancedMCPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-websocket")
    }, classes = { FileServer.class, MyResources.class, AdvancedServer.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(StatelessWebSocketIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.protocol-version", "2026-07-28")
            .withProperty("quarkus.langchain4j.mcp.filesystem.log-responses", "true")
            .withProperty("quarkus.langchain4j.mcp.filesystem.log-requests", "true")
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "websocket")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> getUrl(server));

    protected static String getUrl(RestService service) {
        return service.getURI(Protocol.WS).withPath("/mcp/ws").toString();
    }

    protected RestService serverApp() {
        return server;
    }

    @Override
    protected McpAssured.McpTestClient<?, ?> protocolClient() {
        return McpAssured.newWebSocketClient()
                .setBaseUri(URI.create(serverApp().getURI(Protocol.HTTP).toString()))
                .setEndpointPath("/mcp/ws").setProtocolVersion(McpProtocolVersion.from(REVISION))
                .setClientCapabilities(CLIENT_CAPABILITIES).build().connect();
    }

    @Override
    public RequestSpecification client() {
        return client.given();
    }

    @Override
    public RestService app() {
        return client;
    }

    @Override
    protected List<String> serverLogs(McpAssured.McpTestClient<?, ?> protocol) {
        return serverApp().getLogs();
    }

}
