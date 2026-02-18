package io.quarkus.ts.mcp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.response.Response;

public abstract class BasicMCPIT {
    public abstract RestService client();

    @Test
    public void tools() {
        Response response = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(response.body().asString().contains("filereader"));
    }

    @Test
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
    @Disabled("https://github.com/quarkiverse/quarkus-langchain4j/issues/2138")
    public void readFileResource() {
        Response response = client().given().get("/mcp/resources/readFile");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Hello, AI!", response.body().asString());
    }

    @Test
    @Disabled("https://github.com/quarkiverse/quarkus-langchain4j/issues/2138")
    public void readTemplatedResource() {
        Response response = client().given().get("/mcp/resources/readFile/robot-readable.txt");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Hello, AI!", response.body().asString());
    }

    @Test
    public void toolChanges() {
        Response before = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, before.statusCode());
        Assertions.assertEquals("[rootchecker, filereader, meta]", before.body().asString());

        Response create = client().given().post("/mcp/meta/greeter");
        Assertions.assertEquals(200, create.statusCode());

        Response after = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, after.statusCode());
        Assertions.assertEquals("[rootchecker, filereader, meta, greeter]", after.body().asString());

        Response delete = client().given().delete("/mcp/meta/greeter");
        Assertions.assertEquals(200, delete.statusCode());

        Response last = client().given().get("/mcp/tools");
        Assertions.assertEquals(200, last.statusCode());
        Assertions.assertEquals("[rootchecker, filereader, meta]", last.body().asString());
    }

    @Test
    public void prompts() {
        Response list = client().given().get("/mcp/prompts");
        Assertions.assertEquals(200, list.statusCode());
        Assertions.assertEquals("[meta]", list.body().asString());

        Response usage = client().given().get("/mcp/prompt");
        Assertions.assertEquals(200, usage.statusCode());
        Assertions.assertEquals("This tool will create a new tool named 'hammer'", usage.body().asString());

        // prompts notifications do not work in client
        //        Response create = client().given().post("/mcp/meta/prompt/about-greeter");
        //        Assertions.assertEquals(200, create.statusCode());
        //
        //        Response listAgain = client().given().get("/mcp/prompts");
        //        Assertions.assertEquals(200, listAgain.statusCode());
        //        Assertions.assertEquals("[meta, about-greeter]", listAgain.body().asString());
        //
        //        Response delete = client().given().delete("/mcp/meta/prompt/greeter");
        //        Assertions.assertEquals(200, delete.statusCode());
        //
        //        Response last = client().given().get("/mcp/prompts");
        //        Assertions.assertEquals(200, last.statusCode());
        //        Assertions.assertEquals("[meta]", last.body().asString());
    }

    @Test
    public void roots() {
        Response before = client().given().get("/mcp/roots");
        Assertions.assertEquals(200, before.statusCode());
        Assertions.assertEquals("No roots found", before.body().asString());

        Response change = client().given().post("/mcp/roots");
        Assertions.assertEquals(204, change.statusCode());

        Response after = client().given().get("/mcp/roots");
        Assertions.assertEquals(200, after.statusCode());
        Assertions.assertEquals("first (example.com)", after.body().asString());
    }
}
