package io.quarkus.ts.http.restclient.reactive;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.json.Book;
import io.smallrye.mutiny.Uni;

@Path("/client/book")
public class ReactiveClientBookResource {

    @Inject
    @RestClient
    ReactiveRestInterface restInterface;

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Book> getAsJson() {
        return restInterface.getAsJson();
    }
}
