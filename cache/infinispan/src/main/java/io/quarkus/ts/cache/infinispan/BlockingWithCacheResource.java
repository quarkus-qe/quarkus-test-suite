package io.quarkus.ts.cache.infinispan;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.smallrye.common.annotation.Blocking;

@Blocking
@Path("/api/blocking")
public class BlockingWithCacheResource {

    private static final String CACHE_NAME = "api-blocking-cache";

    private final AtomicInteger counter = new AtomicInteger(0);

    @GET
    @CacheResult(cacheName = CACHE_NAME)
    public String getValue() {
        return "Value: " + counter.getAndIncrement();
    }

    @POST
    @Path("/invalidate-cache")
    @CacheInvalidate(cacheName = CACHE_NAME)
    public void invalidate() {
        // do nothing
    }

    @GET
    @Path("/using-prefix/{prefix}")
    @CacheResult(cacheName = CACHE_NAME)
    public String getValueWithPrefix(@PathParam("prefix") @CacheKey String prefix) {
        return prefix + ": " + counter.getAndIncrement();
    }

    @POST
    @Path("/using-prefix/{prefix}/invalidate-cache")
    @CacheInvalidate(cacheName = CACHE_NAME)
    public void invalidateWithPrefix(@PathParam("prefix") @CacheKey String prefix) {
        // do nothing
    }

    @POST
    @Path("/invalidate-cache-all")
    @CacheInvalidateAll(cacheName = CACHE_NAME)
    public void invalidateAll() {
        // do nothing
    }
}
