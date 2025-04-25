package io.quarkus.ts.opentelemetry.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/untraceable-hello")
public class UntraceableResource {

    @GET
    @Path("/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> helloPathParam(@PathParam("name") String name) {
        return Uni.createFrom().item("Untraced hello " + name);
    }

    @GET
    @Path("")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> helloQueryParam(@QueryParam("name") String name) {
        if (name == null || name.isEmpty()) {
            return Uni.createFrom().item("Untraced hello anonymous");
        }
        return Uni.createFrom().item("Untraced hello " + name);
    }

    @GET
    @Path("/everybody")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> helloEmbedded() {
        return Uni.createFrom().item("Untraced hello to everybody!");
    }

}
