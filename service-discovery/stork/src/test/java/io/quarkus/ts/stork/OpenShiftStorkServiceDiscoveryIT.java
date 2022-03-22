package io.quarkus.ts.stork;

import static io.quarkus.ts.stork.PingResource.HEADER_ID;
import static io.quarkus.ts.stork.PingResource.PING_PREFIX;
import static java.util.regex.Pattern.quote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.FileUtils;

@OpenShiftScenario
@DisabledOnQuarkusVersion(version = "2\\..*", reason = "QE OCP user need more privilege in order to be able to create thr required ClusterRole")
public class OpenShiftStorkServiceDiscoveryIT extends AbstractCommonTestCases {

    private static final String CLUSTER_ROLE_FILE_NAME = "cluster-role.yaml";
    private static final String RBAC_FILE_NAME = "fabric8-rbac.yaml";
    private static final int PONG_INSTANCES_AMOUNT = 2; // we need at least two instances in order to verify stork LB

    @Inject
    static OpenShiftClient openshift;

    @QuarkusApplication(classes = PungResource.class)
    static RestService pung = new RestService()
            .withProperty("stork.pung.service-discovery", "kubernetes")
            .withProperty("stork.pung.service-discovery.k8s-namespace", "all");

    @QuarkusApplication(classes = PongResource.class)
    static RestService pong = new RestService()
            .onPostStart(app -> openshift.scaleTo(app, PONG_INSTANCES_AMOUNT))
            .withProperty("stork.pong.service-discovery", "kubernetes")
            .withProperty("stork.pong.service-discovery.k8s-namespace", "all");

    @QuarkusApplication(classes = { PingResource.class, MyBackendPungProxy.class, MyBackendPongProxy.class })
    static RestService ping = new RestService().onPreStart(app -> setupClusterRoles())
            .withProperty("stork.pong.service-discovery", "kubernetes")
            .withProperty("stork.pong.service-discovery.k8s-namespace", "all")
            .withProperty("stork.pung.service-discovery", "kubernetes")
            .withProperty("stork.pung.service-discovery.k8s-namespace", "all");

    @AfterAll
    public static void tearDown() {
        openshift.delete(Paths.get(new File("target/test-classes/" + CLUSTER_ROLE_FILE_NAME).toURI()));
        openshift.delete(Paths.get(new File("target/test-classes/" + RBAC_FILE_NAME).toURI()));
    }

    @Test
    public void invokeServiceByName() {
        String response = makePingCall(ping, "pung").extract().body().asString();
        assertThat("Service discovery by name fail.", PING_PREFIX + "pung", is(response));
    }

    @Test
    public void storkLoadBalancerByRoundRobin() {
        Map<String, Integer> uniqueResp = new HashMap<>();
        final int requestAmount = 10;
        final int roundRobinError = (requestAmount / PONG_INSTANCES_AMOUNT) - 1;
        for (int i = 0; i < requestAmount; i++) {
            String pongInstanceId = makePingCall(ping, "pong").extract().header(HEADER_ID);
            if (uniqueResp.containsKey(pongInstanceId)) {
                uniqueResp.put(pongInstanceId, uniqueResp.get(pongInstanceId) + 1);
            } else {
                uniqueResp.put(pongInstanceId, 1);
            }
        }

        Assertions.assertEquals(uniqueResp.size(), PONG_INSTANCES_AMOUNT,
                "Only " + PONG_INSTANCES_AMOUNT + " services should response");

        for (Map.Entry<String, Integer> pod : uniqueResp.entrySet()) {
            Assertions.assertTrue(uniqueResp.get(pod.getKey()) >= roundRobinError,
                    "Request load is not distributed following a round-robin distribution");
        }
    }

    /**
     * setup `stork-service-discovery-kubernetes` - roles and roles bindings (required)
     */
    private static void setupClusterRoles() {
        String namespace = openshift.project();
        String clusterRoleContent = FileUtils.loadFile(new File("target/test-classes/" + CLUSTER_ROLE_FILE_NAME))
                .replaceAll(quote("${NAMESPACE}"), namespace);
        openshift.apply(FileUtils.copyContentTo(clusterRoleContent,
                new File("target/test-classes/" + CLUSTER_ROLE_FILE_NAME).toPath()));
        String contentRBAC = FileUtils.loadFile(new File("target/test-classes/" + RBAC_FILE_NAME))
                .replaceAll(quote("${NAMESPACE}"), namespace);
        openshift.apply(FileUtils.copyContentTo(contentRBAC, new File("target/test-classes/" + RBAC_FILE_NAME).toPath()));
    }
}
