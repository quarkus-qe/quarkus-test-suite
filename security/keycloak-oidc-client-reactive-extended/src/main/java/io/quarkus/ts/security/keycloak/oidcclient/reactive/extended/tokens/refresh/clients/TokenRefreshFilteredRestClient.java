package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh.clients;

import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh.TokenRefreshInternalResource.INTERNAL_URL;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.client.filter.OidcClientFilter;

@RegisterRestClient
@OidcClientFilter
@Path(INTERNAL_URL)
public interface TokenRefreshFilteredRestClient {

    @POST
    String revokeAccessTokenAndRespond(String named);
}
