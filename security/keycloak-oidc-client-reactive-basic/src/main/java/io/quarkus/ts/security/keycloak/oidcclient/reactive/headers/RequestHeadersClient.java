package io.quarkus.ts.security.keycloak.oidcclient.reactive.headers;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import io.smallrye.mutiny.Uni;

@RegisterRestClient
@RegisterProvider(OidcClientRequestReactiveFilter.class)
@Path("/request-headers")
public interface RequestHeadersClient {
    @GET
    @Path("/authorization")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<String>> getAuthorizationHeaders();
}
