package io.quarkus.ts.mcp;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.mcp.app.ProtocolObserver;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

final class ProtocolAssertions {
    private ProtocolAssertions() {
    }

    static List<JsonObject> observations(RestService server) {
        String body = server.given().get("/test/protocol-observations").then().statusCode(200).extract().asString();
        return new JsonArray(body).stream().map(JsonObject.class::cast).toList();
    }

    static List<JsonObject> stderrObservations(RestService application) {
        // Stdio log collection is asynchronous; wait for an initialization response or discovery request to appear.
        return Awaitility.await("stdio initialization response or discovery request")
                .atMost(Duration.ofSeconds(15))
                .until(() -> application.getLogs().stream().filter(line -> line.contains(ProtocolObserver.STDERR_PREFIX))
                        .map(line -> new JsonObject(line.substring(line.indexOf(ProtocolObserver.STDERR_PREFIX)
                                + ProtocolObserver.STDERR_PREFIX.length())))
                        .toList(),
                        messages -> messages.stream().anyMatch(message -> ("sent".equals(message.getString("direction"))
                                && "initialize".equals(message.getString("method")))
                                || ("received".equals(message.getString("direction"))
                                        && "server/discover".equals(message.getString("method")))));
    }

    static void assertRevision(Supplier<List<JsonObject>> observations, String revision) {
        // Authentication scenarios can switch revision without changing their behavioral assertions.
        switch (revision) {
            case "2025-11-25" -> assertNegotiated(observations, revision);
            case "2026-07-28" -> assertStateless(observations, revision);
            default -> throw new IllegalArgumentException("No protocol assertions for revision " + revision);
        }
    }

    static void assertNegotiated(Supplier<List<JsonObject>> observations, String revision) {
        List<JsonObject> messages = observations.get();
        Assertions.assertTrue(messages.stream().anyMatch(message -> "received".equals(message.getString("direction"))
                && "initialize".equals(message.getString("method"))), "No LangChain4j initialization request observed");
        // A named-provider server is shared: an earlier client's initialization must not hide a modern client.
        for (JsonObject message : messages) {
            if ("received".equals(message.getString("direction"))) {
                Assertions.assertNotEquals("server/discover", message.getString("method"),
                        () -> "Expected initialization for MCP " + revision + ", but observed server/discover");
                if (message.getString("revision") != null) {
                    Assertions.assertEquals(revision, message.getString("revision"),
                            () -> "Unexpected MCP revision in client " + message.getString("method") + " request");
                }
            }
        }
        List<JsonObject> responses = messages.stream()
                .filter(message -> "sent".equals(message.getString("direction"))).toList();
        Assertions.assertFalse(responses.isEmpty(), "No LangChain4j initialization response observed");
        for (JsonObject response : responses) {
            Assertions.assertEquals(revision, response.getString("revision"),
                    "Server negotiated an unexpected MCP revision");
        }
    }

    static void assertStateless(Supplier<List<JsonObject>> observations, String revision) {
        List<JsonObject> requests = observations.get().stream()
                .filter(message -> "received".equals(message.getString("direction"))).toList();
        Assertions.assertTrue(requests.stream().anyMatch(message -> "server/discover".equals(message.getString("method"))),
                "No LangChain4j discovery request observed");
        for (JsonObject request : requests) {
            Assertions.assertNotEquals("initialize", request.getString("method"),
                    () -> "Expected discovery for MCP " + revision + ", but observed initialize");
            Assertions.assertEquals(revision, request.getString("revision"),
                    () -> "Unexpected MCP revision in " + request.getString("method") + " request metadata");
            Assertions.assertTrue(request.getBoolean("capabilities"),
                    () -> "Missing client-capabilities metadata in " + request.getString("method") + " request");
        }
    }
}
