package oom;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.groups.GeneratorEmitter;
import io.smallrye.mutiny.subscription.Cancellable;

@Path("/client/events")
public class EventsClientResource {
    private Cancellable cancellableTask;
    private static final AtomicLong count = new AtomicLong(0);
    @Inject
    @RestClient
    EventsClient client;

    @GET
    public long getResource() {
        return count.get();
    }

    public void startSpamming(@Observes StartupEvent event) {
        Log.info("Starting spamming!");
        Multi<Response> multi = Multi.createFrom().generator(() -> 0L, getMultiEmitterConsumer())
                .onItem().transformToUniAndConcatenate((value) -> client.sendEvent(value))
                .onTermination().invoke(() -> Log.infof("Total events sent: %s", count.get()));

        cancellableTask = multi.subscribe()
                .with(response -> Log.debugf("Event sent with status %s", response.getStatus()),
                        failure -> Log.errorf(failure, "Failure: %s", failure.getMessage()));
    }

    public void onStop(@Observes ShutdownEvent ev) {
        cancellableTask.cancel();
    }

    private static BiFunction<Long, GeneratorEmitter<? super Long>, Long> getMultiEmitterConsumer() {
        return (id, gen) -> {
            gen.emit(id);
            return count.incrementAndGet();
        };
    }

}
