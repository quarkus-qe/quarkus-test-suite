package io.quarkus.ts.http.restclient.reactive;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/root/{rootParam}")
@RegisterClientHeaders
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public interface SubResourcesClient {

    @Path("/{param1}")
    SubClient sub(@PathParam("rootParam") String rootParam, @PathParam("param1") String param1);

    @Consumes("text/plain")
    @Produces("text/plain")
    interface SubClient {
        @Path("/sub/{param2}")
        SubSubClient sub(@PathParam("param2") String param2);
    }

    @Consumes("text/plain")
    @Produces("text/plain")
    interface SubSubClient {

        @GET
        @Path("/{param3}")
        String get(@PathParam("param3") String param3);
    }
}
