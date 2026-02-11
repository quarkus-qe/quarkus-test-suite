package io.quarkus.ts.cache.multiprovider.services;

import java.util.concurrent.atomic.AtomicInteger;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;

public abstract class BaseServiceWithCache {

    private static final String REDIS_CACHE_NAME = "redis-cache";
    private static final String CAFFEINE_CACHE_NAME = "caffeine-cache";

    private static final AtomicInteger counter = new AtomicInteger(0);

    @CacheResult(cacheName = REDIS_CACHE_NAME)
    public String redisGetValue() {
        return "Value: " + counter.getAndIncrement();
    }

    @CacheInvalidate(cacheName = REDIS_CACHE_NAME)
    public void redisInvalidate() {
        // do nothing
    }

    @CacheResult(cacheName = REDIS_CACHE_NAME)
    public String redisGetValueWithPrefix(@CacheKey String prefix) {
        return prefix + ": " + counter.getAndIncrement();
    }

    @CacheInvalidate(cacheName = REDIS_CACHE_NAME)
    public void redisInvalidateWithPrefix(@CacheKey String prefix) {
        // do nothing
    }

    @CacheInvalidateAll(cacheName = REDIS_CACHE_NAME)
    public void redisInvalidateAll() {
        // do nothing
    }

    @CacheResult(cacheName = CAFFEINE_CACHE_NAME)
    public String caffeineGetValue() {
        return "Value: " + counter.getAndIncrement();
    }

    @CacheInvalidate(cacheName = CAFFEINE_CACHE_NAME)
    public void caffeineInvalidate() {
        // do nothing
    }

    @CacheResult(cacheName = CAFFEINE_CACHE_NAME)
    public String caffeineGetValueWithPrefix(@CacheKey String prefix) {
        return prefix + ": " + counter.getAndIncrement();
    }

    @CacheInvalidate(cacheName = CAFFEINE_CACHE_NAME)
    public void caffeineInvalidateWithPrefix(@CacheKey String prefix) {
        // do nothing
    }

    @CacheInvalidateAll(cacheName = CAFFEINE_CACHE_NAME)
    public void caffeineInvalidateAll() {
        // do nothing
    }
}
