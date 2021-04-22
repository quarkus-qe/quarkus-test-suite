package io.quarkus.ts.openshift.security.basic;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

@Path("/user")
@RolesAllowed("user")
public class UserResource {
    @GET
    public String get(@Context SecurityContext security) {
        return "Hello, user " + security.getUserPrincipal().getName();
    }
}
