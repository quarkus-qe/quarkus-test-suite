package io.quarkus.ts.configmap.api.server;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
@EnabledIfOpenShiftScenarioPropertyIsTrue
public abstract class BaseConfigMapOpenShiftIT {

    static final int ASSERT_TIMEOUT_MINUTES = 5;

    @Inject
    static OpenShiftClient openshift;

    @Test
    public void configMapEndToEnd() {
        // Simple invocation
        getApp().given().get("/hello")
                .then().statusCode(HttpStatus.SC_OK)
                .body("content", is("Hello World from ConfigMap!"));

        // Parameterized invocation
        getApp().given().queryParam("name", "Albert Einstein")
                .when().get("/hello").then().statusCode(HttpStatus.SC_OK)
                .body("content", is("Hello Albert Einstein from ConfigMap!"));

        // Update config map
        applyConfigMap(getApp(), "configmap-update");
        getApp().restart();

        await().atMost(ASSERT_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            getApp().given().get("/hello").then().statusCode(HttpStatus.SC_OK)
                    .body(containsString("Good morning World from an updated ConfigMap!"));
        });

        // Wrong config map
        applyConfigMap(getApp(), "configmap-broken");
        getApp().restart();

        await().atMost(ASSERT_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            getApp().given().get("/hello").then().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        });
    }

    protected abstract RestService getApp();

    protected static void loadDefaultConfigMap(Service service) {
        applyConfigMap(service, "configmap");
    }

    private static void applyConfigMap(Service service, String name) {
        openshift.apply(service, Paths.get(new File("target/test-classes/" + name + ".yaml").toURI()));
    }
}
