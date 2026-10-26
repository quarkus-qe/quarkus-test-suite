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
            .withProperty("quarkus.langchain4j.mcp.filesystem.protocol-version", "2026-07-28")
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "streamable-http")
            // todo remove the line below when https://github.com/quarkiverse/quarkus-langchain4j/issues/2159 is fixed
            .withProperty("quarkus.langchain4j.mcp.filesystem.cache-tool-list", "false")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server.getURI(Protocol.HTTP).withPath("/mcp").toString());
    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService automaticClient = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "streamable-http")
            // todo remove the line below when https://github.com/quarkiverse/quarkus-langchain4j/issues/2159 is fixed
            .withProperty("quarkus.langchain4j.mcp.filesystem.cache-tool-list", "false")
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
    protected McpAssured.McpTestClient<?, ?> protocolClient() {
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
            try (var protocol = McpAssured.newStreamableClient()
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
    public void httpHeadersAndLifecycle() {
        // Discovery succeeds without creating a protocol session (no "Mcp-Session-Id")
        JsonObject discover = rawMessage("server/discover", new JsonObject());
        var response = rawPost(discover, Map.of("MCP-Protocol-Version", REVISION, "Mcp-Method", "server/discover"));
        response.then().statusCode(200);
        Assertions.assertNull(response.header("Mcp-Session-Id"));
        JsonObject discovery = new JsonObject(response.asString());
        Assertions.assertEquals(discover.getValue("id"), discovery.getValue("id"));
        assertComplete(discovery.getJsonObject("result"), true);

        // Reject the older protocol's GET stream and DELETE session lifecycle.
        serverApp().given().header("MCP-Protocol-Version", REVISION).get("/mcp").then().statusCode(405);
        serverApp().given().header("MCP-Protocol-Version", REVISION).delete("/mcp").then().statusCode(405);

        JsonObject call = rawMessage("tools/call", call("filereader", new JsonObject().put("file", "robot-readable.txt")));
        var headers = new HashMap<>(
                Map.of("MCP-Protocol-Version", REVISION, "Mcp-Method", "tools/call", "Mcp-Name", "filereader"));

        // Body metadata does not replace the required HTTP protocol-version header.
        Map<String, String> missingRevision = new HashMap<>(headers);
        missingRevision.remove("MCP-Protocol-Version");
        Response missingRevisionResponse = rawPost(call, missingRevision);
        // TODO: Uncomment the assertion below once https://github.com/quarkiverse/quarkus-mcp-server/issues/1083 is fixed.
        // Currently returns HTTP 200 instead of 400 / -32020
        // assertHttpError(missingRevisionResponse, -32020, "Missing required header: Mcp-Protocol-Version");

        // Reject missing method/name headers and values that disagree with the request body.
        for (String required : List.of("Mcp-Method", "Mcp-Name")) {
            var omitted = new HashMap<>(headers);
            omitted.remove(required);
            assertHttpError(rawPost(call, omitted), -32020, "Missing required header: " + required);
            var mismatched = new HashMap<>(headers);
            mismatched.put(required, "wrong");
            assertHttpError(rawPost(call, mismatched), -32020, "Header mismatch: " + required);
        }
        // Both revisions are supported, but the header and body must request the same one.
        var mismatchedRevision = new HashMap<>(headers);
        mismatchedRevision.put("MCP-Protocol-Version", "2025-11-25");
        assertHttpError(rawPost(call, mismatchedRevision), -32020, "Header mismatch: Mcp-Protocol-Version");

        // Headers do not replace the required protocol and capability metadata in the body.
        for (String key : List.of("io.modelcontextprotocol/protocolVersion",
                "io.modelcontextprotocol/clientCapabilities")) {
            JsonObject missing = discover.copy();
            missing.getJsonObject("params").getJsonObject("_meta").remove(key);
            var invalid = rawPost(missing, Map.of("MCP-Protocol-Version", REVISION, "Mcp-Method", "server/discover"));
            invalid.then().statusCode(400);
            JsonObject error = new JsonObject(invalid.asString());
            Assertions.assertEquals(missing.getValue("id"), error.getValue("id"));
            assertError(error, -32602, key);
        }
        // The newer protocol no longer accepts the initialize handshake.
        JsonObject removed = rawMessage("initialize", new JsonObject().put("protocolVersion", REVISION));
        var removal = rawPost(removed, Map.of("MCP-Protocol-Version", REVISION, "Mcp-Method", "initialize"));
        // TODO: Intentionally not checking returned status code - should be 404 but is 200
        // See https://github.com/quarkiverse/quarkus-mcp-server/issues/1081
        assertError(new JsonObject(removal.asString()), -32601, "initialize");
    }

    @Test
    public void incompatibleRevision() {
        // Discovery with unsupported version in both, header and body
        String revision = "2099-01-01";
        JsonObject discover = rawMessage("server/discover", new JsonObject());
        discover.getJsonObject("params").getJsonObject("_meta").put("io.modelcontextprotocol/protocolVersion", revision);
        var response = rawPost(discover, Map.of("MCP-Protocol-Version", revision, "Mcp-Method", "server/discover"));
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
    protected List<String> serverLogs(McpAssured.McpTestClient<?, ?> protocol) {
        return serverApp().getLogs();
    }

    @Override
    protected SubscriptionObserver subscribe(McpAssured.McpTestClient<?, ?> protocol, JsonObject request) {
        return SubscriptionObserver.openHttp(URI.create(serverApp().getURI(Protocol.HTTP).withPath("/mcp").toString()),
                request);
    }

    @Override
    protected List<JsonObject> protocolObservations() {
        return ProtocolAssertions.observations(serverApp());
    }

}
