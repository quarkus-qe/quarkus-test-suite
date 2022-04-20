package io.quarkus.ts.http.restclient.reactive;

import java.util.Map;

import javax.ws.rs.Consumes;
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

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Book> getBook(@QueryParam("title") String title, @QueryParam("author") String author) {
        return Uni.createFrom().item(new Book(title, author));
    }

    @GET
    @Path("/author/name")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getAuthorName(@QueryParam("author") String author) {
        return Uni.createFrom().item(author);
    }

    @GET
    @Path("/author/profession/title")
    public Uni<String> getProfession() {
        return Uni.createFrom().item("writer");
    }

    @GET
    @Path("/author/profession/wage/currency/name")
    public Uni<String> getCurrency() {
        return Uni.createFrom().item("USD");
    }
}
