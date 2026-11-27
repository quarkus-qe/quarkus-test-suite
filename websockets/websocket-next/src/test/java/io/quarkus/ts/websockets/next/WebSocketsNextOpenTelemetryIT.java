package io.quarkus.ts.websockets.next;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.websockets.next.client.WebSocketTestClient;
import io.restassured.response.Response;

@QuarkusScenario
@Tag("QUARKUS-5658")
public class WebSocketsNextOpenTelemetryIT {

    private static final Logger LOG = Logger.getLogger(WebSocketsNextOpenTelemetryIT.class);
    private Response resp;
    // Jaeger v3 API requires a time window (v1 API did not)
    String startMin = Instant.now().minus(1, ChronoUnit.HOURS).toString();
    String startMax = Instant.now().plus(1, ChronoUnit.HOURS).toString();

    @JaegerContainer(useOtlpCollector = true)
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-opentelemetry"))
    static final RestService app = new RestService()
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d");

    protected RestService getServer() {
        return app;
    }

    // {name="OPEN /chat/:username" && resource.service.name="websocket-next"}
    @Test
    public void testWebSocketsNextTracing() throws URISyntaxException, InterruptedException {
        String serviceName = "websocket-next";
        String openOperationName = "OPEN /chat/:username";
        String closeOperationName = "CLOSE /chat/:username";

        // client CLOSE trace should link to the OPEN trace via FOLLOWS_FROM reference
        WebSocketTestClient aliceClient = createClient("/chat/alice");
        aliceClient.send("hello world");
        aliceClient.close();

        // we use trace ID referred from CLOSE trace to verify OPEN trace
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            // there should be one OPEN operation and one CLOSE operation trace
            retrieveTraces(serviceName, openOperationName);
            thenNumberOfTraces(is(1));
            retrieveTraces(serviceName, closeOperationName);
            thenNumberOfTraces(is(1));
        });

        // CLOSE trace should have a reference to OPEN trace of FOLLOWS_FROM type
        // Jaeger v3 (OTLP) exposes the reference as a span link carrying the traceId
        Map<String, String> links = given().queryParam("query.operation_name", closeOperationName)
                .queryParam("lookback", "1h")
                .queryParam("limit", 10)
                .queryParam("query.service_name", serviceName)
                .queryParam("query.start_time_min", startMin)
                .queryParam("query.start_time_max", startMax)
                .get(jaeger.getTraceV3Url()).path("result.resourceSpans[0].scopeSpans[0].spans[0].links[0]");
        String referenceTraceId = links.get("traceId");

        // assert that the linked traceId equals the OPEN operation traceId
        String openOperationTraceIds = given().queryParam("query.operation_name", openOperationName)
                .queryParam("lookback", "1h")
                .queryParam("limit", 10)
                .queryParam("query.service_name", serviceName)
                .queryParam("query.start_time_min", startMin)
                .queryParam("query.start_time_max", startMax)
                .get(jaeger.getTraceV3Url()).path("result.resourceSpans[0].scopeSpans[0].spans[0].traceId");
        assertEquals(referenceTraceId, openOperationTraceIds);
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

    private void retrieveTraces(String serviceName, String operationName) {
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    resp = given().when()
                            .queryParam("query.operation_name", operationName)
                            .queryParam("lookback", "1h")
                            .queryParam("limit", 10)
                            .queryParam("query.service_name", serviceName)
                            .queryParam("query.start_time_min", startMin)
                            .queryParam("query.start_time_max", startMax)
                            .get(jaeger.getTraceV3Url());
                    List list = resp.jsonPath().getList("result.resourceSpans.scopeSpans.spans.flatten()");
                    return list != null && !list.isEmpty();
                });
    }

    private void thenNumberOfTraces(Matcher<?> matcher) {
        resp.then().body("result.resourceSpans.size()", matcher);
    }

}
