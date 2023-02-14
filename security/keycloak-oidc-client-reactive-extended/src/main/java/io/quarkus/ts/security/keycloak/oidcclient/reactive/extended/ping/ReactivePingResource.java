package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.model.Score;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.clients.ReactivePongClient;
import io.smallrye.mutiny.Uni;

@Path("/reactive-ping")
public class ReactivePingResource {

    @Inject
    @RestClient
    ReactivePongClient pongClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getPing() {
        return pongClient.getPong().onItem().transform(response -> "ping " + response);
    }

    @GET
    @Path("/name/{name}")
    public Uni<String> getPongWithPathName(@PathParam("name") String name) {
        return pongClient.getPongWithPathName(name).onItem().transform(response -> "ping " + response);
    }

    @POST
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> createPongWithBody(Score score) {
        return pongClient.createPongWithBody(score).onItem().transform(response -> "ping -> " + response);
    }

    @PUT
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> updatePongWithBody(Score score) {
        return pongClient.updatePongWithBody(score).onItem().transform(response -> "ping -> " + response);
    }

    @DELETE
    @Path("/{id}")
    public Uni<String> deleteById(@PathParam("id") String id) {
        return pongClient.deletePongById(id).onItem().transform(response -> "ping -> " + response);
    }
}
