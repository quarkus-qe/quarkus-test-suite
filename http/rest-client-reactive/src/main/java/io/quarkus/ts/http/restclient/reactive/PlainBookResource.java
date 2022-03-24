package io.quarkus.ts.http.restclient.reactive;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.quarkus.ts.http.restclient.reactive.json.Book;
import io.smallrye.mutiny.Uni;

@Path("/books")
public class PlainBookResource {

    @GET
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Book> getByQueryMap(@QueryParam("param") Map<String, String> params) {
        return Uni.createFrom()
                .item(new Book(params.get("id"),
                        params.get("author")));
    }
}
