package io.quarkus.qe.messaging.ssl.quickstart;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

@Path("/kafka/ssl")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class SslKafkaEndpoint extends KafkaEndpoint {

    @Inject
    @Named("kafka-consumer-ssl")
    Provider<KafkaConsumer<String, String>> sslConsumer;

    @Inject
    @Named("kafka-producer-ssl")
    Provider<KafkaProducer<String, String>> sslProducer;

    @Inject
    @Named("kafka-admin-ssl")
    Provider<AdminClient> sslAdmin;

    public void initialize(@Observes StartupEvent ev,
            @ConfigProperty(name = "kafka.security.protocol", defaultValue = "") String kafkaSecurityProtocol) {
        if ("SSL".equals(kafkaSecurityProtocol)) {
            super.initialize(sslConsumer.get());
        }
    }

    @Path("/topics")
    @GET
    public Set<String> getTopics() throws InterruptedException, ExecutionException, TimeoutException {
        return super.getTopics(sslAdmin.get());
    }

    @POST
    public long post(@QueryParam("key") String key, @QueryParam("value") String value)
            throws InterruptedException, ExecutionException, TimeoutException {
        return super.produceEvent(sslProducer.get(), key, value);
    }

    @Override
    @GET
    public String getLast() {
        return super.getLast();
    }

}
