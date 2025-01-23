package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.oidc.client.OidcClient;
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
    public Uni<String> forceTokenRefresh(@QueryParam("refreshToken") String refreshToken) {
        OidcClient client = oidcClients.getClient("test-user");
        return client.refreshTokens(refreshToken).flatMap(this::createTokensString);
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
