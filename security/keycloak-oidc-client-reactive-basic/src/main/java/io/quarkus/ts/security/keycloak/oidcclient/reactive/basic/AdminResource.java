package io.quarkus.ts.security.keycloak.oidcclient.reactive.basic;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/admin")
@RolesAllowed("test-admin-role")
public class AdminResource {
    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "Hello, admin " + identity.getPrincipal().getName();
    }
}
