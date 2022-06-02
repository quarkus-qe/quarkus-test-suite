package io.quarkus.ts.messaging.cloudevents.amqpjson;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.ce.impl.DefaultIncomingCloudEventMetadata;

@ApplicationScoped
public class Consumer {

    private static final Logger LOG = Logger.getLogger(Producer.class.getName());

    private final AtomicReference<HashMap<String, String>> results = new AtomicReference<>();

    @Incoming("cloud-events")
    public Uni<Void> process(Message message) {
        int price = Integer.parseInt(message.getPayload().toString());
        LOG.info("process fired: " + price);

        DefaultIncomingCloudEventMetadata<Object> metadata = retrieveCloudEventMetaData(message);

        HashMap<String, String> result = new HashMap<>();
        result.put("source", metadata.getSource().toString());
        result.put("subject", metadata.getSubject().map(Objects::toString).orElse(""));
        result.put("value", metadata.getData().toString());
        result.put("type", metadata.getType());

        this.results.set(result);
        return Uni.createFrom().nothing();
    }

    private DefaultIncomingCloudEventMetadata retrieveCloudEventMetaData(Message message) {
        for (Object metadata : message.getMetadata()) {
            LOG.info("Metadata: " + metadata.getClass());
            if (metadata instanceof DefaultIncomingCloudEventMetadata) {
                return (DefaultIncomingCloudEventMetadata) metadata;
            }
        }
        throw new IllegalStateException("CloudEvent metadata was not found!");
    }

    public HashMap<String, String> getResults() {
        return results.getAcquire();
    }
}
