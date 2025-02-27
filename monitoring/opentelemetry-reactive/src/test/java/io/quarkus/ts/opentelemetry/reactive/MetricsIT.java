package io.quarkus.ts.opentelemetry.reactive;

import static io.quarkus.test.bootstrap.Protocol.HTTP;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class MetricsIT {
    private static final String OTEL_PING_SERVICE_NAME = "pingservice";

    @QuarkusApplication(classes = { PongResource.class, SchedulerResource.class,
            SchedulerService.class }, properties = "pong.properties")
    static final RestService pongservice = new RestService()
            .withProperty("quarkus.application.name", "pongservice");

    @QuarkusApplication(classes = { PingResource.class, PingPongService.class, AdminResource.class }, dependencies = {
            // Required for metrics. TODO: replace when OTLP gets its own metrics provider
            @Dependency(artifactId = "quarkus-micrometer-registry-prometheus")
    })
    static final RestService pingservice = new RestService()
            .withProperty("pongservice.url", () -> pongservice.getURI(HTTP).getRestAssuredStyleUri())
            .withProperty("pongservice.port", () -> Integer.toString(pongservice.getURI(HTTP).getPort()))
            // verify OTEL service name has priority over default Quarkus application name
            .withProperty("quarkus.otel.service.name", OTEL_PING_SERVICE_NAME);

    @BeforeAll
    static void beforeAll() {
        pingPongRequest();
        pingPongRequest();
    }

    private static void pingPongRequest() {
        given().when()
                .get(pingservice.getURI(HTTP).withPath("/ping/pong").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalToIgnoringCase("ping pong"));
    }

    @Test
    @Tag("QUARKUS-5424")
    public void testExemplars() {
        await().ignoreExceptions().atMost(30, TimeUnit.SECONDS)
                .pollInterval(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Response response = pingservice.given().get("/q/metrics");
                    assertEquals(HttpStatus.SC_OK, response.statusCode());
                    final String metricName = "http_server_requests_seconds_count";
                    final String body = response.body().asString();
                    String metric = Arrays.stream(body.split("\n"))
                            .filter(line -> line.startsWith(metricName))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError(metricName + " was not found in " + body));
                    String[] content = metric.split(" ");
                    assertEquals(6, content.length, "Some values are missing from " + metric);
                    assertEquals("""
                            http_server_requests_seconds_count{method="GET",outcome="SUCCESS",status="200",uri="/ping/pong"}
                            """.strip(), content[0]);

                    assertEquals("2.0", content[1], "Amount of events is wrong");
                    assertEquals("#", content[2], "Unexpected exemplar separator!");
                    assertTrue(content[3].contains("span_id"), "Exemplar doesn't contain span ID");
                    assertTrue(content[3].contains("trace_id"), "Exemplar doesn't contain trace ID");
                    assertEquals("1.0", content[4], "Unexpected exemplar value!");
                });
    }

    @Test
    @Tag("QUARKUS-5637")
    public void testNetty() {
        await().ignoreExceptions().atMost(30, TimeUnit.SECONDS)
                .pollInterval(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Response response = pingservice.given().get("/q/metrics");
                    assertEquals(HttpStatus.SC_OK, response.statusCode());
                    final String metricName = "netty_allocator_memory_used";
                    final String body = response.body().asString();
                    List<String> metrics = Arrays.stream(body.split("\n"))
                            .filter(line -> line.startsWith(metricName))
                            .toList();
                    assertNotEquals(0, metrics.size(), "No Netty metrics");
                    for (String metric : metrics) {
                        String type = metric.split(" ")[0];
                        long params = Arrays.stream(type.substring(type.indexOf('{'), type.indexOf('}'))
                                .split(","))
                                .filter(parameter -> parameter.startsWith("id="))
                                .count();
                        assertEquals(0, params, "There is 'id' parameter in " + metric);
                    }
                });
    }
}
