package io.quarkus.ts.opentelemetry.reactive;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/admin")
@RolesAllowed("admin")
public class AdminResource {

    @GET
    public String get(@Context SecurityContext security) {
        return "Hello, admin " + security.getUserPrincipal().getName();
    }
}
