package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.principal;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.principal.clients.TokenPropagationFilteredClient;

@Path("/token-propagation-filter")
public class FilteredTokenResource {

    @Inject
    @RestClient
    TokenPropagationFilteredClient tokenPropagationFilterClient;

    @GET
    public String getUserName() {
        return tokenPropagationFilterClient.getUserName();
    }
}
