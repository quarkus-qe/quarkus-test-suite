package io.quarkus.ts.http.restclient.reactive.resources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
@Path("/http-version")
public class HttpVersionBaseResource {

    @GET
    @Path("synchronous")
    public Response sayHello(@QueryParam("name") String name, RoutingContext context) {
        return Response.ok("Hello " + name + " and your using http protocol in version " + context.request().version().name())
                .build();
    }

    @GET
    @Path("asynchronous")
    public Uni<Response> sayHelloAsync(@QueryParam("name") String name, RoutingContext context) {
        return Uni.createFrom().item(Response.ok(
                "Hello " + name + " and your using http protocol in version " + context.request().version().name()).build());
    }
}
