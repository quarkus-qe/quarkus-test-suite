package io.quarkus.ts.http.advanced;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.example.GreeterGrpc;
import io.quarkus.example.HelloReply;
import io.quarkus.example.HelloRequest;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.ts.http.advanced.utils.GrpcWebSocketListener;
import io.restassured.response.Response;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

// TODO mvavrik: enable test and adjust it to new DEV UI
@Disabled("Disabled as DEV UI is work in progress currenlty")
@Tag("QUARKUS-1026")
@Tag("QUARKUS-1094")
@QuarkusScenario
public class DevModeGrpcIntegrationIT {

    private static final String NAME = "QE";

    private static final int UNARY_ID = 1;
    private static final int SERVER_STREAM_ID = 2;
    private static final int CLIENT_STREAM_ID = 3;
    private static final int BIDIRECTIONAL_STREAM_ID = 4;
    private static final int CLIENT_STREAM_NAMES_SUBMITTED_COUNT = 2;
    private static final int SERVER_STREAM_MESSAGES_RETURNED_COUNT = 5;

    private static final List<String> GRPC_SOCKET_MESSAGES = Arrays.asList(
            "{\"serviceName\":\"helloworld.Greeter\",\"methodName\":\"SayHello\",\"id\":" + UNARY_ID
                    + ",\"content\":\"{\\\"name\\\":\\\"Quarkus\\\"}\"}",
            "{\"serviceName\":\"helloworld.Streaming\",\"methodName\":\"ServerStream\",\"id\":" + SERVER_STREAM_ID
                    + ",\"content\":\"{\\\"name\\\":\\\"Five Times\\\"}\"}",
            "{\"serviceName\":\"helloworld.Streaming\",\"methodName\":\"ClientStream\",\"id\":" + CLIENT_STREAM_ID
                    + ",\"content\":\"{\\\"name\\\":\\\"NameOne\\\"}\"}",
            "{\"serviceName\":\"helloworld.Streaming\",\"methodName\":\"ClientStream\",\"id\":" + CLIENT_STREAM_ID
                    + ",\"content\":\"{\\\"name\\\":\\\"NameTwo\\\"}\"}",
            "{\"id\":" + CLIENT_STREAM_ID + ",\"command\":\"DISCONNECT\"}",
            "{\"serviceName\":\"helloworld.Streaming\",\"methodName\":\"BidirectionalStream\",\"id\":" + BIDIRECTIONAL_STREAM_ID
                    + ",\"content\":\"{\\\"name\\\":\\\"John\\\"}\"}",
            "{\"serviceName\":\"helloworld.Streaming\",\"methodName\":\"BidirectionalStream\",\"id\":" + BIDIRECTIONAL_STREAM_ID
                    + ",\"content\":\"{\\\"name\\\":\\\"Max\\\"}\"}",
            "{\"id\":" + BIDIRECTIONAL_STREAM_ID + ",\"command\":\"DISCONNECT\"}");

    @DevModeQuarkusApplication(grpc = true)
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.keycloak.policy-enforcer.enable", "false")
            .withProperty("quarkus.keycloak.devservices.enabled", "false")
            // Init webSocketListener and webSocket
            .onPostStart(DevModeGrpcIntegrationIT::initDevUiGrpcWsClient);

    static WebSocket webSocket;
    static GrpcWebSocketListener webSocketListener;

    @Test
    public void testGrpcAsClient() {
        HelloRequest request = HelloRequest.newBuilder().setName(NAME).build();
        HelloReply response = GreeterGrpc.newBlockingStub(app.grpcChannel()).sayHello(request);

        assertEquals("Hello " + NAME, response.getMessage());
    }

    @Test
    public void testGrpcViaRest() {
        app.given().when().get("/api/grpc/trinity").then().statusCode(HttpStatus.SC_OK).body(is("Hello trinity"));
    }

