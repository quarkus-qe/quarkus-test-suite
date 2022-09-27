package io.quarkus.ts.http.advanced;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/client")
public class ClientResource {

    @Inject
    @RestClient
    HealthClientService client;

    @GET
    public Response get() {
        return client.health();
    }
}
