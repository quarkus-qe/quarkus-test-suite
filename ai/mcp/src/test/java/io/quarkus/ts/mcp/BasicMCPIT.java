package io.quarkus.ts.mcp;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.restassured.response.Response;

public abstract class BasicMCPIT {
    public abstract RestService client();

    @Test
    @Disabled("https://github.com/quarkiverse/quarkus-langchain4j/issues/2223")
    public void tools() {
        Response response = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(response.body().asString().contains("filereader"));
    }

    @Test
    @Disabled("https://github.com/quarkiverse/quarkus-langchain4j/issues/2223")
    public void args() {
        Response response = client().given().get("/mcp/tools/filereader/arguments");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("[[file]]", response.body().asString());
    }

    @Test
    public void readFile() {
        Response response = client().given().body("robot-readable.txt").post("/mcp/tools/readFile");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Hello, AI!", response.body().asString());
    }

    @Test
    public void readFileResource() {
        Response response = client().given().get("/mcp/resources/readFile");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Hello, AI!", response.body().asString());

        Response content = client().given().get("/mcp/resources/readFile/separate");
        Assertions.assertEquals(200, content.statusCode());
        Assertions.assertEquals("Hello from other resource!", content.body().asString());
    }

    @Test
    public void readTemplatedResource() throws InterruptedException {
        Response response = client().given().get("/mcp/resources/readFile/robot-readable.txt");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Hello, AI!", response.body().asString());

        // also, let's check logs on client side. It may take some time on OpenShift
        List<String> logs;
        List<String> logLines;
        int attempts = 0;
        do {
            attempts++;
            logs = client().getLogs();
            logLines = logs.stream().filter(line -> line.contains("MCP logger")).toList();
            Thread.sleep(1000);
        } while (logLines.isEmpty() && attempts < 10);

        Assertions.assertFalse(logLines.isEmpty(), "No MCP log message found in logs:\n" + logs);
        Assertions.assertEquals(1, logLines.size(), "To many MCP log messages found: \n" + logLines);
        Assertions.assertTrue(logLines.get(0).matches(
                """
                        \\d{2}:\\d{2}:\\d{2},\\d{3} MCP logger: resource_template:fileresource: "Reading provided file: .*robot-readable.txt"
                        """
                        .trim()),
                "Log message has unknown format: " + logLines.get(0));
    }

    @Test
    @Disabled("https://github.com/quarkiverse/quarkus-langchain4j/issues/2223")
    public void toolChanges() {
        Response before = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, before.statusCode());
        String initialTools = "[rootchecker, wait, filereader, meta]";
        Assertions.assertEquals(initialTools, before.body().asString());

        Response create = client().given().post("/mcp/meta/greeter");
        Assertions.assertEquals(200, create.statusCode());
        String changedTools = "[rootchecker, wait, filereader, meta, greeter]";

        Response after = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, after.statusCode());
        Assertions.assertEquals(changedTools, after.body().asString());

        Response delete = client().given().delete("/mcp/meta/greeter");
        Assertions.assertEquals(200, delete.statusCode());

        Response last = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, last.statusCode());
        Assertions.assertEquals(initialTools, last.body().asString());
    }

    @Test
    public void prompts() {
        Response list = client().given().get("/mcp/prompts");
        Assertions.assertEquals(200, list.statusCode());
        Assertions.assertTrue(list.body().asString().contains("meta"));

        Response usage = client().given().get("/mcp/prompt");
        Assertions.assertEquals(200, usage.statusCode());
        Assertions.assertEquals("This tool will create a new tool named 'hammer'", usage.body().asString());
    }

    @Test
    @DisabledOnNative(reason = "https://github.com/quarkiverse/quarkus-langchain4j/issues/2234")
    public void roots() {
        Response before = client().given().get("/mcp/roots");
        Assertions.assertEquals(200, before.statusCode());
        Assertions.assertEquals("No roots found", before.body().asString());

        Response change = client().given().post("/mcp/roots");
        Assertions.assertEquals(HttpStatus.SC_NO_CONTENT, change.statusCode());

        Response after = client().given().get("/mcp/roots");
        Assertions.assertEquals(200, after.statusCode());
        Assertions.assertEquals("first (example.com)", after.body().asString());
    }
}
