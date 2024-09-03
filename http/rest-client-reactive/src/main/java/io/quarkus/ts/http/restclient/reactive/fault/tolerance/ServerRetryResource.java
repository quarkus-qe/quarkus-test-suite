package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

import java.util.concurrent.ExecutionException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Server resource that calling `RetryClient` to trigger
 * an asynchronous client request. It is used to simulate and test
 * retry logic when the client fails to complete a request.
 */
@ApplicationScoped
@Path("/server")
public class ServerRetryResource {

    private final RetryClient retryClient;

    @Inject
    public ServerRetryResource(@RestClient RetryClient retryClient) {
        this.retryClient = retryClient;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("async")
    public Response getItemsAsync() throws ExecutionException, InterruptedException {
        return retryClient.getItemsAsync()
                .toCompletableFuture()
                .get();
    }
}
