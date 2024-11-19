package io.quarkus.ts.cache.caffeine.restclient.types;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/book")
public class BookAsJsonResource {

    public static int counter = 0;

    @GET
    @Path("/json-cache")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCache() throws InterruptedException {
        counter++;
        return "{\"title\":\"Title in Json with counter equal to " + counter + "\"}";
    }

    @GET
    @Path("/json-cache-invalidate")
    public String invalidateCache() {
        return "json cache was invalidated";
    }

    @GET
    @Path("/reset-counter-json")
    public String resetCounter() {
        counter = 0;
        return "Counter reset";
    }
}
