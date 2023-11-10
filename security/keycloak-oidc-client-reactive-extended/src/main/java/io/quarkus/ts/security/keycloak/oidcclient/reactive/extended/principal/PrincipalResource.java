package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.principal;

import java.security.Principal;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.security.Authenticated;

@Path("/principal")
@Authenticated
public class PrincipalResource {

    @Inject
    Principal principal;

    @GET
    public String principalName() {
        return principal.getName();
    }
}
