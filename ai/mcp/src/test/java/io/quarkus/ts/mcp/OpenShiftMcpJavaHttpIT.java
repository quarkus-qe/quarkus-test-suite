package io.quarkus.ts.mcp;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.McpJavaFileServer;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolObservationsResource;
import io.quarkus.ts.mcp.app.ProtocolObserver;
import io.restassured.specification.RequestSpecification;

@OpenShiftScenario
public class OpenShiftMcpJavaHttpIT extends McpJavaHttpIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { McpJavaFileServer.class, ProtocolObserver.class, ProtocolObservationsResource.class, MyResources.class })
    static final RestService openShiftMcpJavaServer = new RestService()
            .withProperty("_ignored", "resource_with_destination::/deployments|robot-readable.txt")
            .withProperty("working.folder", "/deployments");

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class }, properties = "http-client.properties")
    static final RestService openShiftMcpJavaClient = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.protocol-version", STATEFUL_PROTOCOL_VERSION)
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> openShiftMcpJavaServer.getURI(Protocol.HTTP).withPath("/mcp").toString());

    @Override
    public RequestSpecification client() {
        return openShiftMcpJavaClient.given();
    }

    @Override
    public RestService app() {
        return openShiftMcpJavaClient;
    }

    @Override
    protected RestService serverApp() {
        return openShiftMcpJavaServer;
    }
}
