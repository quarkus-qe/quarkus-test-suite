package io.quarkus.ts.security.jpa;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/api/admin")
public class AdminResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> adminResource() {
        return Uni.createFrom().item("admin");
    }
}
