package io.quarkus.ts.stork;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "greeting")
public interface IGreetingResource {
    @GET
    @Path("/greeting")
    @Produces(MediaType.TEXT_PLAIN)
    String hello();
}
