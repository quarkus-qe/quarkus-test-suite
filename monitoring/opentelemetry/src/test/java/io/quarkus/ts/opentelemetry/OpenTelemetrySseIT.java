package io.quarkus.ts.opentelemetry;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Disabled("Input from Clement: RESTEasy classic and SSE is barely working, the fact that RESTEasy classic requires a worker " +
        "thread can lead to very annoying issue, we recommend to switch to RESTEasy reactive")
public class OpenTelemetrySseIT {

    @JaegerContainer(useOtlpCollector = true)
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication()
    static RestService app = new RestService()
            .withProperty("quarkus.application.name", "pingpong")
            .withProperty("quarkus.opentelemetry.tracer.exporter.otlp.endpoint", jaeger::getCollectorUrl);

    private static final String PING_ENDPOINT = "/server-sent-events-ping";
    private static final String PONG_ENDPOINT = "/server-sent-events-pong";

    @Test
    public void testServerClientTrace() throws InterruptedException {
        // When calling ping, the rest will invoke also the pong rest endpoint.
        given()
                .when().get(PING_ENDPOINT)
                .then().statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.SERVER_SENT_EVENTS)
                .body(containsString("ping pong"));

        // Then both ping and pong rest endpoints should have the same trace Id.
        String pingTraceId = given()
                .when().get(PING_ENDPOINT + "/lastTraceId")
                .then().statusCode(HttpStatus.SC_OK).and().extract().asString();

        assertTraceIdWithPongService(pingTraceId);

        // Then Jaeger is invoked
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> given()
                .when().get(jaeger.getTraceUrl() + "?traceID=" + pingTraceId)
                .then().statusCode(HttpStatus.SC_OK)
                .and().body(allOf(containsString(PING_ENDPOINT), containsString(PONG_ENDPOINT))));
    }

    protected void assertTraceIdWithPongService(String expected) {
        String pongTraceId = given()
                .when().get(PONG_ENDPOINT + "/lastTraceId")
                .then().statusCode(HttpStatus.SC_OK).and().extract().asString();

        assertEquals(expected, pongTraceId);
    }

}
