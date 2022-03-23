package io.quarkus.ts.stork;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.ConsulService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnNative(reason = "Too heavy native scenario in terms of memory consumption. Replaced by NativeStorkServiceDiscoveryIT")
public class StorkLoadBalancerIT extends AbstractCommonTestCases {

    @Container(image = "${consul.image}", expectedLog = "Synced node info", port = 8500)
    static ConsulService consul = new ConsulService();

    private static final String DEFAULT_PONG_REPLICA_RESPONSE = "pongReplica";
    private static final String DEFAULT_PONG_RESPONSE = "pong";
    private static final String PONG_PORT = getAvailablePort();
    private static final String PONG_REPLICA_PORT = getAvailablePort();

    @QuarkusApplication(classes = PongResource.class)
    static RestService pongService = new RestService()
            .withProperty("quarkus.http.port", PONG_PORT)
            .withProperty("pong-service-port", PONG_PORT)
            .withProperty("pong-service-host", "localhost")
            .withProperty("stork.pong.service-discovery.refresh-period", "1")
            .withProperty("stork.pong.service-discovery", "consul")
            .withProperty("stork.pong.service-discovery.consul-port", () -> String.valueOf(consul.getPort()))
            .withProperty("stork.pong.service-discovery.consul-host", () -> getConsultEndpoint(consul.getConsulEndpoint()));

    @QuarkusApplication(classes = PongReplicaResource.class)
    static RestService pongReplicaService = new RestService()
            .withProperty("quarkus.http.port", PONG_REPLICA_PORT)
            .withProperty("pong-replica-service-port", PONG_REPLICA_PORT)
            .withProperty("pong-replica-service-host", "localhost")
            .withProperty("stork.pong-replica.service-discovery.refresh-period", "1")
            .withProperty("stork.pong-replica.service-discovery", "consul")
            .withProperty("stork.pong-replica.service-discovery.consul-port", () -> String.valueOf(consul.getPort()))
            .withProperty("stork.pong-replica.service-discovery.consul-host",
                    () -> getConsultEndpoint(consul.getConsulEndpoint()));

    @QuarkusApplication(classes = { PingResource.class, MyBackendPungProxy.class, MyBackendPongProxy.class })
    static RestService mainPingService = new RestService()
            .withProperty("stork.pong-replica.service-discovery", "consul")
            .withProperty("stork.pong-replica.service-discovery.refresh-period", "1")
            .withProperty("stork.pong-replica.load-balancer", "round-robin")
            .withProperty("stork.pong-replica.service-discovery.consul-port", () -> String.valueOf(consul.getPort()))
            .withProperty("stork.pong-replica.service-discovery.consul-host",
                    () -> getConsultEndpoint(consul.getConsulEndpoint()))
            .withProperty("stork.pong.service-discovery", "consul")
            .withProperty("stork.pong.service-discovery.refresh-period", "1")
            .withProperty("stork.pong.load-balancer", "round-robin")
            .withProperty("stork.pong.service-discovery.consul-port", () -> String.valueOf(consul.getPort()))
            .withProperty("stork.pong.service-discovery.consul-host", () -> getConsultEndpoint(consul.getConsulEndpoint()));

    @Test
    public void storkLoadBalancerByRoundRobin() {
        Map<String, Integer> uniqueResp = new HashMap<>();
        final int requestAmount = 100;
        final int roundRobinError = (requestAmount / 2) - 1;
        for (int i = 0; i < requestAmount; i++) {
            String response = makePingCall(mainPingService, "pong").extract().body().asString();
            if (uniqueResp.containsKey(response)) {
                uniqueResp.put(response, uniqueResp.get(response) + 1);
            } else {
                uniqueResp.put(response, 1);
            }
        }

        Assertions.assertEquals(uniqueResp.size(), 2, "Only 2 services should response");
        assertThat("Unexpected service names", uniqueResp.keySet(),
                hasItems(PREFIX + DEFAULT_PONG_RESPONSE, PREFIX + DEFAULT_PONG_REPLICA_RESPONSE));
        assertThat("Load balancer doesn't behaves as round-robin", uniqueResp.get(PREFIX + DEFAULT_PONG_RESPONSE),
                is(greaterThanOrEqualTo(roundRobinError)));
        assertThat("Load balancer doesn't behaves as round-robin", uniqueResp.get(PREFIX + DEFAULT_PONG_REPLICA_RESPONSE),
                is(greaterThanOrEqualTo(roundRobinError)));
    }
}
