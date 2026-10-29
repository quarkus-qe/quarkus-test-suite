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
        return application.getLogs().stream().filter(line -> line.contains(ProtocolObserver.STDERR_PREFIX))
                .map(line -> new JsonObject(line.substring(line.indexOf(ProtocolObserver.STDERR_PREFIX)
                        + ProtocolObserver.STDERR_PREFIX.length())))
                .toList();
    }

    static void assertNegotiated(Supplier<List<JsonObject>> observations, String revision) {
        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<JsonObject> messages = observations.get();
            // A named-provider server is shared: an earlier client's initialization must not hide a modern client.
            for (JsonObject message : messages) {
                if ("received".equals(message.getString("direction"))) {
                    Assertions.assertNotEquals("server/discover", message.getString("method"), message::encode);
                    if (message.getString("revision") != null) {
                        Assertions.assertEquals(revision, message.getString("revision"), message::encode);
                    }
                }
            }
            List<JsonObject> responses = messages.stream()
                    .filter(message -> "sent".equals(message.getString("direction"))).toList();
            Assertions.assertFalse(responses.isEmpty(), "No LangChain4j initialization response observed");
            for (JsonObject response : responses) {
                Assertions.assertEquals(revision, response.getString("revision"), response::encode);
            }
        });
    }

    static void assertStateless(Supplier<List<JsonObject>> observations, String revision) {
        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<JsonObject> requests = observations.get().stream()
                    .filter(message -> "received".equals(message.getString("direction"))).toList();
            Assertions.assertTrue(requests.stream().anyMatch(message -> "server/discover".equals(message.getString("method"))),
                    "No LangChain4j discovery request observed");
            for (JsonObject request : requests) {
                Assertions.assertNotEquals("initialize", request.getString("method"), request::encode);
                Assertions.assertEquals(revision, request.getString("revision"), request::encode);
                Assertions.assertTrue(request.getBoolean("capabilities"), request::encode);
            }
        });
    }
}
