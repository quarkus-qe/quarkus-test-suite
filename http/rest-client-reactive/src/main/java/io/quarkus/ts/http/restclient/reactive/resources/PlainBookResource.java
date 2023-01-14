package io.quarkus.ts.http.restclient.reactive.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.ts.http.restclient.reactive.json.Book;
import io.quarkus.ts.http.restclient.reactive.json.BookIdWrapper;
import io.quarkus.ts.http.restclient.reactive.json.BookRepository;
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
    @Path("/rest-query")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Book>> getRestQuery(@RestQuery Integer firstPlainId, @RestQuery Integer secondPlainId,
            @RestQuery BookIdWrapper firstObjectId, @RestQuery BookIdWrapper secondObjectId,
            @RestQuery List<Integer> additionalIds) {
        var books = new ArrayList<Book>();
        books.add(BookRepository.getById(firstPlainId));
        books.add(BookRepository.getById(secondPlainId));
        books.add(BookRepository.getById(firstObjectId.getId()));
        books.add(BookRepository.getById(secondObjectId.getId()));
        additionalIds.stream().map(BookRepository::getById).forEach(books::add);
        return Uni.createFrom().item(books);
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

    @GET
    @Path("/suffix")
    @Produces("application/text+json")
    public Uni<String> getWithSuffixedType(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_text+json");
    }

    @GET
    @Path("/suffix")
    @Produces("application/text")
    public Uni<String> getWithSubType(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_text");
    }

    @GET
    @Path("/suffix")
    @Produces("application/json")
    public Uni<String> getWithSuffix(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_json");
    }

    @GET
    @Path("/suffix")
    @Produces("application/quarkus")
    public Uni<String> getWithUnrelatedType(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_other");
    }

    @GET
    @Path("/suffix_priority")
    @Produces("application/text")
    public Uni<String> getPriorityWithSubType(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_text");
    }

    @GET
    @Path("/suffix_priority")
    @Produces("application/json")
    public Uni<String> getPriorityWithSuffix(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_json");
    }

}
