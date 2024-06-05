package io.quarkus.ts.http.restclient.reactive.failures;

import java.time.temporal.ChronoUnit;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient(baseUri = "http://localhost:8080/")
@Path("/fail")
public interface FailureClient {

    @GET
    @Retry(delay = 1, delayUnit = ChronoUnit.SECONDS, maxRetries = 5)
    @Path("/cyclic")
    String getVisitor();

    @GET
    @Path("/cyclic")
    Uni<String> getVisitorReactively();
}
