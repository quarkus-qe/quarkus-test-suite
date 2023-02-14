package io.quarkus.ts.http.restclient.reactive.json;

import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;

@Produces(MediaType.APPLICATION_JSON)
@Path("/book/{id}/json")
public class BookAsJsonResource {

    @GET
    public Uni<String> get(@PathParam("id") String id) {
        Map<String, String> body = Map.of("title", "Title in Json: " + id);
        return Uni.createFrom().item(Json.encode(body));
    }
}
