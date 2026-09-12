package io.quarkus.ts.security.pqc;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/hello")
@RegisterRestClient(configKey = "hello-api")
public interface HelloClient {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String hello();
}
