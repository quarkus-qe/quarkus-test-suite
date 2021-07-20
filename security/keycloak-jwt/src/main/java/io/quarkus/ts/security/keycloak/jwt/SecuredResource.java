package io.quarkus.ts.security.keycloak.jwt;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

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
