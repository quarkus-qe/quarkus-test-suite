package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.oidc.token.propagation.common.AccessToken;
import io.smallrye.mutiny.Uni;

@RegisterRestClient
@Path("/rest-pong")
public interface TokenPropagationMethodPongClient {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @AccessToken
    Uni<String> getPongStandard();

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @AccessToken(exchangeTokenClient = "exchange-token")
    Uni<String> getPongWithExchange(@PathParam("name") String name);

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> getPongNoToken(@PathParam("name") String name);

    @GET
    @Path("/public")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> getPongPublic();
}
