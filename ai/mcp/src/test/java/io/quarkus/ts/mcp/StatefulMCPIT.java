package io.quarkus.ts.mcp;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.response.Response;

// The roots callback flow belongs to the older, stateful protocol revisions.
public abstract class StatefulMCPIT extends BasicMCPIT {
    @Test
    public void roots() {
        Response before = client().given().get("/mcp/roots");
        Assertions.assertEquals(200, before.statusCode());
        Assertions.assertEquals("No roots found", before.body().asString());

        Response change = client().given().post("/mcp/roots");
        Assertions.assertEquals(HttpStatus.SC_NO_CONTENT, change.statusCode());

        Response after = client().given().get("/mcp/roots");
        Assertions.assertEquals(200, after.statusCode());
        Assertions.assertEquals("first (file:///workspace)", after.body().asString());
    }
}
