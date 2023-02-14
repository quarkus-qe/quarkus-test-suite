package io.quarkus.ts.security.keycloak.authz.reactive;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

public class UserDetailsResource<T> {

    @Inject
    SecurityIdentity identity;

    @Path("/advanced")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Uni<String> getUserName(T body) {
        return Uni.createFrom().item(identity.getPrincipal().getName());
    }

    @Path("/advanced-specific")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Uni<String> WebAuthnSpecific(String body) {
        return Uni.createFrom().item(identity.getPrincipal().getName());
    }
}
