package io.quarkus.ts.micrometer.prometheus;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/test")
public class PathPatternResource {
    @GET
    @Path("/redirect")
    public Response redirect() {
        return Response.status(Response.Status.FOUND) //302
                .location(URI.create("/new-location"))
                .build();
    }

    @GET
    @Path("/not-found")
    public Response notFound() {
        return Response.status(Response.Status.NOT_FOUND).build(); //404
    }

    @GET
    @Path("/not-found/{uri}")
    public Response notFoundUri(@PathParam("uri") String uri) {
        return Response.status(Response.Status.NOT_FOUND).build(); //404
    }

    @GET
    @Path("/moved/{id}")
    public Response moved(@PathParam("id") String id) {
        return Response.status(Response.Status.MOVED_PERMANENTLY).build(); // 301
    }

    @GET
    @Path("")
    public Response emptyPath() {
        return Response.status(Response.Status.NO_CONTENT).build(); //204
    }

}
