package io.quarkus.ts.security.keycloak.oidcclient.reactive;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
