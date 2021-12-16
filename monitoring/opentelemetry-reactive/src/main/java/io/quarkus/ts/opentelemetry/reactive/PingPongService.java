package io.quarkus.ts.opentelemetry.reactive;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient
public interface PingPongService {

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> getPongResponse();
}
