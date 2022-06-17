package io.quarkus.ts.stork.custom;

import javax.ws.rs.core.MediaType;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;

@RouteBase(path = "/pong", produces = MediaType.TEXT_PLAIN)
public class PongReplicaResource {

    private static final String DEFAULT_PONG_REPLICA_RESPONSE = "pongReplica";

    @Route(path = "*", methods = Route.HttpMethod.GET)
    public Uni<String> pong() {
        return Uni.createFrom().item(DEFAULT_PONG_REPLICA_RESPONSE);
    }
}
