package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.secured.method;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.client.filter.OidcClientFilter;

@RegisterRestClient
@Path("/method-secured/")
public interface MethodFilteredClient {

    @GET
    @Path("/public")
    @Produces(MediaType.TEXT_PLAIN)
    String publicMethodNoAuth();

    @GET
    @Path("/public")
    @OidcClientFilter
    @Produces(MediaType.TEXT_PLAIN)
    String publicMethodAuth();

    @GET
    @Path("/secured")
    @Produces(MediaType.TEXT_PLAIN)
    String securedMethodNoAuth();

    @GET
    @Path("/secured")
    @OidcClientFilter
    @Produces(MediaType.TEXT_PLAIN)
    String securedMethodAuth();

    @GET
    @Path("/secured")
    @OidcClientFilter("test-user")
    @Produces(MediaType.TEXT_PLAIN)
    String securedMethodNamedAuth();
}
