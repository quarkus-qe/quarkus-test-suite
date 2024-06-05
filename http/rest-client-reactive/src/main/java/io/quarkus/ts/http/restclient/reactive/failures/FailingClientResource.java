package io.quarkus.ts.http.restclient.reactive.failures;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;

@Path("/client/failing")
public class FailingClientResource {
    @RestClient
    FailureClient client;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/straight")
    public String getStraightResult() {
        return client.getVisitor();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/reactive")
    public Uni<String> getReactiveResult() {
        return client.getVisitorReactively()
                .onFailure().retry().atMost(5);
    }
}
