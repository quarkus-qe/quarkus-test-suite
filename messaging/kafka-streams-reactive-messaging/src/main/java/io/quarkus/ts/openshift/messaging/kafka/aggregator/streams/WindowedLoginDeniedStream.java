package io.quarkus.ts.openshift.messaging.kafka.aggregator.streams;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.time.Duration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.quarkus.kafka.client.serialization.JsonbSerde;
import io.quarkus.ts.openshift.messaging.kafka.aggregator.model.LoginAggregation;
import io.quarkus.ts.openshift.messaging.kafka.aggregator.model.LoginAttempt;
import io.smallrye.reactive.messaging.annotations.Broadcast;

@ApplicationScoped
public class WindowedLoginDeniedStream {

    public static final String LOGIN_AGGREGATION_STORE = "login-aggregation-store";
    public static final String LOGIN_ATTEMPTS_TOPIC = "login-http-response-values";
    public static final String LOGIN_DENIED_AGGREGATED_TOPIC = "login-denied";
    public static final String LOGIN_ALERTS_TOPIC = "login-alerts";

    @ConfigProperty(name = "login.denied.windows.sec")
    int windowsLoginSec;

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        JsonbSerde<LoginAttempt> loginAttemptSerde = new JsonbSerde<>(LoginAttempt.class);
        JsonbSerde<LoginAggregation> loginAggregationSerde = new JsonbSerde<>(LoginAggregation.class);

        builder.stream(LOGIN_ATTEMPTS_TOPIC, Consumed.with(Serdes.String(), loginAttemptSerde))
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(windowsLoginSec)))
                .aggregate(LoginAggregation::new,
                        (id, value, aggregation) -> aggregation.updateFrom(value),
                        Materialized.<String, LoginAggregation, WindowStore<Bytes, byte[]>> as(LOGIN_AGGREGATION_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(loginAggregationSerde))
                .toStream()
                .filter((k, v) -> v.getCode() == UNAUTHORIZED.getStatusCode() || v.getCode() == FORBIDDEN.getStatusCode())
                .to(LOGIN_DENIED_AGGREGATED_TOPIC);

        return builder.build();
    }

    @Incoming(LOGIN_DENIED_AGGREGATED_TOPIC)
    @Outgoing(LOGIN_ALERTS_TOPIC)
    @Broadcast
    public String fanOut(String jsonLoginAggregation) {
        return jsonLoginAggregation;
    }
}
