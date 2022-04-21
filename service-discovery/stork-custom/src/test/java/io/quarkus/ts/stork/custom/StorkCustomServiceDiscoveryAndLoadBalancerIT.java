package io.quarkus.ts.stork.custom;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.ValidatableResponse;

import junit.framework.AssertionFailedError;

@QuarkusScenario
@Tag("QUARKUS-1413")
public class StorkCustomServiceDiscoveryAndLoadBalancerIT {

    private static final String PREFIX = "ping-";
    private static final String DEFAULT_PONG_REPLICA_RESPONSE = "pongReplica";
    private static final String DEFAULT_PONG_RESPONSE = "pong";
    private static final String PONG_PORT = getAvailablePort();
    private static final String PONG_REPLICA_PORT = getAvailablePort();

    @QuarkusApplication(classes = PongResource.class)
    static RestService pongService = new RestService().withProperty("quarkus.http.port", PONG_PORT);

    @QuarkusApplication(classes = PongReplicaResource.class)
    static RestService pongReplicaService = new RestService().withProperty("quarkus.http.port", PONG_REPLICA_PORT);

    @QuarkusApplication
    static RestService mainPingService = new RestService()
            .withProperty("stork.pong.load-balancer", "simple")
            .withProperty("stork.pong.service-discovery", "simple")
            .withProperty("stork.pong-replica.load-balancer", "simple")
            .withProperty("stork.pong-replica.service-discovery", "simple")
            .withProperty("stork.pong.service-discovery.pongServicePort", () -> "" + pongService.getPort())
            .withProperty("stork.pong-replica.service-discovery.pongReplicaServicePort",
                    () -> "" + pongReplicaService.getPort());

    @Test
    public void verifyStorkCustomServiceDiscoveryAndLoadBalancer() {
        Map<String, Integer> uniqueResp = new HashMap<>();
        final int requestAmount = 100;
        final int randomLbMinHits = (requestAmount / 2) - 20;
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
        assertThat("Load balancer doesn't follow a random distribution", uniqueResp.get(PREFIX + DEFAULT_PONG_RESPONSE),
                is(greaterThanOrEqualTo(randomLbMinHits)));
        assertThat("Load balancer doesn't follow a random distribution", uniqueResp.get(PREFIX + DEFAULT_PONG_REPLICA_RESPONSE),
                is(greaterThanOrEqualTo(randomLbMinHits)));
    }

    protected ValidatableResponse makePingCall(RestService service, String subPath) {
        return service
                .given()
                .get("/ping/" + subPath).then()
                .statusCode(HttpStatus.SC_OK);
    }

    protected static String getAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return String.valueOf(socket.getLocalPort());
        } catch (IOException e) {
            throw new AssertionFailedError();
        }
    }
}
