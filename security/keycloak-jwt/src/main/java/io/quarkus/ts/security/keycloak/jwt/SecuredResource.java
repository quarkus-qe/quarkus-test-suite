package io.quarkus.ts.security.keycloak.jwt;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/secured")
@RequestScoped // required! because we're injecting non-proxyable types in the @Dependent scope
public class SecuredResource {
    @Inject
    JsonWebToken jwt;

    @Inject
    @Claim(standard = Claims.iss)
    String issuer;

    @GET
    @Path("/everyone")
    @RolesAllowed("**")
    public String hello(@Context SecurityContext security) {
        return "Hello, " + jwt.getName() + ", your token was issued by " + issuer;
    }

    @GET
    @Path("/admin")
    @RolesAllowed("test-admin-role")
    public String admin() {
        return "Restricted area! Admin access granted!";
    }

    @GET
    @Path("/user")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("test-user-role")
    public String user() {
        return "Hello, user " + jwt.getName();
    }

    @GET
    @Path("/admin-identity")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("test-admin-role")
    public String adminIdentity() {
        return "Hello, user " + jwt.getName();
    }

}
