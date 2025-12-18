package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.clients.TokenPropagationMethodPongClient;
import io.smallrye.mutiny.Uni;

@Path("/token-propagation-method-ping")
public class TokenPropagationMethodPingResource {

    @Inject
    @RestClient
    TokenPropagationMethodPongClient pongClient;

    @GET
    @Path("/standard")
    public Uni<String> getPingStandard() {
        return pongClient.getPongStandard().map(s -> "ping " + s);
    }

    @GET
    @Path("/exchange")
    public Uni<String> getPingExchange() {
        return pongClient.getPongWithExchange("exchange").map(s -> "ping " + s);
    }

    @GET
    @Path("/none")
    public Uni<Response> getPingNoToken() {
        return pongClient.getPongNoToken("no-token")
                .map(r -> Response.ok("ping " + r).build())
                .onFailure(ClientWebApplicationException.class)
                .recoverWithItem(e -> Response.status(Response.Status.UNAUTHORIZED).build());
    }

    @GET
    @Path("/public-success")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getPingPublicSuccess() {
        return pongClient.getPongPublic().map(r -> "ping " + r);
    }
}
