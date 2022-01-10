package io.quarkus.ts.http.restclient.reactive.json;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
