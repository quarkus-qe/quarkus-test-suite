package io.quarkus.ts.http.graphql;

import static io.quarkus.ts.http.graphql.Utils.createQuery;
import static io.quarkus.ts.http.graphql.Utils.sendQuery;
import static io.restassured.RestAssured.when;
import static org.awaitility.Awaitility.await;

import java.net.HttpURLConnection;
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
public class GraphQLTracingIT {
    private static final String SERVICE_NAME = "graphql-service";

    @JaegerContainer
    static JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.jaeger.service-name", SERVICE_NAME)
            .withProperty("quarkus.jaeger.endpoint", jaeger::getRestUrl);

    @Test
    void verifyTracesInJaegerTest() {
        Response classic = sendQuery(createQuery("friend(name:\"Aristotle\"){name}"));
        Response reactive = sendQuery(createQuery("friend_r(name:\"Aristotle\"){name}"));

        Assertions.assertEquals("Plato", classic.jsonPath().getString("data.friend.name"));
        Assertions.assertEquals("Plato", reactive.jsonPath().getString("data.friend_r.name"));

        // the tracer inside the application doesn't send traces to the Jaeger server immediately,
        // they are batched, so we need to wait a bit
        await().atMost(60, TimeUnit.SECONDS).untilAsserted(() -> {
            Response response = when()
                    .get(jaeger.getTraceUrl() + "?service=" + SERVICE_NAME);
            Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            JsonPath jsonPath = response.body().jsonPath();
            Assertions.assertEquals(2, jsonPath.getList("data").size());
            Assertions.assertEquals(2, jsonPath.getList("data[0].spans").size());
            Assertions.assertEquals(2, jsonPath.getList("data[1].spans").size());
            Assertions.assertTrue(jsonPath.getList("data[0].spans.operationName").contains("GraphQL:Query.friend"));
            Assertions.assertTrue(jsonPath.getList("data[1].spans.operationName").contains("GraphQL:Query.friend_r"));
        });
    }
}
