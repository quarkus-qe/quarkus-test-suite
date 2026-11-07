package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.StdioIT.getJarPath;
import static io.quarkus.ts.mcp.app.Utils.getFileFolder;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.ts.mcp.McpServerMock.Mode;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.McpClients;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolClient;
import io.quarkus.ts.mcp.app.ProtocolServer;
import io.restassured.response.Response;

/**
 * Verifies the behavior of the MCP client over the stdio transport, which depends on the protocol revision: protocol
 * detection, resource subscriptions, list-change subscriptions and tool timeout with cancellation.
 * <p>
 * Every MCP client spawns its own server process, so a client only sees changes made through itself.
 */
@QuarkusScenario
public class StdioProtocolIT {

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-stdio")
    }, classes = { FileServer.class, MyResources.class, ProtocolServer.class })
    static final NeverRunningService server = new NeverRunningService()
            .setAutoStart(false);

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { ProtocolClient.class, McpClients.class }, properties = "stdio-protocol-client.properties")
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.modern.command", StdioProtocolIT::serverCommand)
            .withProperty("quarkus.langchain4j.mcp.static.command", StdioProtocolIT::serverCommand)
            .withProperty("quarkus.langchain4j.mcp.timeout.command", StdioProtocolIT::serverCommand)
            .withProperty("quarkus.langchain4j.mcp.legacy-timeout.command", StdioProtocolIT::serverCommand)
            .withProperty("quarkus.langchain4j.mcp.pinned.command", StdioProtocolIT::serverCommand)
            .withProperty("quarkus.langchain4j.mcp.legacy.command", () -> McpServerMock.stdioCommand(Mode.LEGACY))
            .withProperty("quarkus.langchain4j.mcp.intolerant.command", () -> McpServerMock.stdioCommand(Mode.INTOLERANT))
            .withProperty("quarkus.langchain4j.mcp.intolerant-restart.command",
                    () -> McpServerMock.stdioCommand(Mode.INTOLERANT))
            .withProperty("quarkus.langchain4j.mcp.intolerant-pinned.command",
                    () -> McpServerMock.stdioCommand(Mode.INTOLERANT));

    private static String serverCommand() {
        return "java,-Dworking.folder=" + getFileFolder(StdioProtocolIT.class).replaceAll("\\\\", "/")
                + ",-jar," + getJarPath(server).replaceAll("\\\\", "/");
    }

    @AfterAll
    static void stopMcpServers() {
        // otherwise every stdio process still running adds a minute to stopping the client
        client.given().delete("/mcp-clients").then().statusCode(204);
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

    // Scenario 1, default protocol detection: an intolerant server, which exits on server/discover
    @Test
    void intolerantServerFails() {
        // current behaviour, delete this test once intolerantServerIsRestarted is enabled
        Response response = client.given().get("/protocol/intolerant/modern");
        assertEquals(500, response.statusCode());
        assertTrue(response.asString().contains("The server answered neither the 2026-07-28 protocol detection request"
                + " nor the 2025-11-25 initialization"), response.asString());
        assertTrue(response.asString().contains("skip detection by setting the protocol version explicitly"),
                response.asString());
    }

    // Scenario 1, default protocol detection: an intolerant server, which exits on server/discover, is restarted
    @Test
    @Disabled("Current behaviour: when the server process exits on server/discover, the client sends the 2025-11-25"
            + " initialization to the exited process and fails. Expected: the client restarts the process (SHOULD) and"
            + " falls back to initialize, since any error other than a modern one identifies a legacy server. See"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/transports/stdio#unexpected-termination,"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/transports/stdio#backward-compatibility")
    void intolerantServerIsRestarted() {
        client.given().get("/protocol/intolerant-restart/modern").then().statusCode(200).body(is("false"));
        assertEquals(McpServerMock.LEGACY_TOOL_RESULT,
                executeTool("intolerant-restart", McpServerMock.LEGACY_TOOL, "{}").asString());
    }

    // Scenario 1, default protocol detection: protocol-version=2025-11-25 restores the connection to an intolerant server
    @Test
    void intolerantServerWorksWithPinnedVersion() {
        client.given().get("/protocol/intolerant-pinned/modern").then().statusCode(200).body(is("false"));
        assertEquals(McpServerMock.LEGACY_TOOL_RESULT,
                executeTool("intolerant-pinned", McpServerMock.LEGACY_TOOL, "{}").asString());
    }

    // Scenario 4, subscriptions: updates for several URIs arrive until the client unsubscribes
    @Test
    void resourceSubscriptions() {
        client.given().pathParam("client", "modern").get("/protocol/{client}/modern").then().statusCode(200)
                .body(is("true"));
        client.given().pathParam("client", "modern").delete("/protocol/{client}/updates").then().statusCode(204);

        Response subscription = client.given().pathParam("client", "modern")
                .body(FileServer.DYNAMIC_FILE + "," + FileServer.HELLO_FILE)
                .post("/protocol/{client}/subscriptions");
        assertEquals(200, subscription.statusCode(), subscription.asString());

        runMeta("modern", "resource", FileServer.DYNAMIC_FILE, "update");
        runMeta("modern", "resource", FileServer.HELLO_FILE, "update");
        AwaitilityUtils.untilAsserted(() -> client.given().pathParam("client", "modern").get("/protocol/{client}/updates")
                .then().statusCode(200)
                .body(allOf(containsString(FileServer.DYNAMIC_FILE), containsString(FileServer.HELLO_FILE))));

        client.given().pathParam("client", "modern").pathParam("id", subscription.asString())
                .delete("/protocol/{client}/subscriptions/{id}").then().statusCode(204);
        client.given().pathParam("client", "modern").delete("/protocol/{client}/updates").then().statusCode(204);
        runMeta("modern", "resource", FileServer.DYNAMIC_FILE, "update");
        runMeta("modern", "resource", FileServer.HELLO_FILE, "update");
        waitForNotifications();
        client.given().pathParam("client", "modern").get("/protocol/{client}/updates").then().statusCode(200)
                .body(is(""));
    }

    // Scenario 4, subscriptions: deprecated methods and setRoots throw on a modern server
    @Test
    void deprecatedMethodsFailOnModernServer() {
        client.given().get("/protocol/modern/modern").then().statusCode(200).body(is("true"));
        for (String operation : List.of("subscribeToResource", "unsubscribeFromResource", "setRoots")) {
            Response response = client.given().pathParam("operation", operation)
                    .post("/protocol/modern/deprecated/{operation}");
            assertEquals(500, response.statusCode(), operation + " did not fail");
            assertTrue(response.asString().contains("java.lang.UnsupportedOperationException"), response.asString());
            assertTrue(response.asString().contains("2026-07-28"), response.asString());
        }
    }

    // Scenario 4, subscriptions: subscribeToResources throws on a legacy server
    @Test
    void resourceSubscriptionsFailOnLegacyServer() {
        client.given().get("/protocol/pinned/modern").then().statusCode(200).body(is("false"));
        Response response = client.given().body(FileServer.DYNAMIC_FILE).post("/protocol/pinned/subscriptions");
        assertEquals(500, response.statusCode());
        assertTrue(response.asString().contains("subscribeToResources requires MCP protocol 2026-07-28 or later"),
                response.asString());
    }

    // Scenario 4, subscriptions: with subscribe-to-*-list-changes=true the cached lists are refreshed
    @Test
    void subscribedListsAreRefreshed() {
        client.given().get("/protocol/modern/modern").then().statusCode(200).body(is("true"));
        client.given().get("/protocol/modern/tools").then().statusCode(200).body(not(containsString("greeter-modern")));
        client.given().get("/protocol/modern/prompts").then().statusCode(200).body(not(containsString("prompt-modern")));
        client.given().get("/protocol/modern/resources").then().statusCode(200)
                .body(not(containsString("file:///resource-modern")));
        try {
            runMeta("modern", "tool", "greeter-modern", "create");
            runMeta("modern", "prompt", "prompt-modern", "create");
            runMeta("modern", "resource", "file:///resource-modern", "create");
            AwaitilityUtils.untilAsserted(() -> client.given().get("/protocol/modern/tools").then().statusCode(200)
                    .body(containsString("greeter-modern")));
            AwaitilityUtils.untilAsserted(() -> client.given().get("/protocol/modern/prompts").then().statusCode(200)
                    .body(containsString("prompt-modern")));
            AwaitilityUtils.untilAsserted(() -> client.given().get("/protocol/modern/resources").then().statusCode(200)
                    .body(containsString("file:///resource-modern")));
        } finally {
            runMeta("modern", "tool", "greeter-modern", "delete");
            runMeta("modern", "prompt", "prompt-modern", "delete");
            runMeta("modern", "resource", "file:///resource-modern", "delete");
        }
    }

    // Scenario 4, subscriptions: with subscribe-to-*-list-changes=false the cached lists are kept
    @Test
    void unsubscribedListsStayCached() {
        client.given().get("/protocol/static/modern").then().statusCode(200).body(is("true"));
        client.given().get("/protocol/static/tools").then().statusCode(200).body(not(containsString("greeter-static")));
        client.given().get("/protocol/static/prompts").then().statusCode(200).body(not(containsString("prompt-static")));
        client.given().get("/protocol/static/resources").then().statusCode(200)
                .body(not(containsString("file:///resource-static")));
        try {
            runMeta("static", "tool", "greeter-static", "create");
            runMeta("static", "prompt", "prompt-static", "create");
            runMeta("static", "resource", "file:///resource-static", "create");
            waitForNotifications();
            client.given().get("/protocol/static/tools").then().statusCode(200).body(not(containsString("greeter-static")));
            client.given().get("/protocol/static/prompts").then().statusCode(200).body(not(containsString("prompt-static")));
            client.given().get("/protocol/static/resources").then().statusCode(200)
                    .body(not(containsString("file:///resource-static")));
        } finally {
            runMeta("static", "tool", "greeter-static", "delete");
            runMeta("static", "prompt", "prompt-static", "delete");
            runMeta("static", "resource", "file:///resource-static", "delete");
        }
    }

    // Scenario 5, timeout and cancellation: stdio with 2026-07-28
    @Test
    @Disabled("Current behaviour: the client sends notifications/cancelled for both revisions, but with 2026-07-28 the"
            + " server logs 'Ignored unknown/completed/invalid cancel request' and keeps running the tool, while with"
            + " 2025-11-25 it stops (see legacyToolTimeoutCancelsExecution). With 2026-07-28 every message is handled on its"
            + " own transient connection, and McpMessageHandler.cancelRequest looks the request up by its id and the"
            + " connection of the cancellation. Expected: the server stops processing the cancelled request (SHOULD in"
            + " spec basic/patterns/cancellation). See"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/patterns/cancellation#behavior-requirements")
    void toolTimeoutCancelsExecution() {
        client.given().get("/protocol/timeout/modern").then().statusCode(200).body(is("true"));
        assertTimeoutCancelsSlowTool("timeout");
    }

    // Scenario 5, timeout and cancellation: stdio with 2025-11-25
    @Test
    void legacyToolTimeoutCancelsExecution() {
        client.given().get("/protocol/legacy-timeout/modern").then().statusCode(200).body(is("false"));
        assertTimeoutCancelsSlowTool("legacy-timeout");
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
        assertTrue(elapsed.compareTo(Duration.ofSeconds(5)) < 0, "The timeout came after " + elapsed);

        AwaitilityUtils.untilAsserted(
                () -> assertEquals(ProtocolServer.CANCELLED, executeTool(mcpClient, "slow-status", "{}").asString(),
                        "The server did not stop working on the tool call after the client timed out"),
                AwaitilityUtils.AwaitilitySettings.usingTimeout(Duration.ofSeconds(10)));
    }

    private static void runMeta(String mcpClient, String type, String name, String operation) {
        Response response = executeTool(mcpClient, "meta", """
                {"type":"%s","name":"%s","operation":"%s"}
                """.formatted(type, name, operation));
        assertEquals(200, response.statusCode(), response.asString());
        assertFalse(response.asString().startsWith("Error"), response.asString());
    }

    private static void waitForNotifications() {
        // there is nothing to wait for, when a notification is expected not to arrive
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
