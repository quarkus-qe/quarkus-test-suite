package io.quarkus.ts.stork;

import javax.enterprise.event.Observes;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.consul.ConsulClient;

@RouteBase(path = "/pung", produces = MediaType.TEXT_PLAIN)
public class PungResource {

    @ConfigProperty(name = "stork.pung.service-discovery.consul-host")
    String host;
    @ConfigProperty(name = "stork.pung.service-discovery.consul-port")
    String port;
    @ConfigProperty(name = "pung-service-port")
    String pungPort;
    @ConfigProperty(name = "pung-service-host")
    String pungHost;

    public void init(@Observes StartupEvent ev, Vertx vertx) {
        ConsulClient client = ConsulClient.create(vertx,
                new ConsulClientOptions().setHost(host).setPort(Integer.parseInt(port)));

        client.registerServiceAndAwait(
                new ServiceOptions().setPort(Integer.parseInt(pungPort)).setAddress(pungHost).setName("pung").setId("pung"));
    }

    @Route(path = "/", methods = Route.HttpMethod.GET)
    public Uni<String> pung() {
        return Uni.createFrom().item("pung");
    }
}
