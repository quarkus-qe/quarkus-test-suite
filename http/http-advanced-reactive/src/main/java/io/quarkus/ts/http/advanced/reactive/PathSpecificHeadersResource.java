package io.quarkus.ts.http.advanced.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;

@Path("/filter")
public class PathSpecificHeadersResource {

    @GET
    @Path("/any")
    public Uni<String> headers() {
        return Uni.createFrom().item("ok");
    }

    @GET
    @Path("/another")
    public Uni<String> another() {
        return Uni.createFrom().item("ok");
    }

    @GET
    @Path("/override")
    public Uni<Response> headersOverride() {
        final Response response = Response.ok("ok").header("Cache-Control", "max-age=0").build();
        return Uni.createFrom().item(response);
    }

    @GET
    @Path("/no-cache")
    public Uni<String> noCache() {
        return Uni.createFrom().item("ok");
    }

    @GET
    @Path("/order")
    public Uni<String> order() {
        return Uni.createFrom().item("ok");
    }

}
