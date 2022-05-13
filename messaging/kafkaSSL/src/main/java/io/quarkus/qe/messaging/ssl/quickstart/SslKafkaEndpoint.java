package io.quarkus.qe.messaging.ssl.quickstart;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

@Path("/kafka/ssl")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class SslKafkaEndpoint extends KafkaEndpoint {

    @ConfigProperty(name = "kafka.ssl.enable", defaultValue = "false")
    boolean kafkaSslEnabled;

    @Inject
    @Named("kafka-consumer-ssl")
    Provider<KafkaConsumer<String, String>> sslConsumer;

    @Inject
    @Named("kafka-producer-ssl")
    Provider<KafkaProducer<String, String>> sslProducer;

    @Inject
    @Named("kafka-admin-ssl")
    Provider<AdminClient> sslAdmin;

    public void initialize(@Observes StartupEvent ev) {
        if (kafkaSslEnabled) {
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
