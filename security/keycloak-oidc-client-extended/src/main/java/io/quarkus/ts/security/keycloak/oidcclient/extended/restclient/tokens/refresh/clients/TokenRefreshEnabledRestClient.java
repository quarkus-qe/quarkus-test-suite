package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.refresh.clients;

import static io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.refresh.TokenRefreshInternalResource.INTERNAL_URL;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@RegisterProvider(value = RefreshEnabledRequestFilter.class)
@Path(INTERNAL_URL)
public interface TokenRefreshEnabledRestClient {

    @POST
    String revokeAccessTokenAndRespond(String named);
}
