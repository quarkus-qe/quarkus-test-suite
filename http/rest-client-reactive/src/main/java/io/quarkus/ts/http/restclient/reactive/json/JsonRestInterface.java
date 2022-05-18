package io.quarkus.ts.http.restclient.reactive.json;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
