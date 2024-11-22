package io.quarkus.ts.vertx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;
import static org.testcontainers.shaded.org.hamcrest.Matchers.containsString;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.URILike;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;

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
        assertTrue(metrics.containsKey("worker_pool_rejected_total"));
    }

    @Test
    public void httpServerParsing() {
        requests().get("/hello?name=you").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, you!"));
    }

    @Test
    public void vertxHttpClient() {
        Vertx vertx = Vertx.vertx();
        assertNotNull(vertx);
        HttpClient httpClient = vertx.createHttpClient();
        assertNotNull(httpClient);
        URILike uri = service.getURI();
        httpClient.request(HttpMethod.GET, uri.getPort(), uri.getHost(), "/hello")
                .compose(request -> request.send()
                        .compose(httpClientResponse -> {
                            assertEquals(HttpStatus.SC_OK, httpClientResponse.statusCode());
                            return httpClientResponse.body();
                        }))
                .onSuccess(body -> {
                    assertThat("Body response", body.toString().contains("Hello, World!"));
                }).onFailure(Throwable::printStackTrace);

        httpClient.close();
        vertx.close();
    }

    @Test
    public void vertxHttpClientProtocolHttp2() {
        HttpClientOptions httpClientOptions = new HttpClientOptions().setProtocolVersion(HttpVersion.HTTP_2);
        Vertx vertx = Vertx.vertx();
        assertNotNull(vertx);
        HttpClient httpClient = vertx.createHttpClient(httpClientOptions);
        assertNotNull(httpClient);

        URILike uri = service.getURI(Protocol.NONE);
        httpClient.request(HttpMethod.GET, uri.getPort(), uri.getHost(), "/hello")
                .compose(httpClientRequest -> httpClientRequest.send()
                        .compose(httpClientResponse -> {
                            assertEquals(HttpVersion.HTTP_2, httpClientResponse.version());
                            return httpClientResponse.body();
                        }))
                .onSuccess(body -> {
                    assertThat("Body response", body.toString().contains("Hello, World!"));
                }).onFailure(Throwable::printStackTrace);

        httpClient.close();
        vertx.close();
    }

    @Test
    public void vertxHttpClientWithNameParameter() {
        JsonObject jsonObject = new JsonObject().put("name", "Bender");
        Vertx vertx = Vertx.vertx();
        assertNotNull(vertx);
        HttpClient httpClient = vertx.createHttpClient();
        assertNotNull(httpClient);

        URILike uri = service.getURI();
        httpClient.request(HttpMethod.GET, uri.getPort(), uri.getHost(),
                "/hello?name=" + jsonObject.getString("name"))
                .compose(HttpClientRequest::send)
                .compose(HttpClientResponse::body)
                .onSuccess(body -> {
                    assertThat("Body response", body.toString().contains("Hello, Bender!"));
                })
                .onFailure(Throwable::printStackTrace);

        httpClient.close();
        vertx.close();
    }

    public abstract RequestSpecification requests();

    private static Map<String, Metric> parseMetrics(String body) {
        Map<String, Metric> metrics = new HashMap<>(128);
        Arrays.stream(body.split("\n"))
                .filter(line -> !line.startsWith("#"))
                .map(Metric::new)
                .forEach(metric -> metrics.put(metric.name, metric));
        return metrics;
    }

    static class Metric {
        private final String value;
        private final String name;
        private final String object;

        /**
         *
         * @param source metric from the file, eg:
         *        worker_pool_queue_size{pool_name="vert.x-internal-blocking",pool_type="worker"} 0.0
         *        content in curly brackets is ignored (for now)
         *        since we do not care about values, we store them as strings, and ignore duplicated keys.
         */
        public Metric(String source) {
            final int DEFAULT = -1;
            int space = DEFAULT;
            int closing = DEFAULT;
            int opening = DEFAULT;
            byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
            for (int i = bytes.length - 1; i >= 0; i--) {
                byte current = bytes[i];
                if (current == ' ' && space == DEFAULT) {
                    space = i;
                }
                if (current == '}' && closing == DEFAULT) {
                    closing = i;
                }
                if (current == '{' && opening == DEFAULT) {
                    opening = i;
                }
            }
            String key;
            if (space > 0) {
                value = source.substring(space);
                key = source.substring(0, space);
            } else {
                throw new IllegalArgumentException("Metric " + source + " doesn't contain a value");
            }
            if (closing < space && opening < closing && opening > 0) {
                name = source.substring(0, opening);
                object = source.substring(opening, closing + 1);
            } else {
                name = key;
                object = null;
            }
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public String getObject() {
            return object;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Metric metric = (Metric) o;
            return value.equals(metric.value) && name.equals(metric.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, name);
        }
    }
}
