package io.quarkus.ts.http.httpproblem.sources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkiverse.httpproblem.HttpProblem;

@Path("/problem/client-test")
public class ProblemClientResource {

    @RestClient
    ProblemClient problemClient;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response testRestClientDeserialization() {
        try {
            problemClient.triggerNotFound();
            return Response.ok("{\"error\":\"no exception thrown\"}").build();
        } catch (HttpProblem e) {
            return Response.ok()
                    .entity(String.format("{\"exceptionClass\":\"%s\",\"status\":%d,\"title\":\"%s\"}",
                            e.getClass().getName(), e.getStatusCode(), e.getTitle()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            return Response.ok()
                    .entity(String.format("{\"exceptionClass\":\"%s\",\"message\":\"%s\"}",
                            e.getClass().getName(), e.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
