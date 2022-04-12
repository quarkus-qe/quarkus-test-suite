package io.quarkus.ts.stork.custom;

import javax.enterprise.event.Observes;
import javax.ws.rs.core.MediaType;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.Vertx;

@RouteBase(path = "/pong", produces = MediaType.TEXT_PLAIN)
public class PongResource {

    private static final String DEFAULT_PONG_RESPONSE = "pong";

    public void init(@Observes StartupEvent ev, Vertx vertx) {
        // registered by application.properties
    }

    @Route(path = "/", methods = Route.HttpMethod.GET)
    public void pong(final RoutingContext context) {
        context.response().end(DEFAULT_PONG_RESPONSE);
    }
}
