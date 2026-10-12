package io.quarkus.ts.mcp;

import static io.quarkus.ts.mcp.WebSocketIT.assertText;
import static io.quarkus.ts.mcp.WebSocketIT.call;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkiverse.mcp.server.test.McpAssured;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Adds MCP 2026-07-28 sampling and form elicitation through multi-round-trip
 * requests, filtered subscriptions, request-scoped progress and logging checks.
 */
public abstract class StatelessAdvancedMCPIT extends StatelessMCPIT {
    protected abstract List<String> serverLogs(McpAssured.McpTestClient<?, ?> protocol);

    protected SubscriptionObserver subscribe(McpAssured.McpTestClient<?, ?> protocol, JsonObject request) {
        return SubscriptionObserver.open(protocol, request);
    }

    private SubscriptionObserver listen(McpAssured.McpTestClient<?, ?> protocol, JsonObject notifications) {
        JsonObject request = message(protocol, "subscriptions/listen", new JsonObject().put("notifications", notifications));
        SubscriptionObserver observer = subscribe(protocol, request);
        try {
            JsonObject acknowledgement = observer.await(1);
            assertNotification(observer, acknowledgement, "notifications/subscriptions/acknowledged");
            Assertions.assertEquals(notifications, acknowledgement.getJsonObject("params").getJsonObject("notifications"));
            return observer;
        } catch (Throwable e) {
            observer.close();
            throw e;
        }
    }

    private static void assertNotification(SubscriptionObserver observer, JsonObject notification, String method) {
        Assertions.assertEquals("2.0", notification.getString("jsonrpc"));
        Assertions.assertFalse(notification.containsKey("id"));
        Assertions.assertEquals(method, notification.getString("method"), notification::encode);
        Assertions.assertEquals(observer.id(), notification.getJsonObject("params").getJsonObject("_meta")
                .getValue("io.modelcontextprotocol/subscriptionId"));
    }

