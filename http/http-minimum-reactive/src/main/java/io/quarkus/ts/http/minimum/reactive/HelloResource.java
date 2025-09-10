package io.quarkus.ts.http.minimum.reactive;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;

@Path("/hello")
public class HelloResource {
    private static final String TEMPLATE = "Hello, %s!";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Hello> get(@QueryParam("name") @DefaultValue("World") String name) {
        return Uni.createFrom().item(new Hello(String.format(TEMPLATE, name)));
    }

    @GET
    @Path("/json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Hello> getJson() {
        return Uni.createFrom().item(new Hello("hello"));
    }

    @GET
    @Path("/foo/{something-with-dash:[A-Z0-9]{4}}")
    public Response doSomething(@PathParam("something-with-dash") String param) {
        return Response.noContent().build();
    }

    @GET
    @Path("/no-content-length")
    public Response hello() {
        String content = IntStream.range(1, 100)
                .mapToObj(i -> "Hello no." + i)
                .collect(Collectors.joining("\n"));

        // todo omit wrapping if https://github.com/quarkusio/quarkus/issues/49942 is ever fixed
        var stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return Response
                .ok(stream)
                .build();
    }

    public record Data(String data) {
    }

    @GET
    @Path("short-record-response")
    @Produces(MediaType.APPLICATION_JSON)
    public Response shortRecordInResponseClass() {
        return Response.ok().entity(new Data("ok")).build();
    }

    @GET
    @Path("short-record")
    @Produces(MediaType.APPLICATION_JSON)
    public Data shortRecordReturnedDirectly() {
        return new Data("ok");
    }
}
