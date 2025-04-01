package io.quarkus.ts.security.keycloak.oidcclient.standalone;

import static java.util.Base64.getUrlDecoder;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.client.Tokens;

@Path("/oidc-client-tokens")
public class TokenResource {

    @Inject
    OidcClient defaultOidcClient;

    @Inject
    OidcClients oidcClients;

    @Inject
    Tokens tokens;

    @GET
    public String getInjectedToken() {
        return decodeToken(tokens.getAccessToken());
    }

    @GET
    @Path("default-user")
    public String getTokenOfDefaultUser() {
        String token = defaultOidcClient.getTokens().await().indefinitely().getAccessToken();
        return decodeToken(token);
    }

    @GET
    @Path("custom-user")
    public String getTokenOfCustomUser() {
        String token = oidcClients.getClient("custom-user").getTokens().await().indefinitely().getAccessToken();
        return decodeToken(token);
    }

    private String decodeToken(String token) {
        String[] headerAndPayload = token.split("\\.");
        String header = new String(getUrlDecoder().decode(headerAndPayload[0]));
        String payload = new String(getUrlDecoder().decode(headerAndPayload[1]));
        return header + payload;
    }
}
