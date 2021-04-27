package io.quarkus.ts.openshift.messaging.kafka.producer;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.quarkus.ts.openshift.messaging.kafka.aggregator.model.LoginAttempt;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import io.vertx.core.json.Json;

@ApplicationScoped
public class EventsProducer {

    private static final int SEND_EVENT_EVERY_MILLIS = 1000;
    private static final Logger LOG = Logger.getLogger(EventsProducer.class);

    @ConfigProperty(name = "producer.httpCodes")
    List<Integer> httpCodes;

    @ConfigProperty(name = "producer.loginUrls")
    List<String> loginUrls;

    private Random random = new Random();

    @Outgoing("login-http-response-values")
    public Multi<Record<String, String>> generate() {
        return Multi.createFrom().ticks().every(Duration.ofMillis(SEND_EVENT_EVERY_MILLIS))
                .onOverflow().drop()
                .map(tick -> {
                    String loginEndpoint = getRandomEndpointUrl();
                    String loginEndpointEnc = encodeId(loginEndpoint);
                    Integer httpCode = getRandomHttpCode();

                    LOG.infov("Endpoint: {0} ID: {1}, HTTP-code: {2}", loginEndpoint, loginEndpointEnc, httpCode);
                    return Record
                            .of(loginEndpointEnc, Json.encode(new LoginAttempt(loginEndpointEnc, loginEndpoint, httpCode)));
                });
    }

    private String getRandomEndpointUrl() {
        return loginUrls.get(random.nextInt(loginUrls.size()));
    }

    private Integer getRandomHttpCode() {
        return httpCodes.get(random.nextInt(httpCodes.size()));
    }

    private String encodeId(final String id) {
        return Base64.getEncoder().encodeToString(id.getBytes());
    }

}
