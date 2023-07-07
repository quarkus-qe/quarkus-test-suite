package io.quarkus.ts.http.graphql.telemetry;

import static io.quarkus.ts.http.graphql.telemetry.Utils.createQuery;
import static io.quarkus.ts.http.graphql.telemetry.Utils.sendQuery;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@QuarkusScenario
public class GraphQLTelemetryIT {

    @JaegerContainer(useOtlpCollector = true)
    static JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    @Test
    void verifyTelemetry() {
        Response classic = sendQuery(createQuery("friend(name:\"Aristotle\"){name}"));
        Response reactive = sendQuery(createQuery("friend_r(name:\"Aristotle\"){name}"));

        Assertions.assertEquals("Plato", classic.jsonPath().getString("data.friend.name"));
        Assertions.assertEquals("Plato", reactive.jsonPath().getString("data.friend_r.name"));

        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(10)).untilAsserted(() -> {
            String operation = "POST /graphql";
            Response traces = given().when()
                    .queryParam("operation", operation)
                    .queryParam("lookback", "1h")
                    .queryParam("limit", 10)
                    .queryParam("service", "graphql-telemetry")
                    .get(jaeger.getTraceUrl());
            Assertions.assertEquals(HttpURLConnection.HTTP_OK, traces.statusCode());
            JsonPath body = traces.body().jsonPath();
            Assertions.assertEquals(2, body.getList("data").size());
            Assertions.assertEquals(2, body.getList("data[0].spans").size());
            Assertions.assertEquals(2, body.getList("data[1].spans").size());
            // Span with "operationName": "GraphQL" is child of the span with "operationName": "POST /graphql"
            Assertions.assertTrue(body.getString("data[0].spans[0].operationName").equals(operation) ||
                    body.getString("data[0].spans[0].operationName").equals("GraphQL"));
            Assertions.assertTrue(body.getString("data[1].spans[0].operationName").equals(operation) ||
                    body.getString("data[1].spans[0].operationName").equals("GraphQL"));
        });
    }
}
