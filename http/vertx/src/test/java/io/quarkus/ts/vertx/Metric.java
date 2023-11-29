package io.quarkus.ts.vertx;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.smallrye.mutiny.tuples.Tuple2;

class Metric {
    private final String name;
    private final String value;
    private final Map<String, String> tags;
    private final String object;

    /**
     * @param source metric from the file, eg:
     *        worker_pool_queue_size{pool_name="vert.x-internal-blocking",pool_type="worker"} 0.0
     *        since we do not care about values, we store them as strings, and ignore duplicated keys.
     */
    public Metric(String source) {
        try {
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
                value = source.substring(space + 1);
                key = source.substring(0, space);
            } else {
                throw new IllegalArgumentException("Metric " + source + " doesn't contain a value");
            }
            boolean withBrackets = closing < space && opening < closing && opening > 0;
            if (withBrackets) {
                name = source.substring(0, opening);
                object = source.substring(opening, closing + 1);
            } else {
                name = key;
                object = null;
            }
            if (withBrackets) {
                String params = source.substring(opening + 1, closing);
                tags = Arrays.stream(params.split("\","))
                        .map(pair -> {
                            return pair.split("=");
                        })
                        .map(pair -> Tuple2.of(pair[0], pair[1]))
                        .map(tuple -> tuple.mapItem2(value -> value.replace('"', '\0').trim())) // "value"->value
                        .collect(Collectors.toMap(Tuple2::getItem1, Tuple2::getItem2));
            } else {
                tags = Collections.emptyMap();
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse metric " + source, ex);
        }
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getTags() {
        return tags;
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
        return Objects.equals(name, metric.name) && Objects.equals(value, metric.value) && Objects.equals(tags, metric.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, tags);
    }
}
