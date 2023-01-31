package io.quarkus.ts.vertx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractVertxIT {

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

    public abstract RequestSpecification requests();

    private Map<String, Metric> parseMetrics(String body) {
        Map<String, Metric> metrics = new HashMap<>(128);
        Arrays.stream(body.split("\n"))
                .filter(line -> !line.startsWith("#"))
                .map(Metric::new)
                .forEach(metric -> metrics.put(metric.name, metric));
        return metrics;
    }

    private class Metric {
        private final String value;
        private final String name;

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
            } else {
                name = key;
            }
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
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
