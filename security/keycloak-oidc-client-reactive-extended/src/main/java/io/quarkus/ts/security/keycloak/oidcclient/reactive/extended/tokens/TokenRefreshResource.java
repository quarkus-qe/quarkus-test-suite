package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.quarkus.oidc.client.OidcClientException;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.client.Tokens;
import io.smallrye.mutiny.Uni;

@Path("/token")
public class TokenRefreshResource {

    private static final Logger LOG = Logger.getLogger(TokenRefreshResource.class);

    @Inject
    OidcClients oidcClients;

    @GET
    @Path("/refresh")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> forceTokenRefresh(@QueryParam("refreshToken") String refreshToken) {
        return oidcClients.getClient("test-user")
                .refreshTokens(refreshToken)
                .onItem().transform(tokens -> {
                    if (!tokensAreInitialized(tokens)) {
                        return Response.status(500)
                                .entity("Failed to refresh tokens")
                                .build();
                    } else {
                        LOG.infof("Refreshed AccessToken=%s RefreshToken=%s",
                                tokens.getAccessToken(), tokens.getRefreshToken());
                        return Response.ok(tokens.getAccessToken() + " " + tokens.getRefreshToken())
                                .build();
                    }
                })
                .onFailure(OidcClientException.class)
                .recoverWithItem(t -> Response.status(500).entity("Invalid token: " + t.getMessage()).build());
    }

    private Uni<String> createTokensString(Tokens tokens) {
        if (tokensAreInitialized(tokens)) {
            LOG.infof("Refreshed AccessToken=%s RefreshToken=%s", tokens.getAccessToken(), tokens.getRefreshToken());
            return Uni.createFrom()
                    .item(tokens.getAccessToken() + " " + tokens.getRefreshToken());
        } else {
            return Uni.createFrom().failure(new InternalServerErrorException("Failed to refresh tokens"));
        }
    }

    private boolean tokensAreInitialized(Tokens tokens) {
        return tokens.getAccessToken() != null && tokens.getAccessTokenExpiresAt() != null && tokens.getRefreshToken() != null;
    }
}
