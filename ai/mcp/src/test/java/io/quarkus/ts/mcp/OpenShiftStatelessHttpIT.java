package io.quarkus.ts.mcp;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.AdvancedServer;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolObservationsResource;
import io.quarkus.ts.mcp.app.ProtocolObserver;
import io.restassured.specification.RequestSpecification;

@OpenShiftScenario
public class OpenShiftStatelessHttpIT extends StatelessStreamingHTTPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { FileServer.class, ProtocolObserver.class, ProtocolObservationsResource.class, MyResources.class,
            AdvancedServer.class })
    static final RestService server = new RestService()
            .withProperty("_ignored", "resource_with_destination::/deployments|robot-readable.txt")
            .withProperty("working.folder", "/deployments");

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperties("mcp-2026-client.properties", "http-client.properties")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server.getURI(Protocol.HTTP).withPath("/mcp").toString());

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService automaticClient = new RestService()
            .withProperties("http-client.properties")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server.getURI(Protocol.HTTP).withPath("/mcp").toString());

    @Override
    public RequestSpecification client() {
        return client.given();
    }

    @Override
    public RestService app() {
        return client;
    }

    @Override
    protected RestService serverApp() {
        return server;
    }

    @Override
    protected RestService automaticApp() {
        return automaticClient;
    }

}
