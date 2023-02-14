package io.quarkus.ts.security.jwt;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/mixed")
public class MixedResource {
    @GET
    @Path("/constrained")
    @RolesAllowed("**")
    public String constrained() {
        return "Constrained method";
    }

    @GET
    @Path("/unconstrained")
    public String unconstrained() {
        return "Unconstrained method";
    }
}