    @Test
    public void subscriptionsAndFiltering() {
        try (var protocol = protocolClient();
                var selected = listen(protocol, new JsonObject().put("toolsListChanged", true).put("promptsListChanged", true)
                        .put("resourceSubscriptions", new JsonArray().add("file:///number")));
                var filtered = listen(protocol, new JsonObject().put("promptsListChanged", true)
                        .put("resourceSubscriptions", new JsonArray().add("file:///hello")))) {
            Assertions.assertNotEquals(selected.id(), filtered.id());
            String before = result(protocol, "resources/read", new JsonObject().put("uri", "file:///number"))
                    .getJsonArray("contents").getJsonObject(0).getString("text");
            meta(protocol, "resource", "file:///number", "update");
            JsonObject changedResource = selected.await(2);
            assertNotification(selected, changedResource, "notifications/resources/updated");
            Assertions.assertEquals("file:///number", changedResource.getJsonObject("params").getString("uri"));
            Assertions.assertEquals(Integer.toString(Integer.parseInt(before) + 1),
                    result(protocol, "resources/read", new JsonObject().put("uri", "file:///number"))
                            .getJsonArray("contents").getJsonObject(0).getString("text"));
            meta(protocol, "tool", "subscription-greeter", "create");
            try {
                assertNotification(selected, selected.await(3), "notifications/tools/list_changed");
                assertNames(protocol, "tools/list", "tools", "subscription-greeter", true);
                meta(protocol, "prompt", "subscription-prompt", "create");
                try {
                    assertNotification(selected, selected.await(4), "notifications/prompts/list_changed");
                    assertNotification(filtered, filtered.await(2), "notifications/prompts/list_changed");
                    assertNames(protocol, "prompts/list", "prompts", "subscription-prompt", true);
                } finally {
                    meta(protocol, "prompt", "subscription-prompt", "delete");
                }
                assertNotification(selected, selected.await(5), "notifications/prompts/list_changed");
                assertNotification(filtered, filtered.await(3), "notifications/prompts/list_changed");
                assertNames(protocol, "prompts/list", "prompts", "subscription-prompt", false);
            } finally {
                meta(protocol, "tool", "subscription-greeter", "delete");
            }
            assertNotification(selected, selected.await(6), "notifications/tools/list_changed");
            assertNames(protocol, "tools/list", "tools", "subscription-greeter", false);
            Awaitility.await().during(Duration.ofMillis(500)).atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                Assertions.assertEquals(6, selected.notifications().size());
                Assertions.assertEquals(3, filtered.notifications().size(),
                        "Unrequested resource/tool changes reached the subscription");
            });
        }
    }

    private void meta(McpAssured.McpTestClient<?, ?> protocol, String type, String name, String operation) {
        JsonObject changed = result(protocol, "tools/call", call("meta", new JsonObject()
                .put("type", type).put("name", name).put("operation", operation)));
        Assertions.assertFalse(changed.getBoolean("isError"), changed::encode);
    }

    private void assertNames(McpAssured.McpTestClient<?, ?> protocol, String method, String field, String name,
            boolean present) {
        boolean found = false;
        String cursor = null;
        do {
            JsonObject params = new JsonObject();
            if (cursor != null) {
                params.put("cursor", cursor);
            }
            JsonObject page = result(protocol, method, params);
            found |= page.getJsonArray(field).stream().anyMatch(value -> name.equals(((JsonObject) value).getString("name")));
            cursor = page.getString("nextCursor");
        } while (cursor != null);
        Assertions.assertEquals(present, found, name);
    }

    @Test
    public void samplingMrtr() {
        String question = "What is the airspeed velocity of unladen swallow?";
        try (var protocol = protocolClient()) {
            assertText(
                    exchange(protocol,
                            message(protocol, "tools/call", call("unsampled", new JsonObject().put("question", question)))),
                    "Answer to \"" + question + "\" is 42");
            JsonObject params = call("sampled", new JsonObject().put("question", question));
            JsonObject first = result(protocol, "tools/call", params);
            assertInputRequired(first, "answer", "sampling/createMessage", "qe-sampling-state");
            JsonObject sampling = first.getJsonObject("inputRequests").getJsonObject("answer").getJsonObject("params");
            Assertions.assertEquals(100, sampling.getInteger("maxTokens"));
            JsonObject user = sampling.getJsonArray("messages").getJsonObject(0);
            Assertions.assertEquals("user", user.getString("role"));
            Assertions.assertEquals("text", user.getJsonObject("content").getString("type"));
            Assertions.assertEquals("An African or European swallow?", user.getJsonObject("content").getString("text"));
            JsonObject answer = new JsonObject().put("role", "assistant").put("model", "shmodel").put("stopReason", "endTurn")
                    .put("content", new JsonObject().put("type", "text").put("text", "I do not know"));
            params.put("inputResponses", new JsonObject().put("answer", answer)).put("requestState",
                    first.getString("requestState"));
            JsonObject response = exchange(protocol, message(protocol, "tools/call", params));
            assertText(response, "The answer to \"" + question + "\" is \"I do not know\"");
            assertComplete(response.getJsonObject("result"), false);
            Assertions.assertTrue(protocol.snapshot().requests().isEmpty(), "MRTR must not initiate a sampling callback");
        }
    }

    @Test
    public void formElicitationMrtr() {
        try (var protocol = protocolClient()) {
            JsonObject first = result(protocol, "tools/call", call("elicitation", new JsonObject()));
            assertInputRequired(first, "userInput", "elicitation/create", "qe-form-state");
            JsonObject elicitation = first.getJsonObject("inputRequests").getJsonObject("userInput").getJsonObject("params");
            Assertions.assertEquals("form", elicitation.getString("mode"));
            Assertions.assertEquals("Please provide your information:", elicitation.getString("message"));
            JsonObject schema = elicitation.getJsonObject("requestedSchema");
            Assertions.assertEquals("object", schema.getString("type"));
            Assertions.assertEquals(List.of("name"), schema.getJsonArray("required").getList());
            Assertions.assertEquals("string", schema.getJsonObject("properties").getJsonObject("name").getString("type"));
            JsonObject retry = call("elicitation", new JsonObject()).put("requestState", first.getString("requestState"))
                    .put("inputResponses", new JsonObject().put("userInput", new JsonObject().put("action", "accept")
                            .put("content", new JsonObject().put("name", "QE client"))));
            JsonObject accepted = exchange(protocol, message(protocol, "tools/call", retry));
            assertText(accepted, "Hello QE client!");
            assertComplete(accepted.getJsonObject("result"), false);
            retry.getJsonObject("inputResponses").put("userInput",
                    new JsonObject().put("action", "cancel").put("content", new JsonObject()));
            assertText(exchange(protocol, message(protocol, "tools/call", retry)), "User cancelled the request");
            retry.put("requestState", "wrong-state");
            assertError(exchange(protocol, message(protocol, "tools/call", retry)), -32602, "Unexpected MRTR request state");
            Assertions.assertTrue(protocol.snapshot().requests().isEmpty(), "MRTR must not initiate an elicitation callback");
        }
    }

    @Test
    public void missingClientInputCapabilities() {
        try (var protocol = protocolClient()) {
            for (String tool : List.of("sampled", "elicitation")) {
                JsonObject arguments = "sampled".equals(tool) ? new JsonObject().put("question", "Question") : new JsonObject();
                JsonObject request = message(protocol, "tools/call", call(tool, arguments));
                request.getJsonObject("params").getJsonObject("_meta").put("io.modelcontextprotocol/clientCapabilities",
                        new JsonObject());
                JsonObject response = exchange(protocol, request);
                assertError(response, -32021, "capability");
                Assertions.assertNotNull(
                        response.getJsonObject("error").getJsonObject("data").getJsonObject("requiredCapabilities"));
            }
        }
    }

    private void assertInputRequired(JsonObject result, String key, String method, String state) {
        Assertions.assertEquals("input_required", result.getString("resultType"));
        Assertions.assertEquals(state, result.getString("requestState"));
        Assertions.assertEquals(Set.of(key), result.getJsonObject("inputRequests").fieldNames());
        Assertions.assertEquals(method, result.getJsonObject("inputRequests").getJsonObject(key).getString("method"));
        Assertions.assertFalse(result.containsKey("content"));
        Assertions.assertEquals("mcp",
                result.getJsonObject("_meta").getJsonObject("io.modelcontextprotocol/serverInfo").getString("name"));
    }

    @Test
    public void progressIsRequestScoped() {
        try (var protocol = protocolClient(); var changes = listen(protocol, new JsonObject().put("toolsListChanged", true))) {
            for (boolean automatic : List.of(true, false)) {
                String token = automatic ? "autoToken256" : "manualToken512";
                int count = protocol.snapshot().notifications().size();
                JsonObject params = call("wait", new JsonObject().put("input", "this is it").put("autoLog", automatic))
                        .put("_meta", new JsonObject().put("progressToken", token));
                JsonObject response = exchange(protocol, message(protocol, "tools/call", params));
                assertText(response, "this is it: 123");
                assertComplete(response.getJsonObject("result"), false);
                List<JsonObject> progress = protocol.snapshot().notifications().subList(count,
                        protocol.snapshot().notifications().size());
                Assertions.assertEquals(3, progress.size());
                for (int i = 0; i < 3; i++) {
                    JsonObject event = progress.get(i);
                    Assertions.assertEquals("notifications/progress", event.getString("method"));
                    JsonObject data = event.getJsonObject("params");
                    Assertions.assertEquals(token, data.getString("progressToken"));
                    Assertions.assertEquals(i + 1, data.getInteger("progress"));
                    Assertions.assertEquals(3, data.getInteger("total"));
                    Assertions.assertEquals(automatic ? "Processing item #" + (i + 1) : "Processing step " + (i + 1) + " of 3",
                            data.getString("message"));
                    Assertions.assertFalse(data.getJsonObject("_meta", new JsonObject())
                            .containsKey("io.modelcontextprotocol/subscriptionId"));
                }
            }
            assertNoSubscriptionLeak(changes, "Progress");
        }
    }

    @Test
    public void perRequestLogging() {
        try (var protocol = protocolClient(); var changes = listen(protocol, new JsonObject().put("toolsListChanged", true))) {
            int serverLogCount = serverLogs(protocol).size();
            for (String level : List.of("warning", "info", "warning")) {
                int count = protocol.snapshot().notifications().size();
                JsonObject read = new JsonObject().put("uri", "file:///robot-readable.txt")
                        .put("_meta", new JsonObject().put("io.modelcontextprotocol/logLevel", level));
                JsonObject content = result(protocol, "resources/read", read);
                Assertions.assertEquals("Hello, AI!", content.getJsonArray("contents").getJsonObject(0).getString("text"));
                List<JsonObject> messages = protocol.snapshot().notifications().subList(count,
                        protocol.snapshot().notifications().size());
                Assertions.assertEquals("info".equals(level) ? 1 : 0, messages.size(), "The request log level was not honored");
                if (!messages.isEmpty()) {
                    JsonObject log = messages.get(0);
                    Assertions.assertEquals("notifications/message", log.getString("method"));
                    JsonObject data = log.getJsonObject("params");
                    Assertions.assertEquals("info", data.getString("level"));
                    Assertions.assertEquals("resource_template:fileresource", data.getString("logger"));
                    Assertions.assertTrue(data.getString("data").startsWith("Reading provided file:"));
                    Assertions.assertTrue(data.getString("data").endsWith("robot-readable.txt"));
                    Assertions.assertFalse(data.getJsonObject("_meta", new JsonObject())
                            .containsKey("io.modelcontextprotocol/subscriptionId"));
                }
            }
            JsonObject invalid = message(protocol, "resources/read", new JsonObject().put("uri", "file:///robot-readable.txt")
                    .put("_meta", new JsonObject().put("io.modelcontextprotocol/logLevel", "unknown")));
            assertError(exchange(protocol, invalid), -32602, "Invalid log level");
            Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                Assertions.assertTrue(serverLogs(protocol).stream().skip(serverLogCount)
                        .anyMatch(line -> line.contains("Reading provided file:") && line.endsWith("robot-readable.txt")));
            });
            assertNoSubscriptionLeak(changes, "Logs");
        }
    }

    private static void assertNoSubscriptionLeak(SubscriptionObserver observer, String kind) {
        Awaitility.await().during(Duration.ofMillis(500)).atMost(Duration.ofSeconds(5)).untilAsserted(() -> Assertions
                .assertEquals(1, observer.notifications().size(), kind + " leaked onto the change subscription"));
    }

    @Test
    @Disabled("https://github.com/quarkiverse/quarkus-mcp-server/issues/1076: log notifications emitted without request opt-in")
    public void loggingRequiresOptIn() {
        try (var protocol = protocolClient()) {
            int before = protocol.snapshot().notifications().size();
            JsonObject read = message(protocol, "resources/read", new JsonObject().put("uri", "file:///robot-readable.txt"));
            Assertions.assertFalse(
                    read.getJsonObject("params").getJsonObject("_meta").containsKey("io.modelcontextprotocol/logLevel"));
            JsonObject response = exchange(protocol, read);
            Assertions.assertEquals("Hello, AI!",
                    response.getJsonObject("result").getJsonArray("contents").getJsonObject(0).getString("text"));
            List<JsonObject> notifications = protocol.snapshot().notifications().subList(before,
                    protocol.snapshot().notifications().size());
            Assertions.assertTrue(notifications.isEmpty(), () -> "Unexpected notifications without logLevel: " + notifications);
        }
    }

}
