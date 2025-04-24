package io.quarkus.ts.http.restclient.reactive.resources;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.BookClient;
import io.quarkus.ts.http.restclient.reactive.json.Author;
import io.quarkus.ts.http.restclient.reactive.json.Book;
import io.quarkus.ts.http.restclient.reactive.json.BookIdWrapper;
import io.quarkus.ts.http.restclient.reactive.json.IdBeanParam;
import io.quarkus.ts.http.restclient.reactive.json.JsonRestInterface;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/client/book")
public class ReactiveClientBookResource {

    @Inject
    @RestClient
    JsonRestInterface restInterface;

    @Inject
    @RestClient
    BookClient bookInterface;

    @GET
    @Path("/{id}/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Book> getAsJson(@PathParam("id") String id) {
        return restInterface.getAsJson(id);
    }

    @GET
    @Path("/{id}/jsonByBeanParam")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Book> getAsJsonByBeanParam(@PathParam("id") String id) {
        return restInterface.getWithBeanParam(new IdBeanParam(id));
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Book> getResource(@QueryParam("title") String title, @QueryParam("author") String author) {
        return bookInterface.getBook(title, author);
    }

    @GET
    @Path("/author")
    public Uni<String> getSubResource(@QueryParam("author") String author) {
        return bookInterface.getAuthor().getName(author);
    }

    @GET
    @Path("/profession")
    public Uni<String> getSubSubResource() {
        return bookInterface.getAuthor().getProfession().getTitle();
    }

    @GET
    @Path("/currency")
    public String getLastResource() {
        return bookInterface.getAuthor().getProfession().getWage().getCurrency().getName();
    }

    @GET
    @Path("/author/info")
    public Uni<Author> getAuthorInfo(@QueryParam("author") String author) {
        return bookInterface.getAuthor().getAuthor(author);
    }

    @POST
    @Path("/author/books")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Multi<String> getAuthorInfo(Author author) {
        return bookInterface.getAuthor().getBooksByAuthor(author);
    }

    @GET
    @Path("/quick-search/decoded")
    public Multi<String> getDecodedPath(@QueryParam("searchTerm") String searchTerm) {
        return bookInterface.getByDecodedSearchTerm(searchTerm);
    }

    @GET
    @Path("/quick-search/encoded")
    public Multi<String> getEncodedPath(@QueryParam("searchTerm") String searchTerm) {
        return bookInterface.getByEncodedSearchTerm(searchTerm);
    }

    @GET
    @Path("/rest-query")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Book>> getRestQuery() {
        var primitiveParamsMap = Map.of("firstPlainId", 1, "secondPlainId", 2);
        var classParamsMap = Map.of("firstObjectId", new BookIdWrapper(3),
                "secondObjectId", new BookIdWrapper(4));
        var multivaluedMap = new MultivaluedHashMap<String, Integer>();
        multivaluedMap.put("additionalIds", List.of(5, 6));
        return bookInterface.getByRestQueryMap(primitiveParamsMap, classParamsMap, multivaluedMap);
    }

    @GET
    @Path("/suffix/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<String> getSuffixed(@QueryParam("content") String text,
            @PathParam("type") String type) {
        return switch (type) {
            case "complete" -> bookInterface.getCompleteType(text);
            case "subtype" -> bookInterface.getWithSubType(text);
            case "suffix" -> bookInterface.getWithSuffix(text);
            case "other" -> bookInterface.getUnrelated(text);
            case "priority" -> bookInterface.getPriorities(text);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    @POST
    @Path("/search")
    public Set<Book> postFilter() {
        return bookInterface.postFilter(Map.of("author", "Haruki Murakami"));
    }

    @GET
    @Path("/sequel")
    public Uni<String> getSequel(@QueryParam("title") String title, @QueryParam("author") String author) {
        return bookInterface.getSequel(new Book(title, author));
    }
}
