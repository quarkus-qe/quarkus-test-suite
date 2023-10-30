package io.quarkus.ts.http.restclient.reactive;

import static java.time.temporal.ChronoUnit.SECONDS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/malformed")
@RegisterClientHeaders
@Timeout(value = 5, unit = SECONDS)
public interface MalformedClient {

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    String get();
}
