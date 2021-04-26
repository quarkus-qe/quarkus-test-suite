package io.quarkus.ts.security.jwt;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
