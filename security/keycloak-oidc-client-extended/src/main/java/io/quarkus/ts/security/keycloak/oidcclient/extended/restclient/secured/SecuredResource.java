package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.secured;

import java.util.Set;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/secured")
@DenyAll
@RequestScoped
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
    @Path("/getClaimsFromBeans")
    @RolesAllowed("**")
    public String getClaimsFromBeans(@Context SecurityContext security) {
        return "Hello, " + jwt.getName() + ", your token was issued by " + issuer + " and you are in groups " + groups;
    }

    @GET
    @Path("/getClaimsFromToken")
    @RolesAllowed("**")
    public String getClaimsFromToken(@Context SecurityContext security) {
        return "Hello, " + jwt.getName() + ", your token was issued by " + jwt.getClaim(Claims.iss) + " and you are in groups "
                + jwt.getClaim(Claims.groups);
    }

    @GET
    @Path("/admin")
    @RolesAllowed("admin")
    public String admin() {
        return "Restricted area! Admin access granted!";
    }

    @GET
    @Path("/echoToken")
    @RolesAllowed("**")
    public String echoToken(@HeaderParam("Authorization") String authorization) {
        return authorization.split(" ")[1];
    }
}
