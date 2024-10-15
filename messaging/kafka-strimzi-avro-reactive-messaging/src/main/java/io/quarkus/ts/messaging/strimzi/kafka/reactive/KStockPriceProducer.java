package io.quarkus.ts.messaging.strimzi.kafka.reactive;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ApplicationNotRunning;
import io.quarkus.ts.messaging.kafka.StockPrice;
import io.quarkus.ts.messaging.kafka.status;
import io.smallrye.common.constraint.NotNull;

@ApplicationScoped
public class KStockPriceProducer {

    private static final Logger LOG = Logger.getLogger(KStockPriceProducer.class);
    private static final int BATCH_SIZE = 100;
    private static final AtomicInteger EXECUTION_COUNT = new AtomicInteger(4);

    @Inject
    @Channel("source-stock-price")
    @OnOverflow(value = OnOverflow.Strategy.DROP)
    Emitter<StockPrice> emitter;

    private Random random = new Random();

    @Scheduled(cron = "{cron.expr}", skipExecutionIf = ApplicationNotRunning.class, concurrentExecution = SKIP)
    public void generate() {
        if (EXECUTION_COUNT.getAndDecrement() > 0) {
            IntStream.range(0, BATCH_SIZE).forEach(next -> {
                StockPrice event = StockPrice.newBuilder()
                        .setId("IBM")
                        .setPrice(random.nextDouble())
                        .setStatus(status.PENDING)
                        .build();
                emitter.send(event).whenComplete(handlerEmitterResponse(KStockPriceProducer.class.getName()));
            });
        }
    }

    @NotNull
    private BiConsumer<Void, Throwable> handlerEmitterResponse(final String owner) {
        return (success, failure) -> {
            if (failure != null) {
                LOG.error(String.format("D'oh! %s", failure.getMessage()));
            }
        };
    }

}
