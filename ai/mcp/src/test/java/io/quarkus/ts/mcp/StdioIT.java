package io.quarkus.ts.mcp;

import java.nio.file.Path;

import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.BaseService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MyResources;

@QuarkusScenario
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "https://github.com/quarkiverse/quarkus-langchain4j/issues/2230")
public class StdioIT extends BasicMCPIT {

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-stdio")
    }, classes = { FileServer.class, MyResources.class })
    static final NeverRunningService server = new NeverRunningService()
            .setAutoStart(false);

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "stdio")
            .withProperty("quarkus.langchain4j.mcp.filesystem.command",
                    () -> "java,-Dworking.folder=" + Path.of("target").toAbsolutePath() + ",-jar," + getJarPath(server));

    static String getJarPath(BaseService app) {
        return app.getServiceFolder().toAbsolutePath().resolve("mvn-build/target/quarkus-app/quarkus-run.jar").toString();
    }

    @Override
    public RestService client() {
        return client;
    }
}

class NeverRunningService extends BaseService<NeverRunningService> {

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
