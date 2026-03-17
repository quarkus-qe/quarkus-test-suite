package io.quarkus.ts.security.keycloak.webapp;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/absolute-redirect")
@RolesAllowed("test-user-role")
public class AbsoluteRedirectResource {

    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "absolute-redirect: " + identity.getPrincipal().getName();
    }

    @GET
    @Path("/callback")
    @Produces(MediaType.TEXT_PLAIN)
    public String callback() {
        return "callback: " + identity.getPrincipal().getName();
    }

    @GET
    @Path("/restore-path")
    @Produces(MediaType.TEXT_PLAIN)
    public String restorePath() {
        return "restore-path: " + identity.getPrincipal().getName();
    }
}
