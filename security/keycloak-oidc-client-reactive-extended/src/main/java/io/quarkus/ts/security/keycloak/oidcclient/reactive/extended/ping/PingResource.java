package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.model.Score;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.clients.PongClient;

@OpenAPIDefinition(info = @Info(title = "PingPong API", version = "1.0.1", contact = @Contact(name = "PingPong API Support", email = "techsupport@example.com"), license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
@Tag(name = "Ping", description = "Ping API")
@Path("/rest-ping")
public class PingResource {

    @Inject
    @RestClient
    PongClient pongClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPing() {
        System.out.println("getPing");
        return "ping " + pongClient.getPong();
    }

    @GET
    @Path("/name/{name}")
    public String getPongWithPathName(@PathParam("name") String name) {
        return "ping " + pongClient.getPongWithPathName(name);
    }

    @POST
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String createPongWithBody(Score score) {
        return "ping -> " + pongClient.createPongWithBody(score);
    }

    @PUT
    @Path("/withBody")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String updatePongWithBody(Score score) {
        return "ping -> " + pongClient.updatePongWithBody(score);
    }

    @DELETE
    @Path("/{id}")
    public String deleteById(@PathParam("id") String id) {
        return "ping -> " + pongClient.deletePongById(id);
    }
}
