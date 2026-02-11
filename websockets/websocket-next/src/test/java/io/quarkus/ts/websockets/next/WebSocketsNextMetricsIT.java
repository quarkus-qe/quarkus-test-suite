package io.quarkus.ts.websockets.next;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static java.time.Duration.ofSeconds;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.websockets.next.client.WebSocketTestClient;
import io.restassured.response.ValidatableResponse;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusScenario
@Tag("QUARKUS-5667")
public class WebSocketsNextMetricsIT {

    private static final Logger LOG = Logger.getLogger(WebSocketsNextMetricsIT.class);

    private static final String TOTAL_SERVER_CONNECTIONS_OPEN_FORMAT = "quarkus_websockets_server_connections_opened_total{uri=\"/chat/:username\"} %s.0";
    private static final String TOTAL_SERVER_CONNECTIONS_CLOSED_FORMAT = "quarkus_websockets_server_connections_closed_total{uri=\"/chat/:username\"} %s.0";
    private static final String TOTAL_SERVER_MESSAGES_OUTBOUND_FORMAT = "quarkus_websockets_server_count_total{direction=\"OUTBOUND\",uri=\"/chat/:username\"} %s.0";
    private static final String TOTAL_SERVER_MESSAGES_INBOUND_FORMAT = "quarkus_websockets_server_count_total{direction=\"INBOUND\",uri=\"/chat/:username\"} %s.0";
    private static final String TOTAL_SERVER_BYTES_OUTBOUND_FORMAT = "quarkus_websockets_server_bytes_total{direction=\"OUTBOUND\",uri=\"/chat/:username\"} %s.0";
    private static final String TOTAL_SERVER_BYTES_INBOUND_FORMAT = "quarkus_websockets_server_bytes_total{direction=\"INBOUND\",uri=\"/chat/:username\"} %s.0";
    private static final String TOTAL_SERVER_CONNECTIONS_ONOPEN_ERRORS_FORMAT = "quarkus_websockets_server_connections_onopen_errors_total{uri=\"/failing-onopen-error-no-handler\"} %s.0";
    private static final String TOTAL_SERVER_ENDPOINT_ERRORS_FORMAT = "quarkus_websockets_server_endpoint_count_errors_total{uri=\"%s\"} %%s.0";
    private static final String TOTAL_CLIENT_CONNECTIONS_OPEN_FORMAT = "quarkus_websockets_client_connections_opened_total{uri=\"/chat/{username}\"} %s.0";
    private static final String TOTAL_CLIENT_CONNECTIONS_CLOSED_FORMAT = "quarkus_websockets_client_connections_closed_total{uri=\"/chat/{username}\"} %s.0";
    private static final String TOTAL_CLIENT_MESSAGES_OUTBOUND_FORMAT = "quarkus_websockets_client_count_total{direction=\"OUTBOUND\",uri=\"/chat/{username}\"} %s.0";
    private static final String TOTAL_CLIENT_MESSAGES_INBOUND_FORMAT = "quarkus_websockets_client_count_total{direction=\"INBOUND\",uri=\"/chat/{username}\"} %s.0";
    private static final String TOTAL_CLIENT_BYTES_OUTBOUND_FORMAT = "quarkus_websockets_client_bytes_total{direction=\"OUTBOUND\",uri=\"/chat/{username}\"} %s.0";
    private static final String TOTAL_CLIENT_BYTES_INBOUND_FORMAT = "quarkus_websockets_client_bytes_total{direction=\"INBOUND\",uri=\"/chat/{username}\"} %s.0";
    private static final String TOTAL_CLIENT_ENDPOINT_ERRORS_FORMAT = "quarkus_websockets_client_endpoint_count_errors_total{uri=\"/pingPong\"} %s.0";

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-micrometer-registry-prometheus"))
    static final RestService app = new RestService()
            .withProperty("quarkus.websockets-next.server.metrics.enabled", "true")
            .withProperty("quarkus.websockets-next.client.metrics.enabled", "true");

    protected RestService getServer() {
        return app;
    }

