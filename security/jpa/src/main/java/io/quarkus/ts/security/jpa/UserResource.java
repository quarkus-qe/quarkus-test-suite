package io.quarkus.ts.security.jpa;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import io.smallrye.mutiny.Uni;

@Path("/api/users")
public class UserResource {

    @GET
    @RolesAllowed("user")
    @Path("/me")
    public Uni<String> me(@Context SecurityContext securityContext) {
        return Uni.createFrom().item(securityContext.getUserPrincipal().getName());
    }
}
