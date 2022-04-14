package io.quarkus.ts.http.restclient.reactive;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
}
