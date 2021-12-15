package io.quarkus.ts.reactive.database;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.reactive.id.ReactiveIdentifierGenerator;
import org.hibernate.reactive.session.ReactiveConnectionSupplier;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class AuthorIdGenerator implements ReactiveIdentifierGenerator<Integer> {
    private static final int LAST_IMPORTED_ID = 4;
    private final AtomicInteger lastId = new AtomicInteger(LAST_IMPORTED_ID);

    @Override
    public CompletionStage<Integer> generate(ReactiveConnectionSupplier session, Object entity) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        result.complete(lastId.incrementAndGet());
        return result;
    }
}
