package io.quarkus.ts.http.graphql.telemetry;

import static io.quarkus.ts.http.graphql.telemetry.Utils.createQuery;
import static io.quarkus.ts.http.graphql.telemetry.Utils.sendQuery;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

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
    private static final int GRPC_COLLECTOR_PORT = 14250;

    @JaegerContainer(restPort = GRPC_COLLECTOR_PORT)
    static JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.opentelemetry.tracer.exporter.jaeger.endpoint", jaeger::getRestUrl);

    @Test
    void verifyTelemetry() {
        Response classic = sendQuery(createQuery("friend(name:\"Aristotle\"){name}"));
        Response reactive = sendQuery(createQuery("friend_r(name:\"Aristotle\"){name}"));

        Assertions.assertEquals("Plato", classic.jsonPath().getString("data.friend.name"));
        Assertions.assertEquals("Plato", reactive.jsonPath().getString("data.friend_r.name"));

        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(10)).untilAsserted(() -> {
            String operation = "/graphql";
            Response traces = given().when()
                    .queryParam("operation", operation)
                    .queryParam("lookback", "1h")
                    .queryParam("limit", 10)
                    .queryParam("service", "graphql-telemetry")
                    .get(jaeger.getTraceUrl());
            JsonPath body = traces.body().jsonPath();
            Assertions.assertEquals(2, body.getList("data").size());
            Assertions.assertEquals(1, body.getList("data[0].spans").size());
            Assertions.assertEquals(1, body.getList("data[1].spans").size());
            Assertions.assertEquals(operation, body.getString("data[0].spans[0].operationName"));
            Assertions.assertEquals(operation, body.getString("data[1].spans[0].operationName"));
        });
    }
}
