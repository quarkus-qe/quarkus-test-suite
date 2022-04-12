package io.quarkus.ts.stork.custom;

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

    @RestClient
    MyBackendPongProxy pongService;

    @Route(methods = Route.HttpMethod.GET, path = "/pong")
    public void pong(RoutingContext context) {
        pongService.get().onFailure().transform(error -> new WebApplicationException(error.getMessage())).subscribe()
                .with(resp -> context.response().end(PING_PREFIX + resp.getEntity()));
    }
}
