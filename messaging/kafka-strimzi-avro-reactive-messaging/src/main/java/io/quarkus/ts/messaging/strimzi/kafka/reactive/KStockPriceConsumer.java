package io.quarkus.ts.messaging.strimzi.kafka.reactive;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.quarkus.ts.messaging.kafka.StockPrice;
import io.quarkus.ts.messaging.kafka.status;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordBatchMetadata;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class KStockPriceConsumer {
    private static final Logger LOG = Logger.getLogger(KStockPriceConsumer.class);

    @Incoming("channel-stock-price")
    @Outgoing("price-stream")
    @Broadcast
    public String process(StockPrice next) {
        eventCompleted(next);
        return toJson(next);
    }

    @Incoming("channel-stock-price-batch")
    @Outgoing("price-stream-batch")
    @Broadcast
    public List<String> processBatch(ConsumerRecords<Integer, StockPrice> records,
            IncomingKafkaRecordBatchMetadata<Integer, StockPrice> metadata) {
        return StreamSupport.stream(records.spliterator(), false)
                .peek(record -> {
                    TracingMetadata meta = metadata.getMetadataForRecord(record, TracingMetadata.class);
                    LOG.debug(meta.getCurrentContext());
                })
                .map(ConsumerRecord::value)
                .map(KStockPriceConsumer::eventCompleted)
                .map(KStockPriceConsumer::toJson)
                .collect(Collectors.toList());
    }

    private static StockPrice eventCompleted(StockPrice price) {
        price.setStatus(status.COMPLETED);
        return price;
    }

    private static String toJson(StockPrice price) {
        return new JsonObject().put("id", price.getId()).put("price", price.getPrice()).encode();
    }
}
