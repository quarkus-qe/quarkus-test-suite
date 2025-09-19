package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh.clients.TokenRefreshDisabledRestClient;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh.clients.TokenRefreshEnabledRestClient;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh.clients.TokenRefreshFilteredRestClient;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh.clients.TokenRefreshNamedFilteredRestClient;

@Path("/token-refresh-public/")
public class TokenRefreshResource {
    @Inject
    @RestClient
    TokenRefreshFilteredRestClient tokenRefreshFilterClient;

    @Inject
    @RestClient
    TokenRefreshNamedFilteredRestClient tokenRefreshNamedFilterClient;

    @Inject
    @RestClient
    TokenRefreshEnabledRestClient tokenRefreshEnabledRestClient;

    @Inject
    @RestClient
    TokenRefreshDisabledRestClient tokenRefreshDisabledRestClient;

    @GET
    @Path("/filter")
    public String filterRevokeAndRespond() {
        return tokenRefreshFilterClient.revokeAccessTokenAndRespond("false");
    }

    @GET
    @Path("/namedFilter")
    public String namedFilterRevokeAndRespond() {
        return tokenRefreshNamedFilterClient.revokeAccessTokenAndRespond("true");
    }

    @GET
    @Path("/refreshDisabled")
    public String refreshDisabledRevokeAndRespond() {
        return tokenRefreshDisabledRestClient.revokeAccessTokenAndRespond("false");
    }

    @GET
    @Path("/refreshEnabled")
    public String refreshEnabledRevokeAndRespond() {
        return tokenRefreshEnabledRestClient.revokeAccessTokenAndRespond("false");
    }
}
