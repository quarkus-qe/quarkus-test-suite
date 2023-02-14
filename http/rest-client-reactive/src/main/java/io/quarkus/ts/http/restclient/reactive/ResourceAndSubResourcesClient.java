package io.quarkus.ts.http.restclient.reactive;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
