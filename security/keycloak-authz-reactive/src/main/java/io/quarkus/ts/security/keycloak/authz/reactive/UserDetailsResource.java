package io.quarkus.ts.security.keycloak.authz.reactive;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
