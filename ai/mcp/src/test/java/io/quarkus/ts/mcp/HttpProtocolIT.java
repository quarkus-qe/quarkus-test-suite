package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.app.Utils.getFileFolder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.ts.mcp.McpServerMock.Mode;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolClient;
import io.quarkus.ts.mcp.app.ProtocolServer;
import io.restassured.response.Response;

/**
 * Verifies the behavior of the MCP client over the streamable HTTP transport, which depends on the protocol revision:
 * protocol detection (no {@code protocol-version} set), clients with different revisions in one application, tool
 * timeout with cancellation and multi round-trip requests.
 */
@QuarkusScenario
public class HttpProtocolIT {
    // the default initialization timeout of the LangChain4j client, Quarkus does not allow changing it
    private static final Duration INITIALIZATION_TIMEOUT = Duration.ofSeconds(30);

    static final McpServerMock mcpServerMock = new McpServerMock();

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { FileServer.class, MyResources.class, ProtocolServer.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(HttpProtocolIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { ProtocolClient.class }, properties = "http-protocol-client.properties")
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.modern.url", HttpProtocolIT::serverUrl)
            .withProperty("quarkus.langchain4j.mcp.pinned.url", HttpProtocolIT::serverUrl)
            .withProperty("quarkus.langchain4j.mcp.timeout.url", HttpProtocolIT::serverUrl)
            .withProperty("quarkus.langchain4j.mcp.legacy-timeout.url", HttpProtocolIT::serverUrl)
            .withProperty("quarkus.langchain4j.mcp.legacy.url", () -> mcpServerMock.url(Mode.LEGACY))
            .withProperty("quarkus.langchain4j.mcp.silent-discover.url", () -> mcpServerMock.url(Mode.SILENT_DISCOVER))
            .withProperty("quarkus.langchain4j.mcp.silent.url", () -> mcpServerMock.url(Mode.SILENT))
            .withProperty("quarkus.langchain4j.mcp.intolerant.url", () -> mcpServerMock.url(Mode.INTOLERANT))
            .withProperty("quarkus.langchain4j.mcp.server-error.url", () -> mcpServerMock.url(Mode.SERVER_ERROR));

    private static String serverUrl() {
        return server.getURI(Protocol.HTTP).withPath("/mcp").toString();
    }

    @BeforeAll
    static void startMcpServerMock() {
        mcpServerMock.start();
    }

    @AfterAll
    static void stopMcpServerMock() {
        mcpServerMock.close();
    }

    // Scenario 1, default protocol detection: a real 2.x server is detected as 2026-07-28
    @Test
    void modernServerIsDetected() {
        client.given().get("/protocol/modern/modern").then().statusCode(200).body(is("true"));
        client.given().get("/protocol/modern/tools").then().statusCode(200).body(containsString("filereader"));
        assertEquals("Hello, AI!", readFile("modern"));
    }

    // Scenario 1, default protocol detection: a legacy-only server falls back to 2025-11-25
    @Test
    void legacyServerFallsBack() {
        client.given().get("/protocol/legacy/modern").then().statusCode(200).body(is("false"));
        client.given().get("/protocol/legacy/tools").then().statusCode(200).body(is(McpServerMock.LEGACY_TOOL));
        assertEquals(McpServerMock.LEGACY_TOOL_RESULT, executeTool("legacy", McpServerMock.LEGACY_TOOL, "{}").asString());
    }

    // Scenario 1, default protocol detection: mixed versions in one application
    @Test
    void mixedVersions() {
        client.given().get("/protocol/modern/modern").then().statusCode(200).body(is("true"));
        client.given().get("/protocol/pinned/modern").then().statusCode(200).body(is("false"));
        String modernTools = client.given().get("/protocol/modern/tools").then().statusCode(200).extract().asString();
        client.given().get("/protocol/pinned/tools").then().statusCode(200).body(is(modernTools));
        assertEquals("Hello, AI!", readFile("modern"));
        assertEquals("Hello, AI!", readFile("pinned"));
    }

    // Scenario 1, default protocol detection: a server ignoring server/discover
    @Test
    void silentServerCostsDetectionTimeout() {
        long start = System.nanoTime();
        client.given().get("/protocol/silent-discover/modern").then().statusCode(200).body(is("false"));
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertTrue(elapsed.compareTo(INITIALIZATION_TIMEOUT) >= 0, "Detection took only " + elapsed);
        // the 2025-11-25 initialization must follow the detection timeout right away
        assertTrue(elapsed.compareTo(INITIALIZATION_TIMEOUT.plusSeconds(10)) < 0, "Detection took " + elapsed);
        client.logs().assertContains("The server did not answer the 2026-07-28 protocol detection request within PT30S"
                + ", so it is treated as a 2025-11-25 server");

        assertEquals(McpServerMock.LEGACY_TOOL_RESULT,
                executeTool("silent-discover", McpServerMock.LEGACY_TOOL, "{}").asString());
    }

    // Scenario 1, default protocol detection: a server answering neither server/discover nor initialize
    @Test
    void completelySilentServerFails() {
        long start = System.nanoTime();
        Response response = client.given().get("/protocol/silent/modern");
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertEquals(500, response.statusCode());
        // the detection and the 2025-11-25 initialization time out one after the other
        Duration bothTimeouts = INITIALIZATION_TIMEOUT.multipliedBy(2);
        assertTrue(elapsed.compareTo(bothTimeouts) >= 0, "The failure came after only " + elapsed);
        assertTrue(elapsed.compareTo(bothTimeouts.plusSeconds(10)) < 0, "The failure came after " + elapsed);
        assertTrue(response.asString().contains(
                "The server answered neither the 2026-07-28 protocol detection request nor the 2025-11-25 initialization"),
                response.asString());
        assertTrue(response.asString().contains("TimeoutException"), response.asString());
    }

    // Scenario 1, default protocol detection: an intolerant server, which drops the connection on server/discover
    @Test
    void droppedConnectionFallsBack() {
        // over HTTP every request is independent, so the server is still asked to initialize
        client.given().get("/protocol/intolerant/modern").then().statusCode(200).body(is("false"));
        assertEquals(McpServerMock.LEGACY_TOOL_RESULT,
                executeTool("intolerant", McpServerMock.LEGACY_TOOL, "{}").asString());
    }

    // Scenario 1, default protocol detection: an intolerant server, which answers server/discover with HTTP 500
    @Test
    void serverErrorFallsBack() {
        client.given().get("/protocol/server-error/modern").then().statusCode(200).body(is("false"));
        assertEquals(McpServerMock.LEGACY_TOOL_RESULT,
                executeTool("server-error", McpServerMock.LEGACY_TOOL, "{}").asString());
    }

    // Scenario 1, default protocol detection: the cause of a fallback after a dropped connection is visible
    @Test
    @Disabled("Current behaviour: a detection request failing with a dropped connection silently falls back to"
            + " 2025-11-25, and the cause is logged only at debug level. Expected: the spec defines the fallback only for"
            + " a 4xx without a modern error body, so a fallback for any other failure is visible in the log. See"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/versioning#compatibility-matrix,"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/transports/streamable-http#backward-compatibility")
    void droppedConnectionFallbackIsVisible() {
        client.given().get("/protocol/intolerant/modern").then().statusCode(200).body(is("false"));
        assertTrue(client.getLogs().stream()
                .anyMatch(line -> line.contains("WARN") && line.contains("intolerant")), "Fallback not logged");
    }

    // Scenario 1, default protocol detection: the cause of a fallback after an HTTP 500 is visible
    @Test
    @Disabled("Current behaviour: a detection request answered with HTTP 500 silently falls back to 2025-11-25, and the"
            + " cause is logged only at debug level. Expected: the spec defines the fallback only for a 4xx without a"
            + " modern error body, so a fallback for any other failure is visible in the log. See"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/versioning#compatibility-matrix,"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/transports/streamable-http#backward-compatibility")
    void serverErrorFallbackIsVisible() {
        client.given().get("/protocol/server-error/modern").then().statusCode(200).body(is("false"));
        assertTrue(client.getLogs().stream()
                .anyMatch(line -> line.contains("WARN") && line.contains("server-error") && line.contains("500")),
                "Fallback not logged with its cause");
    }

    // Scenario 5, timeout and cancellation: HTTP with 2026-07-28
    @Test
    @Disabled("Current behaviour: with 2026-07-28 the client closes the request on a timeout only for an SSE response, and"
            + " the server does not treat a closed request as cancellation, so the server keeps running the tool."
            + " Expected: the client closes the request on a timeout (SHOULD) and the server treats the disconnect as"
            + " cancellation (MUST), per spec basic/patterns/cancellation. See"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/patterns/cancellation#transport-specific-cancellation,"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/patterns/cancellation#timeouts")
    void modernToolTimeoutCancelsExecution() {
        client.given().get("/protocol/timeout/modern").then().statusCode(200).body(is("true"));
        assertTimeoutCancelsSlowTool("timeout");
    }

    // Scenario 5, timeout and cancellation: HTTP with 2025-11-25
    @Test
    void legacyToolTimeoutCancelsExecution() {
        client.given().get("/protocol/legacy-timeout/modern").then().statusCode(200).body(is("false"));
        assertTimeoutCancelsSlowTool("legacy-timeout");
    }

    // Scenario 6, multi round-trip retry: requestState only
    @Test
    void multiRoundTripRetryCompletes() {
        assertEquals("Completed with state first-round", executeTool("modern", "mrtr-state", "{}").asString());
    }

    // Scenario 6, multi round-trip retry: inputRequests
    @Test
    void multiRoundTripWithInputRequestsFails() {
        Response response = executeTool("modern", "mrtr-input", "{}");
        assertEquals(500, response.statusCode());
        assertTrue(response.asString().contains("Server sent inputRequests that the client cannot handle"),
                response.asString());
    }

    // Scenario 6, multi round-trip retry: never completes
    @Test
    void multiRoundTripRetriesAreLimited() {
        Response response = executeTool("modern", "mrtr-forever", "{}");
        assertEquals(500, response.statusCode());
        assertTrue(response.asString().contains("Multi round-trip retry limit exceeded for tools/call"),
                response.asString());
        // the default limit of the LangChain4j client, Quarkus does not allow changing it
        assertEquals("3", executeTool("modern", "mrtr-forever-retries", "{}").asString());
    }

    private static Response executeTool(String mcpClient, String tool, String arguments) {
        return client.given().contentType("text/plain; charset=UTF-8").body(arguments)
                .pathParam("client", mcpClient)
                .pathParam("tool", tool)
                .post("/protocol/{client}/tools/{tool}");
    }

    private static String readFile(String mcpClient) {
        Response response = executeTool(mcpClient, "filereader", """
                {"file":"robot-readable.txt"}
                """);
        assertEquals(200, response.statusCode(), response.asString());
        return response.asString();
    }

    /**
     * Calls a tool, which runs longer than the tool execution timeout of the client, and checks, that the client gives up
     * in time, that the server is told to stop working on the call, and that the client is still usable afterwards.
     */
    private static void assertTimeoutCancelsSlowTool(String mcpClient) {
        assertEquals(ProtocolServer.IDLE, executeTool(mcpClient, "slow-reset", "{}").asString());

        long start = System.nanoTime();
        Response response = executeTool(mcpClient, "slow", """
                {"seconds":10}
                """);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertEquals(200, response.statusCode(), response.asString());
        assertEquals("Error: There was a timeout executing the tool", response.asString());
        // tool timeout is 2s from quarkus.langchain4j.mcp.timeout.tool-execution-timeout
        assertTrue(elapsed.compareTo(Duration.ofSeconds(5)) < 0, "The timeout came after " + elapsed);

        AwaitilityUtils.untilAsserted(
                () -> assertEquals(ProtocolServer.CANCELLED, executeTool(mcpClient, "slow-status", "{}").asString(),
                        "The server did not stop working on the tool call after the client timed out"),
                AwaitilityUtils.AwaitilitySettings.usingTimeout(Duration.ofSeconds(10)));
    }
}
