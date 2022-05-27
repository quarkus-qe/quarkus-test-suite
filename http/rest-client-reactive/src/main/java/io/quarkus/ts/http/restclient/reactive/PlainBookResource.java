package io.quarkus.ts.http.restclient.reactive;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.quarkus.ts.http.restclient.reactive.json.Book;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/books")
public class PlainBookResource {

    public static final String SEARCH_TERM_VAL = "Ernest Hemingway";

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

    /**
     * Characters in foreign language: '%E3%82%AF%E3%82%A4%E3%83%83%E3%82%AF%E6%A4%9C%E7%B4%A2' -> 'クイック検索' -> 'quick-search'
     * Reserved characters: '%25%20%23%20%5B%20%5D%20+%20=%20&%20@%20:%20!%20*%20(%20)%20'%20$%20,%20%3F' -> "% # [ ] + = & @ :
     * ! * ( ) ' $ , ?",
     * characters '=', '+', '@', ':', '!', '*', '(', ')', '\'', ',' are not encoded as it's not necessary.
     * Unreserved characters: - _ . ~
     */
    @GET
    @Path("/%E3%82%AF%E3%82%A4%E3%83%83%E3%82%AF%E6%A4%9C%E7%B4%A2/%25%20%23%20%5B%20%5D%20+%20=%20&%20@%20:%20!%20*%20(%20)%20'%20$%20,%20%3F/-%20_%20.%20~")
    public Multi<String> getBySearchTerm(@QueryParam("searchTerm") String searchTerm) {
        if (SEARCH_TERM_VAL.equals(searchTerm)) {
            return Multi.createFrom().items("In Ou"
                    + "r Time", ", ", "The Sun Also Rises", ", ", "A Farewell to Arms", ", ",
                    "The Old Man and the Sea");
        } else {
            return Multi.createFrom().empty();
        }
    }
}