    @Test
    @Order(1)
    public void serverMessageMetricsTest() throws URISyntaxException, InterruptedException {
        // if the three clients join
        WebSocketTestClient aliceClient = createClient("/chat/alice");
        String aliceJoinedMessage = "alice joined";
        assertMessage(aliceJoinedMessage, aliceClient);
        WebSocketTestClient bobClient = createClient("/chat/bob");
        String bobJoinedMessage = "bob joined";
        assertMessage(bobJoinedMessage, aliceClient, bobClient);
        WebSocketTestClient charlieClient = createClient("/chat/charlie");
        String charlieJoinedMessage = "charlie joined";
        assertMessage(charlieJoinedMessage, aliceClient, bobClient, charlieClient);
        // then client count is three
        thenCounterIs(TOTAL_SERVER_CONNECTIONS_OPEN_FORMAT, 3);
        String helloWorldEnglish = "hello world";
        String aliceHelloWorldEnglish = "alice: " + helloWorldEnglish;
        aliceClient.send(helloWorldEnglish);
        assertMessage(aliceHelloWorldEnglish, aliceClient, bobClient, charlieClient);
        String helloWorldCantonese = "你好世界";
        String aliceHelloWorldCantonese = "alice: " + helloWorldCantonese;
        aliceClient.send(helloWorldCantonese);
        assertMessage(aliceHelloWorldCantonese, aliceClient, bobClient, charlieClient);
        String helloBinary = "hello binary";
        byte[] helloBinaryData = helloBinary.getBytes();
        aliceClient.send(helloBinaryData);
        assertBinaryMessage(helloBinaryData, aliceClient, bobClient, charlieClient);
        int outboundMessagesTotalBytes = aliceJoinedMessage.getBytes(StandardCharsets.UTF_8).length // sent only to alice
                + 2 * bobJoinedMessage.getBytes(StandardCharsets.UTF_8).length // sent to bob and alice
                + 3 * charlieJoinedMessage.getBytes(StandardCharsets.UTF_8).length // sent to charlie, bob and alice
                + 3 * aliceHelloWorldEnglish.getBytes(StandardCharsets.UTF_8).length // sent to charlie, bob and alice
                + 3 * aliceHelloWorldCantonese.getBytes(StandardCharsets.UTF_8).length // sent to charlie, bob and alice
                + 3 * helloBinaryData.length; // sent to charlie, bob and alice
        int inboundMessagesTotalBytes = helloWorldEnglish.getBytes(StandardCharsets.UTF_8).length
                + helloWorldCantonese.getBytes(StandardCharsets.UTF_8).length + helloBinaryData.length;
        thenCounterIs(TOTAL_SERVER_MESSAGES_OUTBOUND_FORMAT, 15);
        thenCounterIs(TOTAL_SERVER_BYTES_OUTBOUND_FORMAT, outboundMessagesTotalBytes);
        thenCounterIs(TOTAL_SERVER_MESSAGES_INBOUND_FORMAT, 3);
        thenCounterIs(TOTAL_SERVER_BYTES_INBOUND_FORMAT, inboundMessagesTotalBytes);
        aliceClient.closeBlocking();
        bobClient.closeBlocking();
        charlieClient.closeBlocking();
        thenCounterIs(TOTAL_SERVER_CONNECTIONS_CLOSED_FORMAT, 3);
    }

    @Test
    @Order(2)
    public void serverErrorMetricsWithoutOnErrorHandlerTest() throws URISyntaxException, InterruptedException {
        String endpointPath = "/failing-onopen-error-no-handler";
        WebSocketTestClient client = createClient(endpointPath);

        // No @OnError present, the onOpen() exception causes a connection failure
        getServer().logs().assertContains("Websocket failed to open");

        // Since there is no error handler, the onOpen error counter must increment
        thenCounterIs(TOTAL_SERVER_CONNECTIONS_ONOPEN_ERRORS_FORMAT, 1);

        // Quarkus counts endpoint errors even if @OnError annotation is not present
        String noHandlerUriFormat = String.format(TOTAL_SERVER_ENDPOINT_ERRORS_FORMAT, endpointPath);
        thenCounterIs(noHandlerUriFormat, 1);

        client.close();
    }

