package io.quarkus.ts.security.keycloak.jwt;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/secured")
@RequestScoped // required! because we're injecting unproxyable types in the @Dependent scope
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
}
