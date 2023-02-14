package io.quarkus.ts.infinispan.client;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.infinispan.client.hotrod.RemoteCache;

import io.quarkus.infinispan.client.Remote;

public class InfinispanCounterResource {
    protected AtomicInteger counter = new AtomicInteger(0);

    @Inject
    @Remote("mycache")
    RemoteCache<String, Integer> cache;

    @Path("/get-cache")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Integer getCacheCounter() {
        return cache.get("counter");
    }

    @Path("/get-client")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Integer getClientCounter() {
        return counter.get();
    }

    @Path("/increment-counters")
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    public String incCounters() {
        int invocationClientNumber = counter.incrementAndGet();
        int invocationCacheNumber = cache.get("counter") + 1;
        cache.put("counter", invocationCacheNumber);
        return "Cache=" + invocationCacheNumber + " Client=" + invocationClientNumber;
    }

    @Path("/reset-cache")
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    public String resetCacheCounter() {
        cache.put("counter", 0);
        return "Cache=" + cache.get("counter");
    }

    @Path("/reset-client")
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    public String resetClientCounter() {
        counter.set(0);
        return "Client=" + counter.get();
    }
}
