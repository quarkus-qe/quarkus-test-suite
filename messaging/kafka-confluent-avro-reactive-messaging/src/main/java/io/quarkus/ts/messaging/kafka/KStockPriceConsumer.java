package io.quarkus.ts.messaging.kafka;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class KStockPriceConsumer {

    private static final Logger LOG = Logger.getLogger(KStockPriceConsumer.class);

    @Incoming("channel-stock-price")
    @Outgoing("price-stream")
    @Broadcast
    public String process(StockPrice next) {
        LOG.info(next.getPrice());
        next.setStatus(status.COMPLETED);
        return new JsonObject().put("id", next.getId()).put("price", next.getPrice()).encode();
    }

}
