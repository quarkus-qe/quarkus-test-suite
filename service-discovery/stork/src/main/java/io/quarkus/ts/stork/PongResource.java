package io.quarkus.ts.stork;

import java.util.UUID;

import javax.enterprise.event.Observes;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.consul.ConsulClient;

@RouteBase(path = "/pong", produces = MediaType.TEXT_PLAIN)
public class PongResource {

    public static final String PONG_SERVICE_NAME = "pong";
    private static final String DEFAULT_PONG_RESPONSE = "pong";
    private static final String HEADER_ID = "x-id";
    private String instanceUniqueId;

    @ConfigProperty(name = "quarkus.stork.pong.service-discovery.consul-host", defaultValue = "localhost")
    String host;
    @ConfigProperty(name = "quarkus.stork.pong.service-discovery.consul-port", defaultValue = "8500")
    String port;
    @ConfigProperty(name = "pong-service-port", defaultValue = "8080")
    String pongPort;
    @ConfigProperty(name = "pong-service-host", defaultValue = "localhost")
    String pongHost;
    @ConfigProperty(name = "quarkus.stork.pong.service-discovery.type", defaultValue = "consul")
    String serviceDiscoveryType;

    public void init(@Observes StartupEvent ev, Vertx vertx) {
        instanceUniqueId = UUID.randomUUID().toString();
        if (serviceDiscoveryType.equalsIgnoreCase("consul")) {
            ConsulClient client = ConsulClient.create(vertx,
                    new ConsulClientOptions().setHost(host).setPort(Integer.parseInt(port)));

            client.registerServiceAndAwait(
                    new ServiceOptions().setPort(Integer.parseInt(pongPort)).setAddress(pongHost).setName(PONG_SERVICE_NAME)
                            .setId("pong"));
        }
    }

    @Route(path = "/", methods = Route.HttpMethod.GET)
    public void pong(final RoutingContext context) {
        context.response().putHeader(HEADER_ID, instanceUniqueId).end(DEFAULT_PONG_RESPONSE);
    }
}
