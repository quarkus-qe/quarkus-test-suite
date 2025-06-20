package io.quarkus.ts.security.keycloak;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.oidc.OidcProviderClient;

@Path("/oidc-provider-client")
public class OidcProviderClientResource {

    @Inject
    OidcProviderClient oidcProviderClient;

    @Inject
    AccessTokenCredential accessToken;

    @POST
    @Path("/token/revoke")
    public String revokeAccessTokens(String refreshToken) {
        oidcProviderClient.revokeAccessToken(accessToken.getToken()).await().indefinitely();
        oidcProviderClient.revokeRefreshToken(refreshToken).await().indefinitely();
        return "Logout, tokens revoked";
    }

    @POST
    @Path("/username/from-token")
    public Response getUsernameFromToken(String accessToken) {
        return oidcProviderClient.getUserInfo(accessToken)
                .onItem().transform(userInfo -> Response.ok(userInfo.getPreferredUserName()).build())
                .onFailure().recoverWithItem(throwable -> Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Failed to resolve user info: " + throwable.getMessage())
                        .build())
                .await().indefinitely();
    }

    @POST
    @Path("/token/is-active")
    public String introspectToken(String passedAccessToken) {
        return oidcProviderClient.introspectAccessToken(passedAccessToken).await().indefinitely().isActive()
                ? "active"
                : "inactive";
    }
}
