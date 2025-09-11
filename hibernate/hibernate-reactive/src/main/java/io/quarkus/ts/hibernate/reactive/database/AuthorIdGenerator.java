package io.quarkus.ts.hibernate.reactive.database;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.reactive.id.ReactiveIdentifierGenerator;
import org.hibernate.reactive.session.ReactiveConnectionSupplier;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class AuthorIdGenerator implements ReactiveIdentifierGenerator<Integer> {
    private static final int LAST_IMPORTED_ID = 4;
    private final AtomicInteger lastId = new AtomicInteger(LAST_IMPORTED_ID);

    @Override
    public boolean generatedOnExecution() {
        return false;
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    @Override
    public CompletionStage<Integer> generate(ReactiveConnectionSupplier session, Object entity) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        result.complete(lastId.incrementAndGet());
        return result;
    }
}
