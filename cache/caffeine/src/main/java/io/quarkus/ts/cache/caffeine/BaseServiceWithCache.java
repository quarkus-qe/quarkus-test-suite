package io.quarkus.ts.cache.caffeine;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;

public abstract class BaseServiceWithCache {

    private static final String CACHE_NAME = "service-cache";

    private static int counter = 0;

    @CacheResult(cacheName = CACHE_NAME)
    public String getValue() {
        return "Value: " + counter++;
    }

    @CacheInvalidate(cacheName = CACHE_NAME)
    public void invalidate() {
        // do nothing
    }

    @CacheResult(cacheName = CACHE_NAME)
    public String getValueWithPrefix(@CacheKey String prefix) {
        return prefix + ": " + counter++;
    }

    @CacheInvalidate(cacheName = CACHE_NAME)
    public void invalidateWithPrefix(@CacheKey String prefix) {
        // do nothing
    }

    @CacheInvalidateAll(cacheName = CACHE_NAME)
    public void invalidateAll() {
        // do nothing
    }
}
