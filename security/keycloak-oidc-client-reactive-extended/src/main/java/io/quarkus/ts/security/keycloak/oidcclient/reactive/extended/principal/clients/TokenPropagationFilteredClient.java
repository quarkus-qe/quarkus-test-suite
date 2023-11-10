package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.principal.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters.CustomTokenRequestFilter;

@RegisterRestClient
@RegisterClientHeaders
@Path("/principal")
@RegisterProvider(CustomTokenRequestFilter.class)
public interface TokenPropagationFilteredClient {

    @GET
    String getUserName();
}
