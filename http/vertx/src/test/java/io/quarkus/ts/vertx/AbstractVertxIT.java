package io.quarkus.ts.vertx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;
import static org.testcontainers.shaded.org.hamcrest.Matchers.containsString;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;

public abstract class AbstractVertxIT {

    @QuarkusApplication
    static final RestService service = new RestService();

    @Test
    public void httpServerAndMetrics() {
        requests().get("/hello").then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", is("Hello, World!"));

        Response response = requests().get("/q/metrics");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertThat(response.getContentType(), containsString("application/openmetrics-text"));
        String body = response.body().asString();
        Map<String, Metric> metrics = parseMetrics(body);
        assertTrue(metrics.containsKey("worker_pool_active"));
        assertTrue(metrics.containsKey("worker_pool_completed_total"));
        assertTrue(metrics.containsKey("worker_pool_queue_size"));
    }

    @Test
    public void httpServerParsing() {
        requests().get("/hello?name=you").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, you!"));
    }

    @Test
    public void vertxHttpClient() {
        HttpClient httpClient = Vertx.vertx().createHttpClient();
        httpClient.request(HttpMethod.GET, service.getPort(), service.getHost(), "/hello")
                .compose(request -> request.send()
                        .compose(httpClientResponse -> {
                            assertEquals(HttpStatus.SC_OK, httpClientResponse.statusCode());
                            return httpClientResponse.body();
                        }))
                .onSuccess(body -> {
                    assertThat("Body response", body.toString().contains("Hello, World!"));
                }).onFailure(Throwable::getCause);
    }

    @Test
    public void additionalMetrics() {
        requests().get("/hello/bus?name=train").then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", is("Greetings, train"));
        requests().get("/hello/bus?name=ship").then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", is("Greetings, ship"));
        requests().get("/hello/bus?name=plane").then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", is("Greetings, plane"));

        Response response = requests().get("/q/metrics");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertThat(response.getContentType(), containsString("application/openmetrics-text"));
        String body = response.body().asString();
        Map<String, Metric> metrics = parseMetrics(body);
        Metric sentTotal = metrics.get("eventBus_sent_total");
        assertNotNull(sentTotal);
        assertEquals("3.0", sentTotal.getValue());
        assertEquals("greetings", sentTotal.getTags().get("address"));
        Metric delivered = metrics.get("eventBus_delivered");
        assertNotNull(delivered);
        assertEquals("3.0", delivered.getValue());
        assertEquals("greetings", delivered.getTags().get("address"));
        Metric handlers = metrics.get("eventBus_handlers");
        assertNotNull(handlers);
        assertEquals("2.0", handlers.getValue()); //GreetingService and Second  GreetingService
        assertEquals("greetings", handlers.getTags().get("address"));
        Metric discarded = metrics.get("eventBus_discarded");
        assertNotNull(discarded);
        assertEquals("0.0", discarded.getValue());
    }

    public abstract RequestSpecification requests();

    private Map<String, Metric> parseMetrics(String body) {
        Map<String, Metric> metrics = new HashMap<>(128);
        Arrays.stream(body.split("\n"))
                .filter(line -> !line.startsWith("#"))
                .map(Metric::new)
                .forEach(metric -> metrics.put(metric.getName(), metric));
        return metrics;
    }

}
