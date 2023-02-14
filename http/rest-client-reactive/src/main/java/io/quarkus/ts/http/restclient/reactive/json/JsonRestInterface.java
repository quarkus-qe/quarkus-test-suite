package io.quarkus.ts.http.restclient.reactive.json;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient
@Path("/book/{id}")
@RegisterClientHeaders
public interface JsonRestInterface {

    @GET
    @Path("/json")
    Uni<Book> getWithBeanParam(@BeanParam IdBeanParam beanParam);

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Book> getAsJson(@PathParam("id") String id);
}
