package io.quarkus.ts.security.keycloak.oidcclient.reactive.basic;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/default")
@RolesAllowed("default-roles-test-realm")
public class DefaultRoleResource {

    @Inject
    SecurityIdentity identity;

    @GET
    public String get() {
        return "Hello, user " + identity.getPrincipal().getName();
    }
}
