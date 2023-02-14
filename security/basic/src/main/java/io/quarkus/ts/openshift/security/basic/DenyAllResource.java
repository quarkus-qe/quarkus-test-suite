package io.quarkus.ts.openshift.security.basic;

import jakarta.annotation.security.DenyAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/deny-all")
@DenyAll
public class DenyAllResource {
    @GET
    public String get(@Context SecurityContext security) {
        return "This should never happen!";
    }
}
