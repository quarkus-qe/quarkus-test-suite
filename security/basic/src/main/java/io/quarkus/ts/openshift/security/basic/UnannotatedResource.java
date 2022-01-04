package io.quarkus.ts.openshift.security.basic;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

@Path("/unannotated")
public class UnannotatedResource {
    @GET
    public String get(@Context SecurityContext security) {
        return "Hello!";
    }
}
