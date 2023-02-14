package io.quarkus.ts.security.jwt;

import java.util.Set;

import jakarta.annotation.security.DenyAll;
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
@DenyAll
@RequestScoped // required! because we're injecting unproxyable types in the @Dependent scope
public class SecuredResource {
    @Inject
    JsonWebToken jwt;

    @Inject
    @Claim(standard = Claims.iss)
    String issuer;

    @Inject
    @Claim(standard = Claims.groups)
    Set<String> groups;

    @GET
    @Path("/everyone")
    @RolesAllowed("**")
    public String hello(@Context SecurityContext security) {
        return "Hello, " + jwt.getName() + ", your token was issued by " + issuer + " and you are in groups " + groups;
    }

    @GET
    @Path("/admin")
    @RolesAllowed("admin")
    public String admin() {
        return "Restricted area! Admin access granted!";
    }

    @GET
    @Path("/noone")
    public String noone() {
        return "How did you get here?";
    }
}
