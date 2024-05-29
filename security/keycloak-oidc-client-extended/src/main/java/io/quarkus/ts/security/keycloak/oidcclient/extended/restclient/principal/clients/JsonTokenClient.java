package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.principal.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.token.propagation.JsonWebTokenRequestFilter;

@RegisterRestClient
@RegisterClientHeaders
@Path("/principal")
@RegisterProvider(JsonWebTokenRequestFilter.class)
public interface JsonTokenClient {

    @GET
    String getUserName();
}
