package io.quarkus.ts.messaging.kafka.producer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class KafkaBlockingProducer {

    @Inject
    @Channel("test")
    MutinyEmitter<String> emitter;

    public void pushEvent(final RoutingContext context) {
        Long startMs = System.currentTimeMillis();
        emitter.send("ping")
                .onFailure().invoke(exception -> {
                    long endMs = System.currentTimeMillis() - startMs;
                    context.response()
                            .setStatusCode(408)
                            .putHeader("x-ms", String.valueOf(endMs))
                            .putHeader("Content-Type", "application/json")
                            .end(exception.getMessage());
                })
                .subscribe().with(resp -> {
                    long endMs = System.currentTimeMillis() - startMs;
                    context.response()
                            .putHeader("x-ms", String.valueOf(endMs))
                            .putHeader("Content-Type", "application/json")
                            .end("success");
                });

    }

    public void pushEventToTopic(final RoutingContext context) {
        Long startMs = System.currentTimeMillis();
        String topic = context.request().getParam("topic");
        OutgoingKafkaRecordMetadata<?> metadata = OutgoingKafkaRecordMetadata.builder()
                .withTopic(topic)
                .withKey(UUID.randomUUID().toString())
                .build();

        Message<String> msg = Message.of("ping")
                .withAck(handlerSuccess(context, startMs))
                .withNack(handlerError(context, startMs))
                .addMetadata(metadata);

        emitter.send(msg);
    }

    private Function<Throwable, CompletionStage<Void>> handlerError(RoutingContext context, Long startMs) {
        return exception -> {
            long endMs = System.currentTimeMillis() - startMs;
            context.response()
                    .setStatusCode(408)
                    .putHeader("x-ms", "" + endMs)
                    .putHeader("Content-Type", "application/json")
                    .end(exception.getMessage());

            return CompletableFuture.completedFuture(null);
        };
    }

    private Supplier<CompletionStage<Void>> handlerSuccess(RoutingContext context, Long startMs) {
        return () -> {
            long endMs = System.currentTimeMillis() - startMs;
            context.response()
                    .putHeader("x-ms", "" + endMs)
                    .putHeader("Content-Type", "application/json")
                    .end("success");

            return CompletableFuture.completedFuture(null);
        };
    }
}
