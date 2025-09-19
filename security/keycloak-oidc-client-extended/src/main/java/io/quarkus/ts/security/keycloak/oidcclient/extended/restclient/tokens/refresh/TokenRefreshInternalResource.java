package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.refresh;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import io.quarkus.oidc.client.NamedOidcClient;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.security.Authenticated;

@Path(TokenRefreshInternalResource.INTERNAL_URL)
public class TokenRefreshInternalResource {
    private static final String TOKEN_REFRESH_RESPONSE = "token refresh secret response";
    public static final String INTERNAL_URL = "/token-refresh/internal";
    public static final String NAMED_OIDC_CLIENT_NAME = "test-user";

    @Inject
    OidcClient defaultOidcClient;

    @Inject
    @NamedOidcClient(NAMED_OIDC_CLIENT_NAME)
    OidcClient namedOidcClient;

    @Authenticated
    @POST
    public String revokeAccessTokenAndRespond(@HeaderParam(AUTHORIZATION) String authorizationHeader, String named) {
        String accessToken = authorizationHeader.substring("Bearer ".length());
        OidcClient client = Boolean.parseBoolean(named) ? namedOidcClient : defaultOidcClient;
        boolean tokenRevoked = client.revokeAccessToken(accessToken).await().indefinitely();
        if (tokenRevoked) {
            return TOKEN_REFRESH_RESPONSE;
        }
        // do not expect this to happen
        throw new IllegalStateException("Token is not revoked");
    }
}
