package io.quarkus.ts.mcp;

import java.nio.file.Path;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MCPServerRestAPI;

@QuarkusScenario
public class LocalStreamingHTTPIT extends BasicMCPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { FileServer.class, MCPServerRestAPI.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> Path.of("target").toAbsolutePath().toString());

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

    @Override
    public RestService client() {
        return client;
    }
}
