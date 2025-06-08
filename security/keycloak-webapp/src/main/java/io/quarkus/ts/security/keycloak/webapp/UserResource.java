package io.quarkus.ts.security.keycloak.webapp;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.oidc.OidcProviderClient;
import io.quarkus.oidc.OidcSession;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/user")
@RolesAllowed("test-user-role")
public class UserResource {
    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    @Inject
    OidcProviderClient oidcProviderClient;

    @Inject
    OidcSession oidcSession;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "Hello, user " + identity.getPrincipal().getName();
    }

    @GET
    @Path("/issuer")
    @Produces(MediaType.TEXT_PLAIN)
    public String issuer() {
        return "user token issued by " + jwt.getIssuer();
    }

    @GET
    @Path("/access-token")
    @Produces(MediaType.TEXT_PLAIN)
    public String token() {
        return jwt.getRawToken();
    }

    @GET
    @Path("/inspect")
    @Produces(MediaType.TEXT_PLAIN)
    public String isActive(@QueryParam("token") String accessToken) {
        return oidcProviderClient.introspectAccessToken(accessToken).await().indefinitely().isActive()
                ? "active"
                : "inactive";
    }

    @GET
    @Path("/info")
    @Produces(MediaType.TEXT_PLAIN)
    public String userInfo(@QueryParam("token") String accessToken) {
        return oidcProviderClient.getUserInfo(accessToken)
                .await().indefinitely().getPreferredUserName();
    }

    @GET
    @Path("/logout")
    public String logout() throws InterruptedException {
        oidcSession.logout().await().indefinitely();
        return "You are logged out.";
    }
}
