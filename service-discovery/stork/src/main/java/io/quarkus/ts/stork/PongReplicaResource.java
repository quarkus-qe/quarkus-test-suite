package io.quarkus.ts.stork;

import static io.quarkus.ts.stork.PongResource.PONG_SERVICE_NAME;

import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.consul.ConsulClient;

@RouteBase(path = "/pong", produces = MediaType.TEXT_PLAIN)
public class PongReplicaResource {

    private static final String DEFAULT_PONG_REPLICA_RESPONSE = "pongReplica";

    @ConfigProperty(name = "quarkus.stork.pong-replica.service-discovery.consul-host", defaultValue = "localhost")
    String host;
    @ConfigProperty(name = "quarkus.stork.pong-replica.service-discovery.consul-port", defaultValue = "8500")
    String port;
    @ConfigProperty(name = "pong-replica-service-port", defaultValue = "8080")
    String pongPort;
    @ConfigProperty(name = "pong-replica-service-host", defaultValue = "localhost")
    String pongHost;
    @ConfigProperty(name = "quarkus.stork.pong-replica.service-discovery.type", defaultValue = "consul")
    String serviceDiscoveryType;

    public void init(@Observes StartupEvent ev, Vertx vertx) {
        if (serviceDiscoveryType.equalsIgnoreCase("consul")) {
            ConsulClient client = ConsulClient.create(vertx,
                    new ConsulClientOptions().setHost(host).setPort(Integer.parseInt(port)));

            client.registerServiceAndAwait(
                    new ServiceOptions().setPort(Integer.parseInt(pongPort)).setAddress(pongHost).setName(PONG_SERVICE_NAME)
                            .setId("pongReplica"));
        }
    }

    @Route(path = "*", methods = Route.HttpMethod.GET)
    public Uni<String> pong() {
        return Uni.createFrom().item(DEFAULT_PONG_REPLICA_RESPONSE);
    }
}
