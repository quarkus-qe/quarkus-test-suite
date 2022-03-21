package io.quarkus.ts.stork;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

@RouteBase(path = "/ping", produces = MediaType.TEXT_PLAIN)
public class PingResource {

    public static final String PING_PREFIX = "ping-";
    public static final String HEADER_ID = "x-id";

    @RestClient
    MyBackendPongProxy pongService;

    @RestClient
    MyBackendPungProxy pungService;

    @Route(methods = Route.HttpMethod.GET, path = "/pung")
    public Uni<String> pung() {
        return pungService.get()
                .onFailure().transform(error -> new WebApplicationException(error.getMessage()))
                .map(resp -> PING_PREFIX + resp);
    }

    @Route(methods = Route.HttpMethod.GET, path = "/pong")
    public void pong(RoutingContext context) {
        pongService.get().onFailure().transform(error -> new WebApplicationException(error.getMessage())).subscribe()
                .with(resp -> context.response()
                        .putHeader(HEADER_ID, resp.getHeaderString(HEADER_ID))
                        .end(PING_PREFIX + resp.getEntity()));
    }
}
