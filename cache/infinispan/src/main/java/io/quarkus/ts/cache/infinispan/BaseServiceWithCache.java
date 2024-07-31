package io.quarkus.ts.cache.infinispan;

import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.Proto;
import org.infinispan.protostream.annotations.ProtoSchema;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;

public abstract class BaseServiceWithCache {

    private static final String CACHE_NAME = "service-cache";

    private static final AtomicInteger counter = new AtomicInteger(0);

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
        return new ExpensiveResponse(prefix + ": " + counter.getAndIncrement());
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
}
