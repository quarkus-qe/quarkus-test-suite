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

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.McpClients;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolClient;
import io.restassured.response.Response;

/**
 * Verifies the behavior of the MCP client over the stdio transport, which depends on the protocol revision: protocol
 * detection, resource subscriptions and list-change subscriptions.
 * <p>
 * Every MCP client spawns its own server process, so a client only sees changes made through itself.
 */
@QuarkusScenario
public class StdioProtocolIT {

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-stdio")
    }, classes = { FileServer.class, MyResources.class })
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
            .withProperty("quarkus.langchain4j.mcp.pinned.command", StdioProtocolIT::serverCommand);

    private static String serverCommand() {
        return "java,-Dworking.folder=" + getFileFolder(StdioProtocolIT.class).replaceAll("\\\\", "/")
                + ",-jar," + getJarPath(server).replaceAll("\\\\", "/");
    }

    @AfterAll
    static void stopMcpServers() {
        // otherwise every stdio process still running adds a minute to stopping the client
        client.given().delete("/mcp-clients").then().statusCode(204);
    }

    // Protocol version detection: a real 2.x server is detected as 2026-07-28
    @Test
    void modernServerIsDetected() {
        client.given().get("/protocol/modern/modern").then().statusCode(200).body(is("true"));
        client.given().get("/protocol/modern/tools").then().statusCode(200).body(containsString("filereader"));
        assertEquals("Hello, AI!", readFile("modern"));
    }

    // Subscriptions and list changes: updates for several URIs arrive until the client unsubscribes
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

    // Subscriptions and list changes: deprecated methods and setRoots throw with 2026-07-28
    @Test
    void deprecatedMethodsFailOnModernProtocol() {
        client.given().get("/protocol/modern/modern").then().statusCode(200).body(is("true"));
        for (String operation : List.of("subscribeToResource", "unsubscribeFromResource", "setRoots")) {
            Response response = client.given().pathParam("operation", operation)
                    .post("/protocol/modern/deprecated/{operation}");
            assertEquals(500, response.statusCode(), operation + " did not fail");
            assertTrue(response.asString().contains("java.lang.UnsupportedOperationException"), response.asString());
            assertTrue(response.asString().contains("2026-07-28"), response.asString());
        }
    }

    // Subscriptions and list changes: subscribeToResources throws with 2025-11-25
    @Test
    void resourceSubscriptionsFailOnLegacyProtocol() {
        client.given().get("/protocol/pinned/modern").then().statusCode(200).body(is("false"));
        Response response = client.given().body(FileServer.DYNAMIC_FILE).post("/protocol/pinned/subscriptions");
        assertEquals(500, response.statusCode());
        assertTrue(response.asString().contains("subscribeToResources requires MCP protocol 2026-07-28 or later"),
                response.asString());
    }

    // Subscriptions and list changes: with subscribe-to-*-list-changes=true the cached lists are refreshed
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

    // Subscriptions and list changes: with subscribe-to-*-list-changes=false the cached lists are kept
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
