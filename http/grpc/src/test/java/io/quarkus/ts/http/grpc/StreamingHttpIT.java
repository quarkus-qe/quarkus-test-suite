package io.quarkus.ts.http.grpc;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertx.core.http.HttpHeaders;
import io.vertx.mutiny.ext.web.client.WebClient;

public interface StreamingHttpIT {

    WebClient getWebClient();

    @Test
    default void serverStreaming() {
        var response = getWebClient().get("/http/streaming/server/ServerStreaming").sendAndAwait();
        Assertions.assertEquals(200, response.statusCode());
        List<?> responses = response.bodyAsJsonArray().getList();
        Assertions.assertEquals(GrpcStreamingService.SERVER_STREAM_MESSAGES_COUNT, responses.size());
        responses.forEach(message -> Assertions.assertEquals("Hello ServerStreaming", message));
    }

    @Test
    default void clientStreaming() {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        var response = getWebClient().post("/http/streaming/client")
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .sendJsonAndAwait(names);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Total names submitted: " + names.size(), response.bodyAsString());
    }

    @Test
    default void bidirectional() {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        var response = getWebClient().post("/http/streaming/bi")
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .sendJsonAndAwait(names);
        Assertions.assertEquals(200, response.statusCode());
        var messages = response.bodyAsJsonArray().getList();
        Assertions.assertEquals(names.size() + 1, messages.size());
        Assertions.assertEquals("Hello: Alice;Bob;Charlie;", messages.get(names.size()));
    }
}
