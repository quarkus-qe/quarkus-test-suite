package io.quarkus.ts.http.httpproblem.sources;

import java.net.URI;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.httpproblem.HttpProblem;

@Path("/problem")
@Produces(MediaType.APPLICATION_JSON)
public class HttpProblemResource {

    @GET
    @Path("/standard")
    public Response standard() {
        throw HttpProblem.builder()
                .withType(URI.create("http://localhost/problem/conflict"))
                .withTitle("Conflict")
                .withStatus(Response.Status.CONFLICT)
                .withDetail("Resource already exists")
                .withInstance(URI.create("/problem/standard"))
                .build();
    }

    @GET
    @Path("/instance-path/{path:.+}")
    public Response instancePath(@PathParam("path") String path) {
        throw HttpProblem.builder()
                .withStatus(Response.Status.CONFLICT)
                .withTitle("Conflict")
                .withInstance(URI.create("/" + path))
                .build();
    }

    @GET
    @Path("/custom")
    public Response custom() {
        throw HttpProblem.builder()
                .withStatus(422)
                .withTitle("Validation Failed")
                .with("balance", 30)
                .with("currency", "USD")
                .withHeader("X-Custom-Header", "custom-value")
                .build();
    }

    @GET
    @Path("/not-found")
    public Response notFound() {
        throw new NotFoundException("Resource not found");
    }

    @GET
    @Path("/forbidden")
    public Response forbidden() {
        throw new ForbiddenException("Access denied");
    }

    @GET
    @Path("/unauthorized")
    public Response unauthorized() {
        throw new NotAuthorizedException("Bearer");
    }

    @GET
    @Path("/web-app-exception/{status}")
    public Response webAppException(@PathParam("status") int status) {
        throw new WebApplicationException("Error", status);
    }

    @GET
    @Path("/unhandled")
    public Response unhandled() {
        throw new RuntimeException("internal error details should not leak");
    }

    @GET
    @Path("/post-processed")
    public Response postProcessed() {
        throw HttpProblem.builder()
                .withStatus(Response.Status.CONFLICT)
                .withTitle("Conflict")
                .build();
    }
}
