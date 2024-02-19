package io.quarkus.ts.infinispan.client;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.test.utils.Command;
import io.restassured.response.Response;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OperatorOpenShiftInfinispanCountersIT extends BaseOpenShiftInfinispanIT {

    private static final AtomicBoolean CLUSTER_CREATED = new AtomicBoolean(false);

    @QuarkusApplication
    static RestService one = new RestService()
            .onPreStart(s -> {
                // prevent attempting to create cluster on restart
                if (CLUSTER_CREATED.compareAndSet(false, true)) {
                    createInfinispanCluster();
                }
            });

    @QuarkusApplication
    static RestService two = new RestService();

    /**
     * Simple check of connection to endpoints
     * <p>
     * Expected values = 0
     */
    @Test
    @Order(1)
    public void testConnectToEndpoints() {
        String firstEndpointCache = getCounterValue(one, "/first-counter/get-cache");
        String secondEndpointCache = getCounterValue(one, "/second-counter/get-cache");

        assertEquals("0", secondEndpointCache);
        assertEquals(firstEndpointCache, secondEndpointCache);
    }

    /**
     * Test increment counters by 1
     *
     * Client counters should be 1 for both endpoints
     * Cache counter is shared and should be 2
     */
    @Test
    @Order(2)
    public void testUpdateCacheOnEndpoints() {
        String firstEndpointCounters = fillTheCache(one, "/first-counter/increment-counters");
        String secondEndpointCounters = fillTheCache(one, "/second-counter/increment-counters");

        assertEquals("Cache=1 Client=1", firstEndpointCounters);
        assertEquals("Cache=2 Client=1", secondEndpointCounters);
    }

    /**
     * Client fail-over test. Testing the Quarkus application will connect back to the DataGrid server after restart.
     *
     * Cache counter should remain the same.
     * Client counter is reset to 0
     */
    @Test
    @Order(3)
    public void testCacheAfterClientsRestart() {
        resetCacheCounter(one, "/first-counter/reset-cache");
        resetClientCounter(one, "/first-counter/reset-client");

        // fill the cache
        incrementCountersOnValue(one, "/first-counter/increment-counters", 10);

        // restart the app
        restart(one);

        String cacheCounter = getCounterValue(one, "/first-counter/get-cache");
        String clientCounter = getCounterValue(one, "/first-counter/get-client");

        assertEquals("10", cacheCounter);
        assertEquals("0", clientCounter);
    }

    /**
     * Client fail-over test. Testing the request to the DataGrid server by the failed Quarkus application.
     *
     * Cache counter should remain the same.
     * Client counter is reset to 0
     */
    @Test
    @Order(4)
    public void testInvokeWithFailedNode() {
        resetCacheCounter(one, "/first-counter/reset-cache");
        resetClientCounter(one, "/first-counter/reset-client");

        incrementCountersOnValue(one, "/first-counter/increment-counters", 10);

        // kill the app = fail of the client
        one.stop();

        // wait for app to actually be down
        AwaitilityUtils.untilIsTrue(() -> ocClient.podsInService(one).size() == 0);

        // try to invoke the cache
        one.given()
                .put("/first-counter/increment-counters")
                .then()
                .statusCode(Matchers.allOf(Matchers.greaterThanOrEqualTo(500), Matchers.lessThan(600)));

        // turn-on the app
        one.start();

        String cacheCounter = getCounterValue(one, "/first-counter/get-cache");
        String clientCounter = getCounterValue(one, "/first-counter/get-client");

        assertEquals("10", cacheCounter);
        assertEquals("0", clientCounter);
    }

    /**
     * Infinispan fail-over test. Testing restart the infinispan cluster in DataGrid operator and wait the Quarkus
     * application connects back. The restart is done by reducing the number of infinispan cluster replicas to 0 and it waits
     * for the shutdown condition. Then the number of replicas is changed back to 1.
     *
     * We don't have cache backup in this test case, so the cache is deleted by the restart of infinispan cluster.
     * The cache definition "mycache" remains, but the "counter" cache in it is deleted.
     * Client counter should remain with the same value after the restart.
     */
    @Test
    @Order(5)
    public void testRestartInfinispanCluster() throws IOException, InterruptedException {
        resetCacheCounter(one, "/first-counter/reset-cache");
        resetClientCounter(one, "/first-counter/reset-client");

        incrementCountersOnValue(one, "/first-counter/increment-counters", 10);

        killInfinispanCluster();
        restartInfinispanCluster();

        // try to connect back to infinispan cluster and expect no content
        await().atMost(5, TimeUnit.MINUTES).untilAsserted(() -> {
            one.given()
                    .get("/first-counter/get-cache")
                    .then()
                    .statusCode(204);
        });

        String clientCounter = getCounterValue(one, "/first-counter/get-client");
        assertEquals("10", clientCounter);
    }

    /**
     * Infinispan fail-over test. Testing a restart of the infinispan cluster and increment/change the cache counter value
     * after the restart. The cache is deleted by the restart of infinispan cluster. Because of this, we need to fill the cache
     * again. It is done by 'cache.put("counter", 0)'. Then it could be incremented.
     *
     * Cache newly created after the restart and incremented by 1 so it should be only 1.
     * Client counter should remain the same during the restart and after the counter incrementing should by increased by 1.
     */
    @Test
    @Order(6)
    public void testIncrementAfterRestartInfinispanCluster() throws IOException, InterruptedException {
        resetCacheCounter(one, "/first-counter/reset-cache");
        resetClientCounter(one, "/first-counter/reset-client");

        incrementCountersOnValue(one, "/first-counter/increment-counters", 10);

        killInfinispanCluster();
        restartInfinispanCluster();

        // try to connect back to infinispan cluster and expect no content
        await().atMost(5, TimeUnit.MINUTES).untilAsserted(() -> {
            one.given()
                    .get("/first-counter/get-cache")
                    .then()
                    .statusCode(204);
        });

        // create the deleted cache counter again
        String zeroCache = fillTheCache(one, "/first-counter/reset-cache");
        // try to increment counters
        String firstEndpointCounters = fillTheCache(one, "/first-counter/increment-counters");

        assertEquals("Cache=0", zeroCache);
        assertEquals("Cache=1 Client=11", firstEndpointCounters);
    }

    /**
     * Infinispan fail-over test. Test invoke a request on the Infinispan server which is currently down.
     * Because of our settings in the hotrod-client.properties file, the application is trying to connect only once and only 1s.
     * By default, the app is trying to connect 60 s with 10 retries even when the next tests continue. It means that the
     * counter
     * could be unexpectedly increased in one of the next tests
     *
     * Cache should be empty (status code 204).
     * Client counter should be increased even if the server is down.
     */
    @Test
    @Order(7)
    public void testInvokeOnFailedInfinispanCluster() throws IOException, InterruptedException {
        resetCacheCounter(one, "/first-counter/reset-cache");
        resetClientCounter(one, "/first-counter/reset-client");

        incrementCountersOnValue(one, "/first-counter/increment-counters", 10);

        killInfinispanCluster();

        // try to increment counters
        one.given()
                .put("/first-counter/increment-counters")
                .then()
                .statusCode(Matchers.allOf(Matchers.greaterThanOrEqualTo(500), Matchers.lessThan(600)));

        restartInfinispanCluster();

        // try to connect back to infinispan cluster and expect no content
        await().atMost(5, TimeUnit.MINUTES).untilAsserted(() -> {
            one.given()
                    .get("/first-counter/get-cache")
                    .then()
                    .statusCode(204);
        });

        // check the client counter
        String clientCounter = getCounterValue(one, "/first-counter/get-client");
        assertEquals("11", clientCounter);
    }

    /**
     * Check the connection to the second client (second Quarkus application).
     */
    @Test
    @Order(8)
    public void testConnectSecondClient() {
        resetCacheCounter(one, "/first-counter/reset-cache");

        String secondClientCache = getCounterValue(two, "/first-counter/get-cache");
        assertEquals("0", secondClientCache);
    }

    /**
     * Testing the cache is shared between clients (apps). Every client has its own client counter.
     *
     * Clients counters should be increased only if the increase is called by their client.
     * Cache counter is shared and should contain the sum of both client counters.
     */
    @Test
    @Order(9)
    public void testMultipleClientIncrement() {
        // reset the first and client
        resetCacheCounter(one, "/first-counter/reset-cache");
        resetClientCounter(one, "/first-counter/reset-client");
        resetClientCounter(two, "/first-counter/reset-client");

        // fill the cache in first and second client
        incrementCountersOnValue(one, "/first-counter/increment-counters", 10);
        incrementCountersOnValue(two, "/first-counter/increment-counters", 10);

        // save the cache counters in first and second client
        String firstClientCacheCounter = getCounterValue(one, "/first-counter/get-cache");
        String secondClientCacheCounter = getCounterValue(two, "/first-counter/get-cache");

        // save the client counters in first and second client
        String firstClientAppCounter = getCounterValue(one, "/first-counter/get-client");
        String secondClientAppCounter = getCounterValue(two, "/first-counter/get-client");

        assertEquals("10", firstClientAppCounter);
        assertEquals("10", secondClientAppCounter);

        // sum of both client counters
        String cacheValue = String.valueOf(Integer.valueOf(firstClientAppCounter) + Integer.valueOf(secondClientAppCounter));
        assertEquals(cacheValue, firstClientCacheCounter);
        assertEquals(cacheValue, secondClientCacheCounter);
    }

    /**
     * Multiple client Infinispan fail-over test. Testing restart the infinispan cluster and increment/change counters values
     * of both client applications after the restart.
     *
     * Cache newly created after the restart and incremented by 1 by each client so it should by on value 2.
     * Client counters should remain the same during the restart and after the counters incrementing both are increased by 1.
     */
    @Test
    @Order(10)
    public void testMultipleClientDataAfterRestartInfinispanCluster() throws IOException, InterruptedException {
        resetCacheCounter(one, "/first-counter/reset-cache");
        resetClientCounter(one, "/first-counter/reset-client");
        resetClientCounter(two, "/first-counter/reset-client");

        // update the cache in both clients
        String firstClientCounters = fillTheCache(one, "/first-counter/increment-counters");
        String secondClientCounters = fillTheCache(two, "/first-counter/increment-counters");

        assertEquals("Cache=1 Client=1", firstClientCounters);
        assertEquals("Cache=2 Client=1", secondClientCounters);

        killInfinispanCluster();
        restartInfinispanCluster();

        // wait for clients to be reconnected to infinispan cluster
        await().atMost(1, TimeUnit.MINUTES).untilAsserted(() -> {
            one.given()
                    .get("/first-counter/get-cache")
                    .then()
                    .statusCode(Matchers.allOf(Matchers.greaterThanOrEqualTo(200), Matchers.lessThan(300)));
        });
        await().atMost(1, TimeUnit.MINUTES).untilAsserted(() -> {
            two.given()
                    .get("/first-counter/get-cache")
                    .then()
                    .statusCode(Matchers.allOf(Matchers.greaterThanOrEqualTo(200), Matchers.lessThan(300)));
        });

        // create the deleted cache counter again
        resetCacheCounter(one, "/first-counter/reset-cache");

        // increment counters by the first and second client
        firstClientCounters = fillTheCache(one, "/first-counter/increment-counters");
        secondClientCounters = fillTheCache(two, "/first-counter/increment-counters");

        assertEquals("Cache=1 Client=2", firstClientCounters);
        assertEquals("Cache=2 Client=2", secondClientCounters);
    }

    /**
     * Setting the cache counter value to 0 from provided client url address.
     * At the end, the cache value is tested that it is actually 0.
     */
    private void resetCacheCounter(RestService node, String url) {
        await().atMost(5, TimeUnit.MINUTES).untilAsserted(() -> {
            node.given()
                    .put(url)
                    .then()
                    .body(is("Cache=0"));
        });
    }

    /**
     * Setting the client atomic integer counter to 0 in the provided client url address.
     * At the end, the client counter value is tested that it is actually 0.
     */
    private void resetClientCounter(RestService node, String url) {
        await().atMost(5, TimeUnit.MINUTES).untilAsserted(() -> {
            node.given()
                    .put(url)
                    .then()
                    .body(is("Client=0"));
        });
    }

    /**
     * Restart node.
     */
    private void restart(RestService one) {
        one.restart();
        waitForAppToBeUpAndRunning(one);
    }

    /**
     * Wait for the node to be up and running.
     */
    private void waitForAppToBeUpAndRunning(RestService node) {
        await().atMost(5, TimeUnit.MINUTES).untilAsserted(() -> {
            node.given()
                    .get("/q/health")
                    .then().statusCode(HttpStatus.SC_OK);
        });
    }

    /**
     * Getting the value of either cache or client counters from the provided url address.
     * Tested is only the right returned status code.
     */
    private String getCounterValue(RestService node, String url) {
        Response response = node.given().get(url);
        String body = response.body().asString();
        Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), body);
        return body;
    }

    /**
     * Increasing cache and client counters by 1 from the provided url address.
     */
    private String fillTheCache(RestService node, String url) {
        String actualResponse = node.given()
                .put(url)
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        return actualResponse;
    }

    /**
     * Increasing cache and client counters by the provided count value from the provided url address.
     */
    private void incrementCountersOnValue(RestService node, String url, int count) {
        for (int i = 1; i <= count; i++) {
            node.given()
                    .put(url)
                    .then()
                    .statusCode(HttpStatus.SC_OK);
        }
    }

    /**
     * Reduces the number of infinispan cluster replicas to 0 and wait for the shutdown condition. It is done by changing
     * the YAML file in the target/test-classes directory.
     */
    private void killInfinispanCluster() throws IOException, InterruptedException {
        adjustYml(CLUSTER_CONFIG, "replicas: 1", "replicas: 0");
        applyYaml(CLUSTER_CONFIG);
        new Command("oc", "-n", CLUSTER_NAMESPACE_NAME, "wait", "--for", "condition=gracefulShutdown", "--timeout=300s",
                "infinispan/" + NEW_CLUSTER_NAME).runAndWait();
    }

    /**
     * The number of replicas is increased back to value 1 the same way as in "killInfinispanCluster()" method. The wait command
     * expects "wellFormed" condition in Infinispan cluster status.
     */
    private void restartInfinispanCluster() throws IOException, InterruptedException {
        adjustYml(CLUSTER_CONFIG, "replicas: 0", "replicas: 1");
        applyYaml(CLUSTER_CONFIG);
        new Command("oc", "-n", CLUSTER_NAMESPACE_NAME, "wait", "--for", "condition=wellFormed", "--timeout=360s",
                "infinispan/" + NEW_CLUSTER_NAME).runAndWait();
    }
}
