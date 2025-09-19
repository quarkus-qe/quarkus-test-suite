package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh.clients;

import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh.TokenRefreshInternalResource.INTERNAL_URL;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@RegisterProvider(value = RefreshDisabledRequestFilter.class)
@Path(INTERNAL_URL)
public interface TokenRefreshDisabledRestClient {

    @POST
    String revokeAccessTokenAndRespond(String named);
}
