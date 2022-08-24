package io.quarkus.ts.http.advanced;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
