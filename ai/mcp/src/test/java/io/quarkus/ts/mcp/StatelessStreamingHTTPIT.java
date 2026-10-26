package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.WebSocketIT.call;
import static io.quarkus.ts.mcp.app.Utils.getFileFolder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkiverse.mcp.server.McpProtocolVersion;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkiverse.mcp.server.test.McpAssured.McpTestClient;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.AdvancedServer;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolObservationsResource;
import io.quarkus.ts.mcp.app.ProtocolObserver;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;

@QuarkusScenario
public class StatelessStreamingHTTPIT extends StatelessAdvancedMCPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { FileServer.class, ProtocolObserver.class, ProtocolObservationsResource.class, MyResources.class,
            AdvancedServer.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(StatelessStreamingHTTPIT.class));

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

    protected RestService automaticApp() {
        return automaticClient;
    }

    protected RestService serverApp() {
        return server;
    }

    @Override
    protected McpTestClient<?, ?> protocolClient() {
        return McpAssured.newStreamableClient()
                .setBaseUri(URI.create(serverApp().getURI(Protocol.HTTP).toString()))
                .setProtocolVersion(McpProtocolVersion.from(REVISION))
                .setClientCapabilities(CLIENT_CAPABILITIES)
                .build().connect();
    }

    @Test
    public void automaticSelection() {
        // The unpinned LangChain4j client must select the newer revision and complete a file read.
        automaticApp().given().body("robot-readable.txt").post("/mcp/tools/readFile").then().statusCode(200)
                .body(Matchers.equalTo("Hello, AI!"));
        ProtocolAssertions.assertStateless(() -> ProtocolAssertions.observations(serverApp()), REVISION);
    }

    @Test
    public void supportedRevisions() {
        // Smoke-test every advertised revision against the same server, using McpAssured.
        for (String revision : ADVERTISED_REVISIONS) {
            try (McpStreamableTestClient protocol = McpAssured.newStreamableClient()
                    .setBaseUri(URI.create(serverApp().getURI(Protocol.HTTP).toString()))
                    .setProtocolVersion(McpProtocolVersion.from(revision)).build().connect()) {
                try {
                    // Newer discovery creates no session; older initialization negotiates a revision and session.
                    if (REVISION.equals(revision)) {
                        Assertions.assertNull(protocol.initResult().protocolVersion());
                        Assertions.assertNull(protocol.mcpSessionId());
                    } else {
                        Assertions.assertEquals(revision, protocol.initResult().protocolVersion());
                        Assertions.assertNotNull(protocol.mcpSessionId());
                    }
                    protocol.when().toolsCall("filereader", Map.of("file", "robot-readable.txt"), response -> {
                        Assertions.assertFalse(response.isError());
                        Assertions.assertEquals("Hello, AI!", response.firstContent().asText().text());
                    }).thenAssertResults();
                } finally {
                    if (!REVISION.equals(revision)) {
                        // Terminate each older session and verify that the server no longer accepts it.
                        String sessionId = protocol.mcpSessionId();
                        protocol.terminateSession();
                        serverApp().given().header("MCP-Protocol-Version", revision)
                                .header("Mcp-Session-Id", sessionId).get("/mcp").then().statusCode(404);
                    }
                }
            }
        }
    }

    @Test
    public void discoveryWithoutSession() {
        JsonObject discover = rawMessage("server/discover", new JsonObject());
        Response response = rawPost(discover, Map.of("MCP-Protocol-Version", REVISION, "Mcp-Method", "server/discover"));
        response.then().statusCode(200);
        Assertions.assertNull(response.header("Mcp-Session-Id"));
        Assertions.assertNotNull(new JsonObject(response.asString()).getJsonObject("result"), response::asString);
    }

    @Test
    public void formerSessionLifecycle() {
        // Newer HTTP has neither the older GET stream nor DELETE session lifecycle.
        serverApp().given().header("MCP-Protocol-Version", REVISION).get("/mcp").then().statusCode(405);
        serverApp().given().header("MCP-Protocol-Version", REVISION).delete("/mcp").then().statusCode(405);
    }

    @Test
    public void requiredHttpHeaders() {
        JsonObject request = rawMessage("tools/call", call("filereader", new JsonObject().put("file", "robot-readable.txt")));
        Map<String, String> headers = Map.of("MCP-Protocol-Version", REVISION, "Mcp-Method", "tools/call", "Mcp-Name",
                "filereader");
        Map<String, String> missingRevision = new HashMap<>(headers);
        missingRevision.remove("MCP-Protocol-Version");
        Response missingRevisionResponse = rawPost(request, missingRevision);
        // TODO: Uncomment the assertion below once https://github.com/quarkiverse/quarkus-mcp-server/issues/1083 is fixed.
        // Currently returns HTTP 200 instead of 400 / -32020
        // assertHttpError(missingRevisionResponse, -32020, "Missing required header: Mcp-Protocol-Version");
        for (String required : List.of("Mcp-Method", "Mcp-Name")) {
            Map<String, String> omitted = new HashMap<>(headers);
            omitted.remove(required);
            assertHttpError(rawPost(request, omitted), -32020, "Missing required header: " + required);
            Map<String, String> mismatched = new HashMap<>(headers);
            mismatched.put(required, "wrong");
            assertHttpError(rawPost(request, mismatched), -32020, "Header mismatch: " + required);
        }
    }

    @Test
    public void headerBodyRevisionMismatch() {
        // Both revisions are supported, but the header and body must request the same one.
        JsonObject request = rawMessage("tools/call", call("filereader", new JsonObject().put("file", "robot-readable.txt")));
        assertHttpError(
                rawPost(request,
                        Map.of("MCP-Protocol-Version", "2025-11-25", "Mcp-Method", "tools/call", "Mcp-Name", "filereader")),
                -32020, "Header mismatch: Mcp-Protocol-Version");
    }

    @Test
    public void requiredBodyMetadata() {
        // Headers do not replace the required protocol and capability metadata in the body.
        for (String key : List.of("io.modelcontextprotocol/protocolVersion", "io.modelcontextprotocol/clientCapabilities")) {
            JsonObject missing = rawMessage("server/discover", new JsonObject());
            missing.getJsonObject("params").getJsonObject("_meta").remove(key);
            Response response = rawPost(missing, Map.of("MCP-Protocol-Version", REVISION, "Mcp-Method", "server/discover"));
            response.then().statusCode(400);
            JsonObject error = new JsonObject(response.asString());
            Assertions.assertEquals(missing.getValue("id"), error.getValue("id"));
            assertError(error, -32602, key);
        }
    }

    @Test
    public void removedInitialize() {
        JsonObject removed = rawMessage("initialize", new JsonObject().put("protocolVersion", REVISION));
        Response response = rawPost(removed, Map.of("MCP-Protocol-Version", REVISION, "Mcp-Method", "initialize"));
        // TODO: Intentionally not checking returned status code - should be 404 but is 200
        // See https://github.com/quarkiverse/quarkus-mcp-server/issues/1081
        assertError(new JsonObject(response.asString()), -32601, "initialize");
    }

    @Test
    public void incompatibleRevision() {
        // Discovery with unsupported version in both, header and body
        String revision = "2099-01-01";
        JsonObject discover = rawMessage("server/discover", new JsonObject());
        discover.getJsonObject("params").getJsonObject("_meta").put("io.modelcontextprotocol/protocolVersion", revision);
        Response response = rawPost(discover, Map.of("MCP-Protocol-Version", revision, "Mcp-Method", "server/discover"));
        response.then().statusCode(400);
        JsonObject error = new JsonObject(response.asString());
        // Check for spec-defined code for unsupported protocol version
        assertError(error, -32022, "Unsupported protocol version");
        // The error identifies the rejected revision and lists supported alternatives.
        Assertions.assertEquals(revision, error.getJsonObject("error").getJsonObject("data").getString("requested"));
        Assertions.assertEquals(ADVERTISED_REVISIONS,
                error.getJsonObject("error").getJsonObject("data").getJsonArray("supported").getList());
    }

    private JsonObject rawMessage(String method, JsonObject params) {
        JsonObject request = new JsonObject().put("jsonrpc", "2.0").put("id", 100).put("method", method).put("params", params);
        McpAssured.injectStatelessMeta(request);
        return request;
    }

    private Response rawPost(JsonObject request, Map<String, String> headers) {
        return serverApp().given().contentType("application/json").accept("application/json,text/event-stream")
                .headers(headers).body(request.encode()).post("/mcp");
    }

    private void assertHttpError(Response response, int code, String message) {
        response.then().statusCode(400);
        JsonObject error = new JsonObject(response.asString());
        Assertions.assertEquals("2.0", error.getString("jsonrpc"));
        Assertions.assertNull(error.getValue("id"));
        assertError(error, code, message);
    }

    @Override
    protected List<String> serverLogs(McpTestClient<?, ?> protocol) {
        return serverApp().getLogs();
    }

    @Override
    protected SubscriptionObserver subscribe(McpTestClient<?, ?> protocol, JsonObject request) {
        return SubscriptionObserver.openHttp(URI.create(serverApp().getURI(Protocol.HTTP).withPath("/mcp").toString()),
                request);
    }

    @Override
    protected List<JsonObject> protocolObservations() {
        return ProtocolAssertions.observations(serverApp());
    }

}
