package io.quarkus.ts.qute;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;

@Path("/proxy")
public class Proxy {

    @Inject
    @RestClient
    Client client;

    @GET
    @Path("/book")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> renderBook(@QueryParam("title") String title, @QueryParam("author") String author) {
        Book book = new Book(title, author, "Death", "Taxes");
        return client.book(book);
    }

    @GET
    @Path("/json")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> renderBookThroughJson(@QueryParam("title") String title, @QueryParam("author") String author) {
        Book book = new Book(title, author, "Death", "Taxes");
        return client.jsonBook(book);
    }
}
