package io.quarkus.ts.security.pqc;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/client/hello")
public class ClientResource {

    @Inject
    @RestClient
    HelloClient helloClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String callServer() {
        return helloClient.hello();
    }
}
