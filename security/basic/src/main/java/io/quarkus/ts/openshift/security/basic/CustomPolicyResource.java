package io.quarkus.ts.openshift.security.basic;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import io.quarkus.vertx.http.security.AuthorizationPolicy;

@Path("/custom")
public class CustomPolicyResource {

    @GET
    @Path("/annotated")
    @AuthorizationPolicy(name = "alfa")
    public String annotated(@Context SecurityContext security) {
        return "Hello, user " + security.getUserPrincipal().getName();
    }

    @GET
    @Path("/properties")
    public String properties(@Context SecurityContext security) {
        return "Hello, user " + security.getUserPrincipal().getName();
    }

    @GET
    @Path("/both")
    @AuthorizationPolicy(name = "alfa")
    public String both(@Context SecurityContext security) {
        return "Hello, user " + security.getUserPrincipal().getName();
    }
}
