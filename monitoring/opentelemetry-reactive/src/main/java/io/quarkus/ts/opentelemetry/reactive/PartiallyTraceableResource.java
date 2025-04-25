package io.quarkus.ts.opentelemetry.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/partially-traceable-hello")
public class PartiallyTraceableResource {

    @GET
    @Path("/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> helloPathParam(@PathParam("name") String name) {
        return Uni.createFrom().item("Traced hello " + name);
    }

    @GET
    @Path("")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> helloQueryParam(@QueryParam("name") String name) {
        if (name == null || name.isEmpty()) {
            return Uni.createFrom().item("Untraced hello anonymous");
        }
        return Uni.createFrom().item("Traced hello " + name);
    }

    @GET
    @Path("/everybody")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> helloEmbedded() {
        return Uni.createFrom().item("Traced hello to everybody!");
    }

}
