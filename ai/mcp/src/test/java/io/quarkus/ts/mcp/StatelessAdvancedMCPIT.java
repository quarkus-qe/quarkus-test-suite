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

import io.quarkiverse.mcp.server.test.McpAssured.McpTestClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Adds MCP 2026-07-28 sampling and form elicitation through multi-round-trip
 * requests, filtered subscriptions, request-scoped progress and logging checks.
 */
public abstract class StatelessAdvancedMCPIT extends StatelessMCPIT {
    // Wildcards keep these hooks shared across McpAssured clients with different transport self types.
    protected abstract List<String> serverLogs(McpTestClient<?, ?> protocol);

    protected SubscriptionObserver subscribe(McpTestClient<?, ?> protocol, JsonObject request) {
        return SubscriptionObserver.open(protocol, request);
    }

    private static void assertSubscriptionNotification(SubscriptionObserver observer, JsonObject notification, String method) {
        Assertions.assertEquals(method, notification.getString("method"), "Unexpected notification method");
        Assertions.assertEquals(observer.id(), notification.getJsonObject("params").getJsonObject("_meta")
                .getValue("io.modelcontextprotocol/subscriptionId"), "Unexpected notification subscription ID");
    }

    @Test
    public void subscriptionsAndFiltering() {
        JsonObject selectedFilters = new JsonObject().put("toolsListChanged", true).put("promptsListChanged", true)
                .put("resourceSubscriptions", new JsonArray().add("file:///number"));
        JsonObject filteredFilters = new JsonObject().put("promptsListChanged", true)
                .put("resourceSubscriptions", new JsonArray().add("file:///hello"));
        try (McpTestClient<?, ?> protocol = protocolClient();
                SubscriptionObserver selected = subscribe(protocol,
                        message(protocol, "subscriptions/listen", new JsonObject().put("notifications", selectedFilters)));
                SubscriptionObserver filtered = subscribe(protocol,
                        message(protocol, "subscriptions/listen", new JsonObject().put("notifications", filteredFilters)))) {
            // Each stream starts with an acknowledgement; subsequent counts include that first notification.
            JsonObject selectedAck = selected.await(1);
            // Check the common notification envelope here; other checks focus on event kind and routing.
            Assertions.assertEquals("2.0", selectedAck.getString("jsonrpc"), "Unexpected notification JSON-RPC version");
            Assertions.assertFalse(selectedAck.containsKey("id"), selectedAck::encode);
            assertSubscriptionNotification(selected, selectedAck, "notifications/subscriptions/acknowledged");
            Assertions.assertEquals(selectedFilters, selectedAck.getJsonObject("params").getJsonObject("notifications"));
            JsonObject filteredAck = filtered.await(1);
            assertSubscriptionNotification(filtered, filteredAck, "notifications/subscriptions/acknowledged");
            Assertions.assertEquals(filteredFilters, filteredAck.getJsonObject("params").getJsonObject("notifications"));
            Assertions.assertNotEquals(selected.id(), filtered.id());

            JsonObject beforeResponse = exchange(protocol,
                    message(protocol, "resources/read", new JsonObject().put("uri", "file:///number")));
            String before = beforeResponse.getJsonObject("result").getJsonArray("contents").getJsonObject(0).getString("text");
            changeFeature(protocol, "resource", "file:///number", "update");
            JsonObject changedResource = selected.await(2); // acknowledgement + selected resource update
            assertSubscriptionNotification(selected, changedResource, "notifications/resources/updated");
            Assertions.assertEquals("file:///number", changedResource.getJsonObject("params").getString("uri"));
            JsonObject afterResponse = exchange(protocol,
                    message(protocol, "resources/read", new JsonObject().put("uri", "file:///number")));
            Assertions.assertEquals(Integer.toString(Integer.parseInt(before) + 1),
                    afterResponse.getJsonObject("result").getJsonArray("contents").getJsonObject(0).getString("text"));

            changeFeature(protocol, "tool", "subscription-greeter", "create");
            try {
                // Selected: acknowledgement + resource update + tool creation.
                assertSubscriptionNotification(selected, selected.await(3), "notifications/tools/list_changed");
                List<String> names = toolPages(protocol).stream().flatMap(page -> page.getJsonArray("tools").stream())
                        .map(tool -> ((JsonObject) tool).getString("name")).toList();
                Assertions.assertTrue(names.contains("subscription-greeter"), names::toString);
            } finally {
                changeFeature(protocol, "tool", "subscription-greeter", "delete");
            }
            // Tool deletion is the selected stream's fourth notification.
            assertSubscriptionNotification(selected, selected.await(4), "notifications/tools/list_changed");
            List<String> toolNames = toolPages(protocol).stream().flatMap(page -> page.getJsonArray("tools").stream())
                    .map(tool -> ((JsonObject) tool).getString("name")).toList();
            Assertions.assertFalse(toolNames.contains("subscription-greeter"), toolNames::toString);

            changeFeature(protocol, "prompt", "subscription-prompt", "create");
            try {
                // Prompt creation is selected's fifth notification and filtered's second, after its acknowledgement.
                assertSubscriptionNotification(selected, selected.await(5), "notifications/prompts/list_changed");
                assertSubscriptionNotification(filtered, filtered.await(2), "notifications/prompts/list_changed");
                JsonObject response = exchange(protocol, message(protocol, "prompts/list", new JsonObject()));
                List<String> names = response.getJsonObject("result").getJsonArray("prompts").stream()
                        .map(prompt -> ((JsonObject) prompt).getString("name")).toList();
                Assertions.assertTrue(names.contains("subscription-prompt"), names::toString);
            } finally {
                changeFeature(protocol, "prompt", "subscription-prompt", "delete");
            }
            // Prompt deletion adds one notification to each stream: selected six, filtered three.
            assertSubscriptionNotification(selected, selected.await(6), "notifications/prompts/list_changed");
            assertSubscriptionNotification(filtered, filtered.await(3), "notifications/prompts/list_changed");
            JsonObject response = exchange(protocol, message(protocol, "prompts/list", new JsonObject()));
            List<String> promptNames = response.getJsonObject("result").getJsonArray("prompts").stream()
                    .map(prompt -> ((JsonObject) prompt).getString("name")).toList();
            Assertions.assertFalse(promptNames.contains("subscription-prompt"), promptNames::toString);
            // Selected: ack + resource + two tool changes + two prompt changes. Filtered: ack + two prompt changes.
            Awaitility.await().during(Duration.ofMillis(500)).atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                Assertions.assertEquals(6, selected.notifications().size());
                Assertions.assertEquals(3, filtered.notifications().size(),
                        "Unrequested resource/tool changes reached the subscription");
            });
        }
    }

    private void changeFeature(McpTestClient<?, ?> protocol, String type, String name, String operation) {
        JsonObject response = exchange(protocol, message(protocol, "tools/call", call("meta", new JsonObject()
                .put("type", type).put("name", name).put("operation", operation))));
        JsonObject changed = response.getJsonObject("result");
        Assertions.assertNotNull(changed, response::encode);
        Assertions.assertEquals(false, changed.getBoolean("isError"), response::encode);
    }

    @Test
    public void samplingWithoutClientInput() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject response = exchange(protocol, message(protocol, "tools/call",
                    call("unsampled", new JsonObject().put("question", "Question"))));
            assertText(response, "Answer to \"Question\" is 42");
        }
    }

    @Test
    public void samplingMultiRoundTrip() {
        // Multi-round-trip requests return the required client input, then resume with its response and request state.
        String question = "What is the airspeed velocity of unladen swallow?";
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject params = call("sampled", new JsonObject().put("question", question));
            JsonObject firstResponse = exchange(protocol, message(protocol, "tools/call", params));
            JsonObject first = firstResponse.getJsonObject("result");
            Assertions.assertNotNull(first, firstResponse::encode);
            Assertions.assertEquals("input_required", first.getString("resultType"));
            // Input-required is a different result family from discovery's complete result.
            Assertions.assertEquals("mcp",
                    first.getJsonObject("_meta").getJsonObject("io.modelcontextprotocol/serverInfo").getString("name"));
            Assertions.assertEquals("qe-sampling-state", first.getString("requestState"));
            Assertions.assertEquals(Set.of("answer"), first.getJsonObject("inputRequests").fieldNames());
            JsonObject input = first.getJsonObject("inputRequests").getJsonObject("answer");
            Assertions.assertEquals("sampling/createMessage", input.getString("method"));
            Assertions.assertFalse(first.containsKey("content"));
            JsonObject sampling = input.getJsonObject("params");
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
            Assertions.assertEquals("complete", response.getJsonObject("result").getString("resultType"));
            Assertions.assertTrue(protocol.snapshot().requests().isEmpty(), "MRTR must not initiate a sampling callback");
        }
    }

    @Test
    public void formElicitationMultiRoundTrip() {
        // The client supplies form input in a retry instead of answering an elicitation callback.
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject firstResponse = exchange(protocol,
                    message(protocol, "tools/call", call("elicitation", new JsonObject())));
            JsonObject first = firstResponse.getJsonObject("result");
            Assertions.assertNotNull(first, firstResponse::encode);
            Assertions.assertEquals("input_required", first.getString("resultType"));
            Assertions.assertEquals("qe-form-state", first.getString("requestState"));
            Assertions.assertEquals(Set.of("userInput"), first.getJsonObject("inputRequests").fieldNames());
            JsonObject input = first.getJsonObject("inputRequests").getJsonObject("userInput");
            Assertions.assertEquals("elicitation/create", input.getString("method"));
            Assertions.assertFalse(first.containsKey("content"));
            JsonObject elicitation = input.getJsonObject("params");
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
            Assertions.assertEquals("complete", accepted.getJsonObject("result").getString("resultType"));
            Assertions.assertTrue(protocol.snapshot().requests().isEmpty(), "MRTR must not initiate an elicitation callback");
        }
    }

    @Test
    public void formElicitationCancelled() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject first = exchange(protocol, message(protocol, "tools/call", call("elicitation", new JsonObject())))
                    .getJsonObject("result");
            Assertions.assertNotNull(first);
            JsonObject retry = call("elicitation", new JsonObject()).put("requestState", first.getString("requestState"))
                    .put("inputResponses", new JsonObject().put("userInput", new JsonObject().put("action", "cancel")
                            .put("content", new JsonObject())));
            assertText(exchange(protocol, message(protocol, "tools/call", retry)), "User cancelled the request");
            Assertions.assertTrue(protocol.snapshot().requests().isEmpty(), "MRTR must not initiate an elicitation callback");
        }
    }

    @Test
    public void formElicitationInvalidState() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject first = exchange(protocol, message(protocol, "tools/call", call("elicitation", new JsonObject())))
                    .getJsonObject("result");
            Assertions.assertNotNull(first);
            JsonObject retry = call("elicitation", new JsonObject()).put("requestState", "wrong-state")
                    .put("inputResponses", new JsonObject().put("userInput", new JsonObject().put("action", "accept")
                            .put("content", new JsonObject().put("name", "QE client"))));
            assertError(exchange(protocol, message(protocol, "tools/call", retry)), -32602, "Unexpected MRTR request state");
        }
    }

    @Test
    public void samplingRequiresClientCapability() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject request = message(protocol, "tools/call", call("sampled", new JsonObject().put("question", "Question")));
            request.getJsonObject("params").getJsonObject("_meta").put("io.modelcontextprotocol/clientCapabilities",
                    new JsonObject());
            JsonObject response = exchange(protocol, request);
            assertError(response, -32021, "capability");
            Assertions
                    .assertNotNull(response.getJsonObject("error").getJsonObject("data").getJsonObject("requiredCapabilities"));
        }
    }

    @Test
    public void elicitationRequiresClientCapability() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject request = message(protocol, "tools/call", call("elicitation", new JsonObject()));
            request.getJsonObject("params").getJsonObject("_meta").put("io.modelcontextprotocol/clientCapabilities",
                    new JsonObject());
            JsonObject response = exchange(protocol, request);
            assertError(response, -32021, "capability");
            Assertions
                    .assertNotNull(response.getJsonObject("error").getJsonObject("data").getJsonObject("requiredCapabilities"));
        }
    }

    @Test
    public void progressIsRequestScoped() {
        try (McpTestClient<?, ?> protocol = protocolClient();
                SubscriptionObserver changes = subscribe(protocol, message(protocol, "subscriptions/listen",
                        new JsonObject().put("notifications", new JsonObject().put("toolsListChanged", true))))) {
            assertSubscriptionNotification(changes, changes.await(1), "notifications/subscriptions/acknowledged");
            for (boolean automatic : List.of(true, false)) {
                String token = automatic ? "autoToken256" : "manualToken512";
                int count = protocol.snapshot().notifications().size();
                JsonObject params = call("wait", new JsonObject().put("input", "this is it").put("autoLog", automatic))
                        .put("_meta", new JsonObject().put("progressToken", token));
                JsonObject response = exchange(protocol, message(protocol, "tools/call", params));
                assertText(response, "this is it: 123");
                Assertions.assertEquals("complete", response.getJsonObject("result").getString("resultType"));
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
            assertNoRequestNotificationsOnSubscription(changes, "Progress");
        }
    }

    @Test
    public void perRequestLogging() {
        try (McpTestClient<?, ?> protocol = protocolClient();
                SubscriptionObserver changes = subscribe(protocol, message(protocol, "subscriptions/listen",
                        new JsonObject().put("notifications", new JsonObject().put("toolsListChanged", true))))) {
            assertSubscriptionNotification(changes, changes.await(1), "notifications/subscriptions/acknowledged");
            // The final warning request proves that the info level from the preceding request does not persist.
            for (String level : List.of("warning", "info", "warning")) {
                int count = protocol.snapshot().notifications().size();
                JsonObject read = new JsonObject().put("uri", "file:///robot-readable.txt")
                        .put("_meta", new JsonObject().put("io.modelcontextprotocol/logLevel", level));
                JsonObject response = exchange(protocol, message(protocol, "resources/read", read));
                Assertions.assertEquals("Hello, AI!",
                        response.getJsonObject("result").getJsonArray("contents").getJsonObject(0).getString("text"));
                List<JsonObject> messages = protocol.snapshot().notifications().subList(count,
                        protocol.snapshot().notifications().size());
                Assertions.assertEquals("info".equals(level) ? 1 : 0, messages.size(),
                        "The request log level was not honored: " + level);
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
            assertNoRequestNotificationsOnSubscription(changes, "Logs");
        }
    }

    @Test
    public void invalidRequestLogLevel() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            JsonObject request = message(protocol, "resources/read", new JsonObject().put("uri", "file:///robot-readable.txt")
                    .put("_meta", new JsonObject().put("io.modelcontextprotocol/logLevel", "unknown")));
            assertError(exchange(protocol, request), -32602, "Invalid log level");
        }
    }

    @Test
    public void resourceServerLogging() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
            int before = serverLogs(protocol).size();
            JsonObject response = exchange(protocol,
                    message(protocol, "resources/read", new JsonObject().put("uri", "file:///robot-readable.txt")
                            .put("_meta", new JsonObject().put("io.modelcontextprotocol/logLevel", "info"))));
            Assertions.assertEquals("Hello, AI!",
                    response.getJsonObject("result").getJsonArray("contents").getJsonObject(0).getString("text"));
            Awaitility.await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> Assertions.assertTrue(serverLogs(protocol).stream()
                            .skip(before)
                            .anyMatch(line -> line.contains("Reading provided file:") && line.endsWith("robot-readable.txt"))));
        }
    }

    private static void assertNoRequestNotificationsOnSubscription(SubscriptionObserver observer, String kind) {
        Awaitility.await().during(Duration.ofMillis(500)).atMost(Duration.ofSeconds(5)).untilAsserted(() -> Assertions
                .assertEquals(1, observer.notifications().size(),
                        kind + " notifications reached the change subscription: " + observer.notifications()));
    }

    @Test
    @Disabled("https://github.com/quarkiverse/quarkus-mcp-server/issues/1076: log notifications emitted without request opt-in")
    public void loggingRequiresOptIn() {
        try (McpTestClient<?, ?> protocol = protocolClient()) {
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
