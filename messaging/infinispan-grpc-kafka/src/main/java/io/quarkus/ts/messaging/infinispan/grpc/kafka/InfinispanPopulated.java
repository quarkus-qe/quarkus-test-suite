package io.quarkus.ts.messaging.infinispan.grpc.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class InfinispanPopulated {

    private static final Logger LOGGER = Logger.getLogger(InfinispanPopulated.class);

    @Inject
    @Remote("mycache")
    RemoteCache<String, String> cache;

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Populate cache named mycache");
        cache.put("hello", "Hello World, Infinispan is up!");
    }
}
