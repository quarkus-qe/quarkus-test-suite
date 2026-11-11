package io.quarkus.ts.mcp.app;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.vertx.core.json.JsonArray;

@Path("/test/protocol-observations")
public class ProtocolObservationsResource {
    @Inject
    ProtocolObserver observer;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String observations() {
        return new JsonArray(observer.observations()).encode();
    }
}
