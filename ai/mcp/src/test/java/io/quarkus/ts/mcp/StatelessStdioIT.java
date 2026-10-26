package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.app.Utils.getFileFolder;

import java.util.List;

import io.quarkiverse.mcp.server.McpProtocolVersion;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.test.bootstrap.BaseService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.AdvancedServer;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolObserver;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;

@QuarkusScenario
public class StatelessStdioIT extends StatelessAdvancedMCPIT {

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-stdio")
    }, classes = { FileServer.class, ProtocolObserver.class, MyResources.class, AdvancedServer.class })
    static final NeverRunningService server = new NeverRunningService()
            .withProperty("quarkus.profile", "debug")
            .setAutoStart(false);

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperties("mcp-2026-client.properties")
            .withProperty("quarkus.log.category.\"dev.langchain4j.mcp.client.transport.stdio.ProcessStderrHandler\".level",
                    "DEBUG")
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "stdio")
            .withProperty("quarkus.langchain4j.mcp.filesystem.command",
                    () -> "java,-Dqe.mcp.protocol-observer.stderr=true,-Dworking.folder="
                            + makeSafeForWindows(getFileFolder(StatelessStdioIT.class))
                            + ",-jar," + makeSafeForWindows(getJarPath(server)));

    static String getJarPath(BaseService app) {
        return app.getServiceFolder().toAbsolutePath().resolve("mvn-build/target/quarkus-app/quarkus-run.jar").toString();
    }

    private static String makeSafeForWindows(String folder) {
        return folder.replaceAll("\\\\", "/");
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
    protected McpAssured.McpTestClient<?, ?> protocolClient() {
        return McpAssured.newStdioClient()
                .setCommand("java", "-Dworking.folder=" + getFileFolder(StatelessStdioIT.class), "-jar", getJarPath(server))
                .setWorkingDirectory(server.getServiceFolder().toAbsolutePath())
                .setProtocolVersion(McpProtocolVersion.from(REVISION)).setClientCapabilities(CLIENT_CAPABILITIES)
                .build().connect();
    }

    @Override
    protected List<String> serverLogs(McpAssured.McpTestClient<?, ?> protocol) {
        return ((McpAssured.McpStdioTestClient) protocol).stderrLines();
    }

    @Override
    protected List<JsonObject> protocolObservations() {
        return ProtocolAssertions.stderrObservations(app());
    }

}
