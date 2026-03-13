package io.quarkus.ts.mcp;

import java.nio.file.Path;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.AdvancedServer;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MyResources;
import io.restassured.response.Response;

@QuarkusScenario
public class WebSocketIT extends BasicMCPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-websocket")
    }, classes = { FileServer.class, MyResources.class, AdvancedServer.class })
    static final RestService server = new RestService()
            .withProperty("working.folder", () -> Path.of("target").toAbsolutePath().toString());

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest-jackson"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "websocket")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> getUrl(server));
    private static final int NUMBER_OF_TOOLS = 7; // 4 declared in FileServer class, 3 in AdvancedServer

    protected static String getUrl(RestService service) {
        return service.getURI(Protocol.WS).withPath("/mcp/ws").toString();
    }

    @Override
    public RestService client() {
        return client;
    }

    @Test
    @Disabled("https://github.com/quarkiverse/quarkus-langchain4j/issues/2223")
    @Override // This test has access to more tools due to AdvancedServer class. They also may be in a different order
    public void toolChanges() {
        Response before = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, before.statusCode());
        List<String> initialTools = before.body().jsonPath().getList(".");
        Assertions.assertEquals(NUMBER_OF_TOOLS, initialTools.size());
        Assertions.assertFalse(initialTools.contains("greeter"));

        Response create = client().given().post("/mcp/meta/greeter");
        Assertions.assertEquals(200, create.statusCode());

        Response after = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, after.statusCode());
        List<String> changedTools = after.body().jsonPath().getList(".");
        Assertions.assertEquals(NUMBER_OF_TOOLS + 1, changedTools.size());
        Assertions.assertTrue(changedTools.contains("greeter"));

        Response delete = client().given().delete("/mcp/meta/greeter");
        Assertions.assertEquals(200, delete.statusCode());

        Response last = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, last.statusCode());
        List<String> finalTools = before.body().jsonPath().getList(".");
        Assertions.assertEquals(NUMBER_OF_TOOLS, finalTools.size());
        Assertions.assertFalse(finalTools.contains("greeter"));
    }

    @Test
    @Disabled("https://github.com/quarkiverse/quarkus-mcp-server/issues/672")
    //Icons work, but pagination doesn't
    void iconCheck() {
        Session session = new Session(getUrl(server));
        session.sendRequest("initialize", Session.initParamas());
        JsonNode initialization = session.getResponse();
        Assertions.assertEquals("mcp", initialization
                .get("result")
                .get("serverInfo")
                .get("name").asText());

        session.sendRequest("tools/list", null);

        JsonNode result = session.getResponse().get("result");
        // responses for tools should be paginated
        Assertions.assertTrue(result.has("nextCursor"), "The result %s is not paginated!".formatted(result));
        Assertions.assertEquals(2, result.get("tools").size(), "The page size is incorrect!");

        List<JsonNode> tools = session.getToolsFromPaginatedAnswer(result, "tools/list");
        Assertions.assertEquals(NUMBER_OF_TOOLS, tools.size(), "The list of tools: %s has wrongs size ".formatted(tools));

        JsonNode rootchecker = null;
        for (JsonNode jsonNode : tools) {
            if (jsonNode.get("name").asText().equals("rootchecker")) {
                rootchecker = jsonNode;
                break;
            }
        }
        Assertions.assertNotNull(rootchecker);
        Assertions.assertEquals("image/svg", rootchecker
                .get("icons")
                .get(0)
                .get("mimeType").asText());
    }

    @Test
    void resourceNotifications() {
        Response resources = client().given().get("/mcp/resources/");
        Assertions.assertEquals(200, resources.statusCode());
        Assertions.assertEquals("[file:///hello, file:///number, file:///separate]", resources.body().asString());

        Session session = new Session(getUrl(server));
        session.sendRequest("initialize", Session.initParamas());
        JsonNode initialization = session.getResponse();
        Assertions.assertEquals("mcp", initialization
                .get("result")
                .get("serverInfo")
                .get("name").asText());

        Response before = client().given().get("/mcp/resources/readFile/number");
        Assertions.assertEquals(200, before.statusCode());
        Assertions.assertEquals("1", before.body().asString());

        session.sendRequest("resources/subscribe", """
                {"uri": "file:///number"}
                """);

        JsonNode subscription = session.getResponse();
        Assertions.assertEquals("", subscription.get("result").asText());
        session.clearHistory();

        Response update = client().given().put("/mcp/meta/resources/number");
        Assertions.assertEquals(HttpStatus.SC_NO_CONTENT, update.statusCode());

        JsonNode notification = session.getResponse();
        Assertions.assertEquals("notifications/resources/updated",
                notification.get("method").asText(),
                "No update in " + notification.asText());
        Assertions.assertEquals("file:///number", notification.get("params").get("uri").asText());

        Response after = client().given().get("/mcp/resources/readFile/number");
        Assertions.assertEquals(200, after.statusCode());
        Assertions.assertEquals("2", after.body().asString());
    }

    @Test
    void promptNotifications() {
        Session session = new Session(getUrl(server));
        session.sendRequest("initialize", Session.initParamas());
        JsonNode initialization = session.getResponse();
        Assertions.assertEquals("mcp", initialization
                .get("result")
                .get("serverInfo")
                .get("name").asText());

        session.sendRequest("prompts/list", null);
        JsonNode before = session.getResponse();
        JsonNode promptsBefore = before.get("result").get("prompts");
        Assertions.assertEquals(2, promptsBefore.size());
        Assertions.assertEquals("foodOrder", promptsBefore.get(0).get("name").asText());
        Assertions.assertEquals("meta", promptsBefore.get(1).get("name").asText());

        session.sendRequest("notifications/initialized", null);
        session.clearHistory();

        // another client asks to create a new prompt
        Response create = client().given().post("/mcp/meta/prompt/about-greeter");
        Assertions.assertEquals(200, create.statusCode());

        // a notification about update has arrived
        JsonNode notification = session.getResponse();
        Assertions.assertEquals("notifications/prompts/list_changed",
                notification.get("method").asText());

        session.sendRequest("prompts/list", null);
        JsonNode list = session.getResponse();
        JsonNode promptsAfter = list.get("result").get("prompts");
        Assertions.assertEquals(3, promptsAfter.size());
        Assertions.assertEquals("foodOrder", promptsAfter.get(0).get("name").asText());
        Assertions.assertEquals("meta", promptsAfter.get(1).get("name").asText());
        Assertions.assertEquals("about-greeter", promptsAfter.get(2).get("name").asText());

        session.clearHistory();
        Response delete = client().given().delete("/mcp/meta/prompt/about-greeter");
        Assertions.assertEquals(200, delete.statusCode());

        JsonNode deleteNotification = session.getResponse();
        Assertions.assertEquals("notifications/prompts/list_changed",
                deleteNotification.get("method").asText());

        session.sendRequest("prompts/list", null);
        JsonNode last = session.getResponse();
        JsonNode finalPrompts = last.get("result").get("prompts");
        Assertions.assertEquals(2, finalPrompts.size());
        Assertions.assertEquals("foodOrder", finalPrompts.get(0).get("name").asText());
        Assertions.assertEquals("meta", finalPrompts.get(1).get("name").asText());
    }

    @Test
    @DisabledOnNative(reason = "https://github.com/quarkiverse/quarkus-mcp-server/issues/679")
    void sampling() {
        Session session = new Session(getUrl(server));
        session.sendRequest("initialize", Session.initParamas());
        JsonNode initialization = session.getResponse();
        Assertions.assertEquals("mcp", initialization
                .get("result")
                .get("serverInfo")
                .get("name").asText());
        session.confirmReadiness();
        session.sendRequest("tools/call", """
                {
                 "name":"unsampled",
                 "arguments": {
                  "question": "What is the airspeed velocity of unladen swallow?"
                  }
                }
                """);
        JsonNode unsampled = session.getResponse().get("result");
        Assertions.assertFalse(unsampled.get("isError").asBoolean());
        Assertions.assertEquals("Answer to \"What is the airspeed velocity of unladen swallow?\" is 42",
                unsampled.get("content").get(0).get("text").asText());
        session.sendRequest("tools/call", """
                {
                 "name":"sampled",
                 "arguments": {
                  "question": "What is the airspeed velocity of unladen swallow?"
                  }
                }
                """);

        JsonNode sampled = session.getResponse();
        Assertions.assertEquals("sampling/createMessage", sampled.get("method").asText());
        Assertions.assertEquals("An African or European swallow?",
                sampled.get("params").get("messages").get(0).get("content").get("text").asText());
        session.sendResponse(sampled.get("id").asInt(), """
                {
                 "role": "assistant",
                 "model": "shmodel",
                 "stopReason": "it's enough",
                 "content": {
                   "type": "text",
                   "text": "I do not know"
                 }
                }
                """);
        JsonNode result = session.getResponse().get("result");
        Assertions.assertFalse(result.get("isError").asBoolean());
        Assertions.assertEquals("""
                The answer to "What is the airspeed velocity of unladen swallow?" is "I do not know"
                """.trim(),
                result.get("content").get(0).get("text").asText());
    }

    @Test
    @DisabledOnNative(reason = "https://github.com/quarkiverse/quarkus-mcp-server/issues/679")
    void elicitation() {
        Session session = new Session(getUrl(server));
        session.sendRequest("initialize", Session.initParamas());
        JsonNode initialization = session.getResponse();
        Assertions.assertEquals("mcp", initialization
                .get("result")
                .get("serverInfo")
                .get("name").asText());
        session.confirmReadiness();
        session.sendRequest("tools/call", """
                {
                 "name":"elicitation"
                }
                """);
        JsonNode elicitation = session.getResponse();
        session.sendResponse(elicitation.get("id").asInt(), """
                 {
                  "action": "accept",
                  "content": {
                    "name": "QE client"
                   }
                }
                """);

        JsonNode result = session.getResponse().get("result");
        Assertions.assertFalse(result.get("isError").asBoolean());
        Assertions.assertEquals("Hello QE client!", result.get("content").get(0).get("text").asText());
    }

    @Test
    void completion() {
        Session session = new Session(getUrl(server));
        session.sendRequest("initialize", Session.initParamas());
        JsonNode initialization = session.getResponse();
        Assertions.assertEquals("mcp", initialization
                .get("result")
                .get("serverInfo")
                .get("name").asText());

        // when we ask to complete an empty line, we should be offered all four possible options
        // namely "broccoli", "pizza", "salad", "pie"
        session.sendRequest("completion/complete", getCompletionFor(""));
        Assertions.assertEquals(4,
                session.getResponse().get("result").get("completion").get("values").size());

        // initial "pi" shrinks the number of options
        session.sendRequest("completion/complete", getCompletionFor("pi"));
        JsonNode partialSuggestion = session.getResponse().get("result").get("completion").get("values");
        Assertions.assertEquals(2, partialSuggestion.size());
        Assertions.assertEquals("pizza", partialSuggestion.get(0).asText());
        Assertions.assertEquals("pie", partialSuggestion.get(1).asText());

        session.sendRequest("completion/complete", getCompletionFor("piz"));
        JsonNode finalSuggestion = session.getResponse().get("result").get("completion").get("values");
        Assertions.assertEquals(1, finalSuggestion.size());
        Assertions.assertEquals("pizza", finalSuggestion.get(0).asText());
    }

    @Test
    void automaticProgress() {
        Session session = new Session(getUrl(server));
        session.sendRequest("initialize", Session.initParamas());
        JsonNode initialization = session.getResponse();
        Assertions.assertEquals("mcp", initialization
                .get("result")
                .get("serverInfo")
                .get("name").asText());
        session.confirmReadiness();

        String progressToken = "autoToken256";
        session.sendRequest("tools/call", """
                {
                 "name":"wait",
                 "_meta": {
                   "progressToken": "%s"
                 },
                 "arguments": {
                   "input": "this is it",
                   "autoLog": true
                  }
                }
                """.formatted(progressToken));

        // The 'wait' tool in FileServer (method 'longOperation') processes data in three batches/steps
        // and sends notifications on each of them
        // On the first notifications we do additional checks, to make sure, that progress tracker properly passes
        // both total number of steps and progress token
        JsonNode first = session.getResponse();
        session.clearHistory();
        Assertions.assertEquals("notifications/progress", first.get("method").asText());
        Assertions.assertEquals(progressToken, first.get("params").get("progressToken").asText());
        Assertions.assertEquals(1, first.get("params").get("progress").asInt());
        Assertions.assertEquals(3, first.get("params").get("total").asInt());
        Assertions.assertEquals("Processing item #1", first.get("params").get("message").asText());

        JsonNode second = session.getResponse();
        session.clearHistory();
        Assertions.assertEquals("notifications/progress", second.get("method").asText());
        Assertions.assertEquals(2, second.get("params").get("progress").asInt());
        Assertions.assertEquals("Processing item #2", second.get("params").get("message").asText());

        JsonNode third = session.getResponse();
        session.clearHistory();
        Assertions.assertEquals("notifications/progress", third.get("method").asText());
        Assertions.assertEquals(3, third.get("params").get("progress").asInt());
        Assertions.assertEquals("Processing item #3", third.get("params").get("message").asText());

        JsonNode result = session.getResponse().get("result");
        Assertions.assertFalse(result.get("isError").asBoolean());
        Assertions.assertEquals("this is it: 123", result.get("content").get(0).get("text").asText());
    }

    @Test
    void manualProgress() {
        Session session = new Session(getUrl(server));
        session.sendRequest("initialize", Session.initParamas());
        JsonNode initialization = session.getResponse();
        Assertions.assertEquals("mcp", initialization
                .get("result")
                .get("serverInfo")
                .get("name").asText());
        session.confirmReadiness();

        String progressToken = "manualToken512";
        session.sendRequest("tools/call", """
                {
                 "name":"wait",
                 "_meta": {
                   "progressToken": "%s"
                 },
                 "arguments": {
                   "input": "this is it",
                   "autoLog": false
                  }
                }
                """.formatted(progressToken));

        // The 'wait' tool in FileServer (method 'longOperation') processes data in three batches/steps
        // and sends notifications on each of them
        // On the first notifications we do additional checks, to make sure, that notificationBuilder properly passes
        // both total number of steps and progress token
        JsonNode first = session.getResponse();
        session.clearHistory();
        Assertions.assertEquals("notifications/progress", first.get("method").asText());
        Assertions.assertEquals(progressToken, first.get("params").get("progressToken").asText());
        Assertions.assertEquals(3, first.get("params").get("total").asInt());
        Assertions.assertEquals(1, first.get("params").get("progress").asInt());
        Assertions.assertEquals("Processing step 1 of 3", first.get("params").get("message").asText());

        JsonNode second = session.getResponse();
        session.clearHistory();
        Assertions.assertEquals("notifications/progress", second.get("method").asText());
        Assertions.assertEquals(2, second.get("params").get("progress").asInt());
        Assertions.assertEquals("Processing step 2 of 3", second.get("params").get("message").asText());

        JsonNode third = session.getResponse();
        session.clearHistory();
        Assertions.assertEquals("notifications/progress", third.get("method").asText());
        Assertions.assertEquals(3, third.get("params").get("progress").asInt());
        Assertions.assertEquals("Processing step 3 of 3", third.get("params").get("message").asText());

        JsonNode result = session.getResponse().get("result");
        Assertions.assertFalse(result.get("isError").asBoolean());
        Assertions.assertEquals("this is it: 123", result.get("content").get(0).get("text").asText());
    }

    @Test
    public void logging() throws InterruptedException {
        Session session = new Session(getUrl(server));
        session.sendRequest("initialize", Session.initParamas());
        Assertions.assertTrue(session.getResponse().get("result").get("capabilities").has("logging"));

        session.confirmReadiness();
        session.sendRequest("resources/read", """
                {
                "uri" : "file:///another-file.txt"
                }
                """);
        List<JsonNode> responses = session.getResponses();
        Assertions.assertEquals(2, responses.size()); // log + answer

        JsonNode response = responses.get(1);
        Assertions.assertEquals("There is some content there, too!",
                response.get("result").get("contents").get(0).get("text").asText());
        JsonNode log = responses.get(0);
        Assertions.assertEquals("notifications/message", log.get("method").asText());
        Assertions.assertEquals("info", log.get("params").get("level").asText());
        Assertions.assertEquals("resource_template:fileresource", log.get("params").get("logger").asText());
        String message = log.get("params").get("data").asText();
        Assertions.assertTrue(message.startsWith("Reading provided file"));
        Assertions.assertTrue(message.endsWith("another-file.txt"));

        // now check logs in server output. It may take some time on OpenShift
        List<String> logs;
        List<String> logLines;
        int attempts = 0;
        do {
            attempts++;
            logs = server.getLogs();
            logLines = logs.stream().filter(line -> line.endsWith("another-file.txt")).toList();
            Thread.sleep(1000);
        } while (logLines.isEmpty() && attempts < 10);

        Assertions.assertFalse(logLines.isEmpty(), "No MCP log message found in server logs!");
        Assertions.assertEquals(1, logLines.size(), "To many MCP log messages found: \n" + logLines);
        Assertions.assertTrue(logLines.get(0).matches(
                "\\d{2}:\\d{2}:\\d{2},\\d{3} Reading provided file: .*another-file.txt"),
                "Log message has unknown format: " + logLines.get(0));
    }

    private String getCompletionFor(String name) {
        return """
                {
                    "ref": {
                      "type": "ref/prompt",
                      "name": "foodOrder"
                    },
                    "argument": {
                      "name": "food",
                      "value": "%s"
                    }
                  }
                """.formatted(name);
    }

}
