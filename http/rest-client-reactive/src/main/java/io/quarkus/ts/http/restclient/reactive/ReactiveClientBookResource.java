package io.quarkus.ts.http.restclient.reactive;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.json.Book;
import io.quarkus.ts.http.restclient.reactive.json.IdBeanParam;
import io.quarkus.ts.http.restclient.reactive.json.JsonRestInterface;
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
        return bookInterface.getAuthor().getProfession().getName();
    }

    @GET
    @Path("/currency")
    public String getLastResource() {
        return bookInterface.getAuthor().getProfession().getWage().getCurrency().getName();
    }
}
