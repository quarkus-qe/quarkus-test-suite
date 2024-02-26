package io.quarkus.ts.http.grpc;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public interface StreamingHttpIT {

    RestService app();

    default RequestSpecification given() {
        return app().given();
    }

    @Test
    default void serverStreaming() {
        Response response = given().when().get("/http/streaming/server/ServerStreaming");
        Assertions.assertEquals(200, response.statusCode());
        List<String> responses = response.jsonPath().getList(".");
        Assertions.assertEquals(GrpcStreamingService.SERVER_STREAM_MESSAGES_COUNT, responses.size());
        responses.forEach(message -> Assertions.assertEquals("Hello ServerStreaming", message));
    }

    @Test
    default void clientStreaming() {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        Response response = given().when()
                .contentType(ContentType.JSON)
                .body(names)
                .post("/http/streaming/client");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Total names submitted: " + names.size(), response.body().asString());
    }

    @Test
    default void bidirectional() {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        Response response = given().when()
                .contentType(ContentType.JSON)
                .body(names)
                .post("/http/streaming/bi");
        Assertions.assertEquals(200, response.statusCode());
        List<String> messages = response.jsonPath().getList(".");
        Assertions.assertEquals(names.size() + 1, messages.size());
        Assertions.assertEquals("Hello: Alice;Bob;Charlie;", messages.get(names.size()));
    }
}
