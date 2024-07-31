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
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;

@NonBlocking
@Path("/api/reactive")
public class ReactiveWithCacheResource {

    private static final String CACHE_NAME = "api-reactive-cache";

    private final AtomicInteger counter = new AtomicInteger(0);

    @GET
    @CacheResult(cacheName = CACHE_NAME)
    public Uni<String> getValue() {
        return Uni.createFrom().item("Value: " + counter.getAndIncrement());
    }

    @POST
    @Path("/invalidate-cache")
    @CacheInvalidate(cacheName = CACHE_NAME)
    public Uni<Void> invalidate() {
        return Uni.createFrom().nullItem();
    }

    @GET
    @Path("/using-prefix/{prefix}")
    @CacheResult(cacheName = CACHE_NAME)
    public Uni<String> getValueWithPrefix(@PathParam("prefix") @CacheKey String prefix) {
        return Uni.createFrom().item(prefix + ": " + counter.getAndIncrement());
    }

    @POST
    @Path("/using-prefix/{prefix}/invalidate-cache")
    @CacheInvalidate(cacheName = CACHE_NAME)
    public Uni<Void> invalidateWithPrefix(@PathParam("prefix") @CacheKey String prefix) {
        return Uni.createFrom().nullItem();
    }

    @POST
    @Path("/invalidate-cache-all")
    @CacheInvalidateAll(cacheName = CACHE_NAME)
    public Uni<Void> invalidateAll() {
        return Uni.createFrom().nullItem();
    }
}
