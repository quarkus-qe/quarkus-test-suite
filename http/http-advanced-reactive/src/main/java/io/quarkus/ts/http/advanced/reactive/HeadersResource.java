package io.quarkus.ts.http.advanced.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;

@Path("/headers")
public class HeadersResource {

    @GET
    @Path("/any")
    public Uni<String> headers() {
        return Uni.createFrom().item("ok");
    }

    @GET
    @Path("/pragma")
    public Uni<String> pragmaHeaderMustBeSet() {
        return Uni.createFrom().item("ok");
    }

    @GET
    @Path("/override")
    public Uni<Response> headersOverride() {
        final Response response = Response.ok("ok").header("foo", "abc").build();
        return Uni.createFrom().item(response);
    }

}
