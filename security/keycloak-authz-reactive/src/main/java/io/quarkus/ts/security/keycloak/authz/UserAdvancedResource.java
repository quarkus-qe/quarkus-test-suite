package io.quarkus.ts.security.keycloak.authz;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
