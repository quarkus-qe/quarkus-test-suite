package io.quarkus.ts.http.restclient.reactive;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.ts.http.restclient.reactive.json.Book;
import io.smallrye.mutiny.Uni;

@RegisterRestClient
@Path("/book")
@RegisterClientHeaders
public interface ReactiveRestInterface {

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Book> getAsJson();
}
