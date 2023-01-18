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
@RegisterClientHeaders
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public interface ResourceAndSubResourcesClient {
    @Path("clients")
    ClientsResource clients();

    interface ClientsResource {
        @Path("{id}")
        ClientResource get(@PathParam("id") String id);
    }

    interface ClientResource {
        @Path("/resource-server")
        SubResource sub();
    }

    interface LeafResource {
        @GET
        String retrieve();
    }

    interface SubResource {
        @Path("{id}")
        LeafResource findById(@PathParam("id") String id);
    }
}
