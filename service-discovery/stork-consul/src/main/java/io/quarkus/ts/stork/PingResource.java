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

    @RestClient
    MyBackendPongProxy pongService;

    @RestClient
    MyBackendPungProxy pungService;

    @Route(methods = Route.HttpMethod.GET, path = "/pung")
    public Uni<String> pung(RoutingContext context) {
        return formatResponse(pungService.get());
    }

    @Route(methods = Route.HttpMethod.GET, path = "/pong")
    public Uni<String> pong(RoutingContext context) {
        return formatResponse(pongService.get());
    }

    private Uni<String> formatResponse(Uni<String> response) {
        return response
                .onFailure().transform(error -> new WebApplicationException(error.getMessage()))
                .map(resp -> "ping-" + resp);
    }
}
