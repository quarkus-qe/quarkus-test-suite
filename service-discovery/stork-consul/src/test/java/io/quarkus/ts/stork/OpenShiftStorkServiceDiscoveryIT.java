package io.quarkus.ts.stork;

import static java.util.regex.Pattern.quote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.FileUtils;

@OpenShiftScenario
public class OpenShiftStorkServiceDiscoveryIT {

    private static final String PREFIX = "ping-";
    private static final String CLUSTER_ROLE_FILE_NAME = "cluster-role.yaml";
    private static final String RBAC_FILE_NAME = "fabric8-rbac.yaml";

    @Inject
    static OpenShiftClient openshift;

    @QuarkusApplication(classes = PungResource.class)
    static RestService pung = new RestService()
            .withProperty("stork.pung.service-discovery", "kubernetes")
            .withProperty("stork.pung.service-discovery.k8s-namespace", "all");

    @QuarkusApplication(classes = { PingResource.class, MyBackendPungProxy.class, MyBackendPongProxy.class })
    static RestService ping = new RestService().onPreStart(app -> setupClusterRoles())
            .withProperty("stork.pung.service-discovery", "kubernetes")
            .withProperty("stork.pung.service-discovery.k8s-namespace", "all");

    @AfterAll
    public static void tearDown() {
        openshift.delete(Paths.get(new File("target/test-classes/" + CLUSTER_ROLE_FILE_NAME).toURI()));
        openshift.delete(Paths.get(new File("target/test-classes/" + RBAC_FILE_NAME).toURI()));
    }

    @Test
    public void invokeServiceByName() {
        String response = makePingCall("pung");
        assertThat("Service discovery by name fail.", PREFIX + "pung", is(response));
    }

    @Test
    public void storkLoadBalancerByRoundRobin() {

    }

    private String makePingCall(String subPath) {
        return ping
                .given()
                .get("/ping/" + subPath).then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().asString();
    }

    /**
     * setup `stork-service-discovery-kubernetes` - roles and roles bindings (required)
     */
    private static void setupClusterRoles() {
        String namespace = openshift.project();
        openshift.apply(Paths.get(new File("target/test-classes/" + CLUSTER_ROLE_FILE_NAME).toURI()));
        String content = FileUtils.loadFile(new File("target/test-classes/" + RBAC_FILE_NAME))
                .replaceAll(quote("${NAMESPACE}"), namespace);
        Path target = FileUtils.copyContentTo(content, new File("target/test-classes/" + RBAC_FILE_NAME).toPath());
        openshift.apply(target);
    }
}