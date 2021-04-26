package io.quarkus.ts.security.keycloak.oidcclient;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
