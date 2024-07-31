package io.quarkus.ts.cache.infinispan;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;

@Path("/cache")
public class CacheExpirationResource {

    @GET
    @Path("/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    @CacheResult(cacheName = "expiring-cache")
    public String getCachedValue(@CacheKey String key) {
        return "Value for key " + key + " at " + System.currentTimeMillis();
    }
}
