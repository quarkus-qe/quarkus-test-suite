package io.quarkus.ts.openshift.security.basic;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/permit-all")
@PermitAll
public class PermitAllResource {
    @GET
    public String get(@Context SecurityContext security) {
        return "Hello!";
    }
}
