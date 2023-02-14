package io.quarkus.ts.security.keycloak.authz.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;

@Path("/user-details")
@Authenticated
public class UserAdvancedResource extends UserDetailsResource<String> {
    @GET
    public Uni<String> info() {
        return Uni.createFrom().item(identity.getPrincipal().getName());
    }
}
