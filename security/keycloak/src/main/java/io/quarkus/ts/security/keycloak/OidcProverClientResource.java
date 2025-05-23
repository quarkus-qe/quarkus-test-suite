package io.quarkus.ts.security.keycloak;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.oidc.OidcProviderClient;
import io.quarkus.oidc.OidcSession;
import io.quarkus.oidc.RefreshToken;
import io.quarkus.oidc.UserInfo;

@Path("/oidc-prover-client")
public class OidcProverClientResource {

    @Inject
    OidcSession oidcSession;

    @Inject
    OidcProviderClient oidcProviderClient;

    @Inject
    AccessTokenCredential accessToken;

    @Inject
    RefreshToken refreshToken;

    @Inject
    UserInfo userInfo;

    @GET
    @Path("/token/revoke")
    public String logout() {
        String access = accessToken != null ? accessToken.getToken() : "null";
        String refresh = refreshToken != null ? refreshToken.getToken() : "null";

        System.out.println("Access Token: " + access);
        System.out.println("Refresh Token: " + refresh);

        return oidcSession.logout()
                .chain(() -> oidcProviderClient.revokeAccessToken(accessToken.getToken()))
                .chain(() -> oidcProviderClient.revokeRefreshToken(refreshToken.getToken()))
                .map((result) -> "Logout, tokens revoked")
                .await().indefinitely();
    }

    @GET
    @Path("/username")
    public String getUsername() {
        return userInfo.getPreferredUserName();
    }

    //    @GET
    //    @Path("/introspect")
    //    @Produces(MediaType.APPLICATION_JSON)
    //    public Map<String, Object> isTokenActive() {
    //        var result = oidcProviderClient.introspectAccessToken(accessToken.getToken()).map(TokenIntrospection::isActive);
    //        return oidcProviderClient.introspectAccessToken(accessToken.getToken())
    //                .await().indefinitely();
    //    }
}
