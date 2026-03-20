package io.quarkus.ts.langchain4j;

import static io.quarkus.ts.langchain4j.auxiliary.CommonTools.DEFAULT_ARGS;
import static io.quarkus.ts.langchain4j.auxiliary.CommonTools.getKey;

import java.nio.file.Path;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.langchain4j.auxiliary.MCPFileServer;

@QuarkusScenario
public class ToolsIT extends AbstractToolsIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { MCPFileServer.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(ToolsIT.class));

    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkiverse/quarkus-langchain4j.git", branch = "1.7", contextDir = "samples/mcp-tools", mavenArgs = DEFAULT_ARGS
            + " -Dsamples -Dplatform-deps -Dquarkus.langchain4j.mcp.filesystem.transport-type=streamable-http")
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "streamable-http")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server.getURI(Protocol.HTTP).withPath("/mcp").toString())
            .withProperty("quarkus.langchain4j.openai.api-key", getKey());

    protected static String getFileFolder(Class testClass) {
        return Path.of("target")
                .resolve(testClass.getSimpleName())
                .resolve("server")
                .toAbsolutePath().toString();
    }

    @Override
    protected RestService getServer() {
        return server;
    }

    @Override
    protected RestService getClient() {
        return client;
    }
}
