package io.quarkus.ts.cache.infinispan.services;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.Proto;
import org.infinispan.protostream.annotations.ProtoSchema;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.quarkus.ts.cache.infinispan.cdi.request.context.RequestContextState;
import io.smallrye.mutiny.Uni;

public abstract class BaseServiceWithCache {

    private static final String CACHE_NAME = "service-cache";

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Inject
    RequestContextState requestContextState;

    @CacheResult(cacheName = CACHE_NAME)
    public String getValue() {
        return "Value: " + counter.getAndIncrement();
    }

    @CacheInvalidate(cacheName = CACHE_NAME)
    public void invalidate() {
        // do nothing
    }

    @CacheResult(cacheName = CACHE_NAME)
    public ExpensiveResponse getValueWithPrefix(@CacheKey String prefix) {
        String result = prefix + ": " + counter.getAndIncrement();
        requestContextState.setState(result);
        return new ExpensiveResponse(result);
    }

    @CacheInvalidate(cacheName = CACHE_NAME)
    public void invalidateWithPrefix(@CacheKey String prefix) {
        // do nothing
    }

    @CacheInvalidateAll(cacheName = CACHE_NAME)
    public void invalidateAll() {
        // do nothing
    }

    @Proto
    public record ExpensiveResponse(String result) {
    }

    @ProtoSchema(includeClasses = { ExpensiveResponse.class })
    interface Schema extends GeneratedSchema {
    }

    @CacheResult(cacheName = CACHE_NAME)
    public CompletionStage<ExpensiveResponse> getCompletionStageValueWithPrefix(@CacheKey String prefix) {
        String result = prefix + ": " + counter.getAndIncrement();
        requestContextState.setState(result);
        var expensiveResponse = new ExpensiveResponse(result);
        return Uni.createFrom().item(expensiveResponse)
                // delay so that the response is not completed immediately
                .onItem().delayIt().by(Duration.ofSeconds(2))
                .subscribeAsCompletionStage();
    }
}
