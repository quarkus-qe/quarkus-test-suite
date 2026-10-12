package io.quarkus.ts.mcp;

import java.time.Duration;
import java.util.List;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;

import io.quarkus.test.bootstrap.RestService;
import io.vertx.core.json.JsonObject;

final class ProtocolAssertions {
    private ProtocolAssertions() {
    }

    static void assertNegotiated(RestService application, String revision, int clients) {
        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<JsonObject> responses = application.getLogs().stream()
                    .filter(line -> line.contains("\"jsonrpc\""))
                    .map(line -> new JsonObject(line.substring(line.indexOf('{'))))
                    .map(message -> message.getJsonObject("result", new JsonObject()))
                    .filter(result -> result.containsKey("protocolVersion"))
                    .toList();
            for (JsonObject result : responses) {
                Assertions.assertEquals(revision, result.getString("protocolVersion"));
            }
            Assertions.assertTrue(responses.size() >= clients, "Missing initialization responses for " + clients + " clients");
        });
    }

    static void assertProposed(RestService application, String revision, int clients) {
        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<JsonObject> requests = application.getLogs().stream()
                    .filter(line -> line.contains("\"jsonrpc\""))
                    .map(line -> new JsonObject(line.substring(line.indexOf('{'))))
                    .filter(message -> "initialize".equals(message.getString("method")))
                    .toList();
            for (JsonObject request : requests) {
                Assertions.assertEquals(revision,
                        request.getJsonObject("params").getString("protocolVersion"));
            }
            Assertions.assertTrue(requests.size() >= clients,
                    "Missing outgoing initialization requests for " + clients + " clients");
        });
    }

    static void assertStateless(RestService application, String revision) {
        assertStateless(application, revision, 1);
    }

    static void assertStateless(RestService application, String revision, int clients) {
        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<JsonObject> requests = application.getLogs().stream()
                    .filter(line -> line.contains("\"jsonrpc\""))
                    .map(line -> new JsonObject(line.substring(line.indexOf('{'))))
                    .filter(message -> message.containsKey("method") && message.containsKey("id"))
                    .toList();
            Assertions.assertTrue(
                    requests.stream().filter(message -> "server/discover".equals(message.getString("method")))
                            .count() >= clients,
                    "Missing discovery requests for " + clients + " clients");
            for (JsonObject request : requests) {
                Assertions.assertNotEquals("initialize", request.getString("method"));
                JsonObject meta = request.getJsonObject("params").getJsonObject("_meta");
                Assertions.assertEquals(revision, meta.getString("io.modelcontextprotocol/protocolVersion"), request::encode);
                Assertions.assertNotNull(meta.getJsonObject("io.modelcontextprotocol/clientCapabilities"), request::encode);
            }
        });
    }

}
