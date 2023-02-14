package io.quarkus.ts.security.keycloak.oidcclient.reactive.basic;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/secured")
public class SecuredResource {
    @Inject
    SecurityIdentity identity;

    @GET
    @RolesAllowed("**")
    public String get() {
        return "Hello, user " + identity.getPrincipal().getName();
    }
}
