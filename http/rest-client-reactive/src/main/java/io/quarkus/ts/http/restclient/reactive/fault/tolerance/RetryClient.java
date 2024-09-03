package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

import java.util.concurrent.CompletionStage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Interface defines a REST client making asynchronous calls
 * to the `/client/async` endpoint. It uses the `@Retry` annotation
 * to retry in case of a `ProcessingException`. It is called by the
 * server resource `ServerRetryResource`.
 */
@ApplicationScoped
@RegisterRestClient(configKey = "client.endpoint")
@Path("/client")
public interface RetryClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Retry(retryOn = ProcessingException.class)
    @Path("async")
    CompletionStage<Response> getItemsAsync();
}
