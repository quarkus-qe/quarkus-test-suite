package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.app.Utils.getFileFolder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolClient;
import io.restassured.response.Response;

/**
 * Verifies, that MCP clients with different protocol revisions work side by side in one application over the streamable
 * HTTP transport: one detects the revision (no {@code protocol-version} set), the other is set to {@code 2025-11-25}.
 */
@QuarkusScenario
public class HttpProtocolIT {

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { FileServer.class, MyResources.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(HttpProtocolIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { ProtocolClient.class }, properties = "http-protocol-client.properties")
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.modern.url", HttpProtocolIT::serverUrl)
            .withProperty("quarkus.langchain4j.mcp.pinned.url", HttpProtocolIT::serverUrl);

    private static String serverUrl() {
        return server.getURI(Protocol.HTTP).withPath("/mcp").toString();
    }

    // Protocol version detection: a detecting and a 2025-11-25 client in one application
    @Test
    void mixedVersions() {
        client.given().get("/protocol/modern/modern").then().statusCode(200).body(is("true"));
        client.given().get("/protocol/pinned/modern").then().statusCode(200).body(is("false"));
        String modernTools = client.given().get("/protocol/modern/tools").then().statusCode(200).extract().asString();
        client.given().get("/protocol/pinned/tools").then().statusCode(200).body(is(modernTools));
        assertEquals("Hello, AI!", readFile("modern"));
        assertEquals("Hello, AI!", readFile("pinned"));
    }

    private static String readFile(String mcpClient) {
        Response response = client.given().contentType("text/plain; charset=UTF-8").body("""
                {"file":"robot-readable.txt"}
                """)
                .pathParam("client", mcpClient)
                .pathParam("tool", "filereader")
                .post("/protocol/{client}/tools/{tool}");
        assertEquals(200, response.statusCode(), response.asString());
        return response.asString();
    }
}