    @Test
    @Order(3)
    public void serverErrorMetricsWithOnErrorHandlerTest() throws URISyntaxException, InterruptedException {
        String endpointPath = "/failing-onopen-error-with-handler";
        WebSocketTestClient client = createClient(endpointPath);

        // The @OnError method handles the exception thrown from onOpen()
        getServer().logs().assertContains("Error on websocket: Websocket failed to open");

        // Because the failure is handled, the onOpen error counter should not appear in metrics
        thenCounterAbsent("quarkus_websockets_server_connections_onopen_errors_total",
                "/failing-onopen-error-with-handler");

        client.send("Create an error");
        getServer().logs().assertContains("Error on websocket: Create an error");

        String handlerUriFormat = String.format(TOTAL_SERVER_ENDPOINT_ERRORS_FORMAT, endpointPath);
        thenCounterIs(handlerUriFormat, 2);

        client.close();
    }

    @Test
    @Order(4)
    public void clientMessageMetricsTest() throws URISyntaxException, InterruptedException {
        WebSocketTestClient client = createClient("/chat/alice");
        assertMessage("alice joined", client);
        // connect server-side client
        given().queryParam("username", "bob").get("/userDataRes/connect"); // "bob joined"
        assertMessage("bob joined", client);
        int inboundClientMessagesTotalBytes = "bob joined".getBytes(StandardCharsets.UTF_8).length;
        try {
            // server-side client will respond to "login" message by sending its username (which is stored in user data)
            client.send("login"); // "alice: login" and "bob: bob"
            assertMessage("alice: login");
            assertMessage("bob: bob");
            inboundClientMessagesTotalBytes = inboundClientMessagesTotalBytes
                    + "alice: login".getBytes(StandardCharsets.UTF_8).length
                    + "bob: bob".getBytes(StandardCharsets.UTF_8).length;
            int outboundClientMessagesTotalBytes = "bob".getBytes(StandardCharsets.UTF_8).length;
            thenCounterIs(TOTAL_CLIENT_CONNECTIONS_OPEN_FORMAT, 1);
            thenCounterIs(TOTAL_CLIENT_BYTES_INBOUND_FORMAT, inboundClientMessagesTotalBytes);
            thenCounterIs(TOTAL_CLIENT_MESSAGES_INBOUND_FORMAT, 3);
            thenCounterIs(TOTAL_CLIENT_BYTES_OUTBOUND_FORMAT, outboundClientMessagesTotalBytes);
            thenCounterIs(TOTAL_CLIENT_MESSAGES_OUTBOUND_FORMAT, 1);
        } finally {
            given().get("/userDataRes/disconnect");
            assertMessage("bob left", client);
            client.close();
        }
        thenCounterIs(TOTAL_CLIENT_CONNECTIONS_CLOSED_FORMAT, 1);
    }

    @Test
    @Order(5)
    public void clientErrorMetricsTest() throws InterruptedException {
        given().get("/pingPongRes/connect");
        Thread.sleep(10000); // client pings every two seconds, third pong response produces RTE
        given().get("/pingPongRes/disconnect");
        thenCounterIs(TOTAL_CLIENT_ENDPOINT_ERRORS_FORMAT, 1);
    }

    private ValidatableResponse callMetrics() {
        return when()
                .get("/q/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    private void thenCounterIs(String counterFormat, int expectedCounter) {
        callMetrics().body(containsString(String.format(counterFormat, expectedCounter)));
    }

    private void thenCounterAbsent(String counterName, String uri) {
        callMetrics().body(not(containsString(counterName + "{uri=\"" + uri + "\"")));
    }

    private URI getUri(String with) throws URISyntaxException {
        return new URI(getServer().getURI(Protocol.WS).toString()).resolve(with);
    }

    private WebSocketTestClient createClient(String endpoint)
            throws URISyntaxException, InterruptedException {
        WebSocketTestClient client = new WebSocketTestClient(getUri(endpoint), false);
        if (!client.connectBlocking()) {
            LOG.error("Websocket client fail to connect to " + endpoint);
        }
        return client;
    }

    private void assertMessage(String expectedMessage, WebSocketTestClient... clients) {
        for (WebSocketTestClient client : clients) {
            Awaitility
                    .await()
                    .atMost(ofSeconds(2))
                    .untilAsserted(() -> assertEquals(expectedMessage, client.waitForAndGetMessage()));
        }
    }

    private void assertBinaryMessage(byte[] expectedMessage, WebSocketTestClient... clients) {
        for (WebSocketTestClient client : clients) {
            Awaitility
                    .await()
                    .atMost(ofSeconds(2))
                    .untilAsserted(() -> assertArrayEquals(expectedMessage, client.waitForAndGetBinaryMessage().array()));
        }
    }

}
