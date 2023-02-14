package io.quarkus.ts.http.advanced;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.vertx.core.http.HttpServerRequest;

@Path("/details")
public class RequestDetailsResource {

    @Inject
    HttpServerRequest request;

    @GET
    @Path("/server/address")
    @Produces(MediaType.TEXT_PLAIN)
    public String serverAddress() {
        return request.localAddress().toString();
    }

    @GET
    @Path("/client/address")
    @Produces(MediaType.TEXT_PLAIN)
    public String clientAddress() {
        return request.remoteAddress().toString();
    }
}