    @Test
    public void testGrpcDevUISocket() throws JsonProcessingException {
        for (String message : GRPC_SOCKET_MESSAGES) {
            webSocket.send(message);
        }

        await().atMost(60, TimeUnit.SECONDS).until(this::isGrpcStreamsCompleted);
        checkGrpcDevUiOutput();
    }

    @Test
    public void testGrpcDevUIServicesView() {
        List<Pattern> patterns = Arrays.asList(
                Pattern.compile("helloworld\\.Streaming(?=.*SERVER_STREAMING ServerStream)" +
                        "(?=.*CLIENT_STREAMING ClientStream)(?=.*BIDI_STREAMING BidirectionalStream)"),
                Pattern.compile("helloworld\\.Greeter(?=.*UNARY SayHello)"));
        Response response = app.given().when().get("/q/dev/io.quarkus.quarkus-grpc/services")
                .then().statusCode(HttpStatus.SC_OK).extract().response();
        Document doc = Jsoup.parse(response.getBody().asString());
        Element table = doc.select("table").get(0);

        assertTrue(patterns.stream().allMatch(pattern -> pattern.matcher(table.text()).find()),
                "DevUI gRPC services view s incomplete");
    }

    private void checkGrpcDevUiOutput() throws JsonProcessingException {
        Map<Integer, List<String>> grpcOutputMap = webSocketListener.getServiceOutputMessagesMap();

        List<String> messages;
        for (int streamId : grpcOutputMap.keySet()) {
            switch (streamId) {
                case (UNARY_ID):
                    Log.info("Testing gRPC Unary service...");
                    messages = getMessagesByStreamId(streamId);
                    assertTrue(messages.contains("Hello Quarkus"), "Unary service FAILED");
                    break;
                case (SERVER_STREAM_ID):
                    Log.info("Testing gRPC Server stream service...");
                    messages = getMessagesByStreamId(streamId);
                    int actualMessagesReturnedCount = Collections.frequency(messages, "Hello Five Times");
                    assertEquals(SERVER_STREAM_MESSAGES_RETURNED_COUNT, actualMessagesReturnedCount,
                            "Server Stream service FAILED");
                    break;
                case (CLIENT_STREAM_ID):
                    Log.info("Testing gRPC Client stream service...");
                    messages = getMessagesByStreamId(streamId);
                    assertTrue(messages.contains("Total names submitted: " + CLIENT_STREAM_NAMES_SUBMITTED_COUNT),
                            "Client Stream service FAILED");
                    break;
                case (BIDIRECTIONAL_STREAM_ID):
                    Log.info("Testing gRPC Bidirectional stream service...");
                    messages = getMessagesByStreamId(streamId);
                    assertTrue(messages.contains("Hello: John;"), "Bidirectional Stream service FAILED");
                    assertTrue(messages.contains("Hello: John;Max;"), "Bidirectional Stream service FAILED");
                    break;
                default: // Do nothing
            }
        }
    }

    private List<String> getMessagesByStreamId(int id) throws JsonProcessingException {
        List<String> messages = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (String payload : webSocketListener.getServiceOutputMessagesMap().get(id)) {
            ObjectNode node = objectMapper.readValue(payload, ObjectNode.class);
            JsonNode body;
            if ((body = node.get("body")) != null) {
                node = objectMapper.readValue(body.textValue(), ObjectNode.class);
            }
            JsonNode message;
            if ((message = node.get("message")) != null) {
                messages.add(message.textValue());
            }
        }
        return messages;
    }

    private boolean isGrpcStreamsCompleted() {
        final int expectedCompletedStreams = 4;
        long completedStreams = webSocketListener.getServiceOutputMessagesMap()
                .values().stream()
                .filter(list -> list.get(list.size() - 1).contains("COMPLETED"))
                .count();
        return completedStreams == expectedCompletedStreams;
    }

    private static void initDevUiGrpcWsClient(Service service) {
        webSocketListener = new GrpcWebSocketListener();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("ws://localhost:" + app.getPort() + "/q/dev/io.quarkus.quarkus-grpc/grpc-test")
                .build();
        webSocket = client.newWebSocket(request, webSocketListener);
    }
}
