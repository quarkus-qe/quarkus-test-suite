package io.quarkus.ts.cache.caffeine.services;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheManager;
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.ts.cache.caffeine.keys.CollisionTestKey;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ReactiveFailureCacheService {
    private static final Logger LOG = Logger.getLogger(ReactiveFailureCacheService.class);

    private static final String CACHE_NAME = "reactive-failure-cache";
    private static final String EVICTION_CACHE_NAME = "eviction-test-cache";
    private static final String COLLISION_CACHE_NAME = "collision-cache";

    private final AtomicInteger noArgsCounter = new AtomicInteger(0);
    private final Map<String, Integer> callCounters = new ConcurrentHashMap<>();
    private final AtomicInteger evictionCounter = new AtomicInteger(0);
    private final AtomicInteger globalFailureCounter = new AtomicInteger(0);

    @Inject
    CacheManager cacheManager;

    @CacheResult(cacheName = CACHE_NAME)
    public Uni<String> noArgs() {
        int count = noArgsCounter.incrementAndGet();
        if (count == 1) {
            return Uni.createFrom().failure(
                    new CacheTestException("Simulated service not ready on startup for no-args method"));
        }
        return Uni.createFrom().item("Success Service ready - no args");
    }

    public Uni<String> getValueWithGlobalFailureCounter(String key) {
        int currentCounter = globalFailureCounter.getAndIncrement();
        if (currentCounter == 0) {
            return Uni.createFrom().failure(new RuntimeException("Simulated failure for key: " + key));
        } else {
            return Uni.createFrom().item("Success for key: " + key);
        }
    }

    public Set<Object> getCacheKeys(String cacheName) {
        return cacheManager.getCache(cacheName)
                .map(cache -> cache.as(CaffeineCache.class).keySet())
                .orElse(Set.of());
    }

    @CacheResult(cacheName = CACHE_NAME)
    public Uni<String> getSingleArgData(String key) {
        LOG.infof("Thread '%s' ENTERING getSingleArgData for key '%s'",
                Thread.currentThread().getName(), key);
        int count = incrementCounter(key);

        LOG.infof("Thread '%s' -> Counter for key '%s' is now %d",
                Thread.currentThread().getName(), key, count);

        if (count == 1) {
            return Uni.createFrom().failure(
                    new CacheTestException("Service temporarily unavailable"));
        }
        LOG.infof("Thread '%s' -> SUCCESS for key '%s'",
                Thread.currentThread().getName(), key);
        return Uni.createFrom().item("Success for key: " + key);
    }

    @CacheResult(cacheName = CACHE_NAME)
    public Uni<String> getMultiArgsData(String param1, String param2) {
        String combinedKey = param1 + ":" + param2;
        int count = incrementCounter(combinedKey);

        if (count == 1) {
            return Uni.createFrom().failure(
                    new CacheTestException("Connection timeout"));
        }

        return Uni.createFrom().item("Result for: " + combinedKey);
    }

    @CacheResult(cacheName = CACHE_NAME)
    public Uni<String> getCacheKeyData(@CacheKey String apiKey, String endpoint) {
        int count = incrementCounter(apiKey);

        if (count == 1) {
            return Uni.createFrom().failure(
                    new CacheTestException("Rate limit exceeded"));
        }

        return Uni.createFrom().item("API response for key: " + apiKey);
    }

    @CacheResult(cacheName = CACHE_NAME)
    public Uni<String> getNullValue(String id) {
        incrementCounter("null-" + id);
        return Uni.createFrom().nullItem();
    }

    /**
     * Method for cache eviction testing
     * Uses a separate cache with maximum size of 2
     */
    @CacheResult(cacheName = EVICTION_CACHE_NAME)
    public Uni<String> getEvictionData(String key) {
        String value = "Value-" + evictionCounter.incrementAndGet() + "-key:" + key;
        return Uni.createFrom().item(value);
    }

    @CacheInvalidate(cacheName = CACHE_NAME)
    public Uni<Void> invalidateCache(@CacheKey String key) {
        // @CacheInvalidate annotation handles the cache clearing
        try {
            LOG.infof("Invalidating cache for key: %s", key);
        } catch (Exception e) {
            LOG.errorf(e, "Error invalidating cache for key: %s", key);
            throw new RuntimeException("Failed to invalidate cache for key: " + key, e);
        }
        return Uni.createFrom().voidItem();
    }

    @CacheResult(cacheName = COLLISION_CACHE_NAME)
    public Uni<String> getCollisionData(CollisionTestKey key) {
        String uniqueValue = "Value-for-" + key.getId() + "-" + System.currentTimeMillis();
        return Uni.createFrom().item(uniqueValue);
    }

    public int getCallCount(String key) {
        if ("no-args".equals(key)) {
            return noArgsCounter.get();
        }
        return callCounters.getOrDefault(key, 0);
    }

    public void resetCounters() {
        noArgsCounter.set(0);
        callCounters.clear();
        evictionCounter.set(0);
        globalFailureCounter.set(0);
    }

    private int incrementCounter(String key) {
        return callCounters.merge(key, 1, Integer::sum);
    }

    public static class CacheTestException extends RuntimeException {
        public CacheTestException(String message) {
            super(message);
        }
    }
}
