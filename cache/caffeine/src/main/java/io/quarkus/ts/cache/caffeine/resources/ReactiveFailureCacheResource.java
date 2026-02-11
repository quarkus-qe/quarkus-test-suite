package io.quarkus.ts.cache.caffeine.resources;

import java.util.Set;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.quarkus.ts.cache.caffeine.keys.CollisionTestKey;
import io.quarkus.ts.cache.caffeine.services.ReactiveFailureCacheService;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;

@NonBlocking
@Path("/api/reactive-failure")
public class ReactiveFailureCacheResource {

    @Inject
    ReactiveFailureCacheService cacheService;

    @GET
    @Path("/no-args")
    public Uni<Response> getValueWithFailureNoArgs() {
        return cacheService.noArgs()
                .onItem().transform(data -> Response.ok(data).build());

    }

    @GET
    @Path("/failure/{key}")
    @CacheResult(cacheName = "reactive-failure-cache")
    public Uni<Response> getValueWithFailure(@PathParam("key") @CacheKey String key) {
        return cacheService.getValueWithGlobalFailureCounter(key)
                .onItem().transform(data -> Response.ok(data).build());
    }

    @GET
    @Path("/single-arg/{key}")
    public Uni<Response> getValueWithSingleArgument(@PathParam("key") String key) {
        return cacheService.getSingleArgData(key)
                .onItem().transform(data -> Response.ok(data).build());
    }

    @GET
    @Path("/multi-args")
    public Uni<Response> getValueWithMultipleArguments(
            @QueryParam("param1") String param1,
            @QueryParam("param2") String param2) {

        return cacheService.getMultiArgsData(param1, param2)
                .onItem().transform(data -> Response.ok(data).build());
    }

    @GET
    @Path("/cache-key/{apiKey}/{endpoint}")
    public Uni<Response> getValueWithCacheKeyAnnotation(
            @PathParam("apiKey") String apiKey,
            @PathParam("endpoint") String endpoint) {

        return cacheService.getCacheKeyData(apiKey, endpoint)
                .onItem().transform(data -> Response.ok(data).build());
    }

    @GET
    @Path("/null-value/{id}")
    public Uni<Response> getNullValue(@PathParam("id") String id) {
        return cacheService.getNullValue(id)
                .onItem().transform(data -> Response.noContent().build());
    }

    @GET
    @Path("/eviction/{key}")
    public Uni<Response> getEvictableValue(@PathParam("key") String key) {
        return cacheService.getEvictionData(key)
                .onItem().transform(data -> Response.ok(data).build());
    }

    @GET
    @Path("/cache-keys/{cacheName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getCacheKeys(@PathParam("cacheName") String cacheName) {
        Set<Object> keys = cacheService.getCacheKeys(cacheName);
        return Uni.createFrom().item(Response.ok(keys).build());
    }

    @POST
    @Path("/invalidate/{key}")
    public Uni<Response> invalidateCache(@PathParam("key") String key) {
        return cacheService.invalidateCache(key)
                .onItem().transform(unused -> Response.noContent().build());
    }

    @GET
    @Path("/call-count/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getCallCount(@PathParam("key") String key) {
        int count = cacheService.getCallCount(key);
        return Uni.createFrom().item(Response.ok(count).build());
    }

    @POST
    @Path("/reset")
    public Uni<Response> reset() {
        cacheService.resetCounters();
        return Uni.createFrom().item(Response.noContent().build());
    }

    // Collision case
    @POST
    @Path("/collision/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    @CacheResult(cacheName = "collision-cache")
    public Uni<Response> getValueWithCollisionKey(@PathParam("id") String id) {
        CollisionTestKey key = new CollisionTestKey(id);
        return cacheService.getCollisionData(key)
                .onItem().transform(data -> Response.ok(data).build());
    }

}
