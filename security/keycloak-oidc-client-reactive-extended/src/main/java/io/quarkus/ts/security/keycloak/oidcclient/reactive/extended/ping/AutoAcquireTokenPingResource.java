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
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.clients.AutoAcquireTokenPongClient;

@Path("/auto-acquire-token-ping")
public class AutoAcquireTokenPingResource {

    @Inject
    @RestClient
    AutoAcquireTokenPongClient pongClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPing() {
        return "ping " + pongClient.getPong();
    }

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPingWithPathName(@PathParam("name") String name) {
        return "ping " + pongClient.getPongWithPathName(name);
    }

    @POST
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    public String createPongWithBody(Score score) {
        return "ping -> " + pongClient.createPongWithBody(score);
    }

    @PUT
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    public String updatePongWithBody(Score score) {
        return "ping -> " + pongClient.updatePongWithBody(score);
    }

    @DELETE
    @Path("/{id}")
    public String deleteById(@PathParam("id") String id) {
        return "ping -> " + pongClient.deletePongById(id);
    }
}
