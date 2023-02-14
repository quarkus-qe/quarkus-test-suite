package io.quarkus.ts.openshift.security.basic;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/unannotated")
public class UnannotatedResource {
    @GET
    public String get(@Context SecurityContext security) {
        return "Hello!";
    }
}
