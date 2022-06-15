package io.quarkus.ts.opentelemetry;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows does not support Linux Containers / Testcontainers")
public class OpentelemetryIT {
    private static final int GRPC_COLLECTOR_PORT = 14250;

    private Response resp;

    @JaegerContainer(restPort = GRPC_COLLECTOR_PORT, expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(classes = PongResource.class, properties = "pong.properties")
    static final RestService pongservice = new RestService()
            .withProperty("quarkus.application.name", "pongservice")
            .withProperty("quarkus.opentelemetry.tracer.exporter.jaeger.endpoint", jaeger::getRestUrl);

    @QuarkusApplication(classes = { PingResource.class, PingPongService.class })
    static final RestService pingservice = new RestService()
            .withProperty("quarkus.application.name", "pingservice")
            .withProperty("pongservice_url", pongservice::getHost)
            .withProperty("pongservice_port", () -> String.valueOf(pongservice.getPort()))
            .withProperty("quarkus.opentelemetry.tracer.exporter.jaeger.endpoint", jaeger::getRestUrl);

    @Test
    public void testContextPropagation() {
        int pageLimit = 10;
        String operationName = "/ping/pong";
        String[] operations = new String[] { "/ping/pong", "/hello", "/hello" };

        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            whenDoPingPongRequest();
            thenRetrieveTraces(pageLimit, "1h", pingservice.getName(), operationName);
            thenTriggeredOperationsMustBe(containsInAnyOrder(operations));
            thenTraceSpanSizeMustBe(is(3)); // 2 endpoint's + rest client call
        });
    }

    public void whenDoPingPongRequest() {
        given().when()
                .get(pingservice.getHost() + ":" + pingservice.getPort() + "/ping/pong")
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalToIgnoringCase("ping pong"));
    }

    private void thenRetrieveTraces(int pageLimit, String lookBack, String serviceName, String operationName) {
        resp = given().when()
                .queryParam("operation", operationName)
                .queryParam("lookback", lookBack)
                .queryParam("limit", pageLimit)
                .queryParam("service", serviceName)
                .get(jaeger.getTraceUrl());
    }

    private void thenTraceSpanSizeMustBe(Matcher<?> matcher) {
        resp.then().body("data[0].spans.size()", matcher);
    }

    private void thenTriggeredOperationsMustBe(Matcher<?> matcher) {
        resp.then().body("data[0].spans.operationName", matcher);
    }
}
