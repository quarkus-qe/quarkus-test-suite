package io.quarkus.ts.http.restclient.reactive.json;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Produces(MediaType.APPLICATION_JSON)
@Path("/book/json")
public class BookAsJsonResource {

    @GET
    public Uni<String> get() {
        return Uni.createFrom().item("{\"title\":\"Title in Json\"}");
    }
}
