package io.quarkus.ts.http.httpproblem.sources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkiverse.httpproblem.client.ThrowingHttpProblemClientExceptionMapper;

@RegisterRestClient(configKey = "problem-client")
@RegisterProvider(ThrowingHttpProblemClientExceptionMapper.class)
@Path("/problem")
public interface ProblemClient {

    @GET
    @Path("/not-found")
    @Produces(MediaType.APPLICATION_JSON)
    String triggerNotFound();
}
