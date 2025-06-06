package io.quarkus.ts.security.keycloak;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.oidc.OidcProviderClient;
import io.quarkus.oidc.OidcSession;
import io.quarkus.oidc.UserInfo;

@Path("/oidc-provider-client")
public class OidcProviderClientResource {

    @Inject
    OidcSession oidcSession;

    @Inject
    OidcProviderClient oidcProviderClient;

    @Inject
    AccessTokenCredential accessToken;

    @Inject
    UserInfo userInfo;

    @POST
    @Path("/token/revoke")
    public String revokeAccessTokens(String refreshToken) {
        return oidcSession.logout()
                .chain(() -> oidcProviderClient.revokeAccessToken(accessToken.getToken()))
                .chain(() -> oidcProviderClient.revokeRefreshToken(refreshToken))
                .map((result) -> "Logout, tokens revoked")
                .await().indefinitely();
    }

    @GET
    @Path("/username")
    public String getUsername() {
        return userInfo.getPreferredUserName();
    }
}
