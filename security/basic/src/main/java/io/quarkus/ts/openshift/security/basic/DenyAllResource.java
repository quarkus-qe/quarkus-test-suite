package io.quarkus.ts.openshift.security.basic;

import javax.annotation.security.DenyAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

@Path("/deny-all")
@DenyAll
public class DenyAllResource {
    @GET
    public String get(@Context SecurityContext security) {
        return "This should never happen!";
    }
}
