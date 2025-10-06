package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.clients.TokenEchoClient;

@Path("/token-echo")
public class TokenEchoResource {
    @Inject
    @RestClient
    TokenEchoClient tokenEchoClient;

    private static int tokenRefreshCounts;

    public static void addTokenRefreshCount() {
        tokenRefreshCounts++;
    }

    @GET
    public String echoToken() {
        tokenRefreshCounts = 0;
        return tokenEchoClient.echoToken();
    }

    @GET
    @Path("/refresh-counts")
    public int getTokenRefreshCounts() {
        return tokenRefreshCounts;
    }
}
