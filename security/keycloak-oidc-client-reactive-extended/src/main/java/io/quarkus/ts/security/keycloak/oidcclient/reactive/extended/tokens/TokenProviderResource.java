package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClients;

@Path("/generate-token")
public class TokenProviderResource {
    @Inject
    OidcClient oidcClient;

    @Inject
    OidcClients allOidcClients;

    @GET
    @Path("/client-credentials")
    public String getTokenUsingClientCredentialsGrant() {
        return generateToken(oidcClient);
    }

    @GET
    @Path("/test-user")
    public String getTokenUsingAdminUserPasswordGrant() {
        return generateToken(allOidcClients.getClient("test-user"));
    }

    private String generateToken(OidcClient client) {
        return client.getTokens().await().indefinitely().getAccessToken();
    }
}
