package io.quarkus.ts.security.oidcclient.mtls;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

@Authenticated
@Path("/auth-mechanism-priority")
public class AuthMechanismPriorityResource {
    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String principal() {
        return identity.getPrincipal().getName();
    }
}
