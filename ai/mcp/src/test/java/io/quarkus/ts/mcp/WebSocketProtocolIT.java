package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.app.Utils.getFileFolder;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MyResources;
import io.quarkus.ts.mcp.app.ProtocolClient;
import io.restassured.response.Response;

/**
 * Verifies the behavior of the MCP client over the WebSocket transport, which depends on the protocol revision: protocol
 * detection, resource subscriptions and list-change subscriptions.
 */
@QuarkusScenario
public class WebSocketProtocolIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-websocket")
    }, classes = { FileServer.class, MyResources.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> getFileFolder(WebSocketProtocolIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { ProtocolClient.class }, properties = "websocket-protocol-client.properties")
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.modern.url", WebSocketProtocolIT::serverUrl)
            .withProperty("quarkus.langchain4j.mcp.unsubscribe.url", WebSocketProtocolIT::serverUrl)
            .withProperty("quarkus.langchain4j.mcp.static.url", WebSocketProtocolIT::serverUrl)
            .withProperty("quarkus.langchain4j.mcp.pinned.url", WebSocketProtocolIT::serverUrl);

    private static String serverUrl() {
        return server.getURI(Protocol.WS).withPath("/mcp/ws").toString();
    }

    // Protocol version detection: a real 2.x server is detected as 2026-07-28
    @Test
    void modernServerIsDetected() {
        client.given().get("/protocol/modern/modern").then().statusCode(200).body(is("true"));
        client.given().get("/protocol/modern/tools").then().statusCode(200).body(containsString("filereader"));
        assertEquals("Hello, AI!", readFile("modern"));
    }

    // Subscriptions and list changes: updates for several URIs arrive
    @Test
    void resourceUpdatesDelivered() {
        assertResourceUpdatesDelivered("modern");
    }

    // Subscriptions and list changes: updates stop after the client unsubscribes
    @Test
    @Disabled("Current behaviour: the client sends notifications/cancelled on unsubscribe, but the server logs 'Ignored"
            + " unknown/completed/invalid cancel request' and keeps sending updates. With 2026-07-28 every message is handled"
            + " on its own transient connection, and McpMessageHandler.cancelSubscription searches the other connections"
            + " for the subscription only for stdio. Expected: no updates after unsubscribing, as over stdio. See"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/patterns/subscriptions#cancellation")
    void resourceSubscriptions() {
        String subscriptionId = assertResourceUpdatesDelivered("unsubscribe");

        client.given().pathParam("id", subscriptionId).delete("/protocol/unsubscribe/subscriptions/{id}").then()
                .statusCode(204);
        client.given().delete("/protocol/unsubscribe/updates").then().statusCode(204);
        runMeta("unsubscribe", "resource", FileServer.DYNAMIC_FILE, "update");
        runMeta("unsubscribe", "resource", FileServer.HELLO_FILE, "update");
        waitForNotifications();
        client.given().get("/protocol/unsubscribe/updates").then().statusCode(200).body(is(""));
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

    /**
     * Subscribes to several resources, changes one of them and checks, that the update arrives.
     *
     * @return the subscription id
     */
    private static String assertResourceUpdatesDelivered(String mcpClient) {
        client.given().pathParam("client", mcpClient).get("/protocol/{client}/modern").then().statusCode(200)
                .body(is("true"));
        client.given().pathParam("client", mcpClient).delete("/protocol/{client}/updates").then().statusCode(204);

        Response subscription = client.given().pathParam("client", mcpClient)
                .body(FileServer.DYNAMIC_FILE + "," + FileServer.HELLO_FILE)
                .post("/protocol/{client}/subscriptions");
        assertEquals(200, subscription.statusCode(), subscription.asString());

        runMeta(mcpClient, "resource", FileServer.DYNAMIC_FILE, "update");
        runMeta(mcpClient, "resource", FileServer.HELLO_FILE, "update");
        AwaitilityUtils.untilAsserted(() -> client.given().pathParam("client", mcpClient).get("/protocol/{client}/updates")
                .then().statusCode(200)
                .body(allOf(containsString(FileServer.DYNAMIC_FILE), containsString(FileServer.HELLO_FILE))));
        return subscription.asString();
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
