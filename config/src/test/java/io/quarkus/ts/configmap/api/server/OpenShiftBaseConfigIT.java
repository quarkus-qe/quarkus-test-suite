package io.quarkus.ts.configmap.api.server;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public abstract class OpenShiftBaseConfigIT {

    static final int ASSERT_TIMEOUT_MINUTES = 5;
    static final String CONFIGMAP = "configmap";
    static final String SECRET = "secret";

    @Inject
    static OpenShiftClient openshift;

    @Test
    public void configMapEndToEnd() {
        // Simple invocation
        getApp().given().get("/hello/message")
                .then().statusCode(HttpStatus.SC_OK)
                .body("content", is("Hello World from " + getConfigType()));

        // Parameterized invocation
        getApp().given().queryParam("name", "Albert Einstein")
                .when().get("/hello/message").then().statusCode(HttpStatus.SC_OK)
                .body("content", is("Hello Albert Einstein from " + getConfigType()));

        // Update config map
        applyConfig(getConfigType() + "-update");
        getApp().restart();

        await().atMost(ASSERT_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            getApp().given().get("/hello/message").then().statusCode(HttpStatus.SC_OK)
                    .body(containsString("Good morning World from an updated " + getConfigType()));
        });

        // Wrong config map
        applyConfig(getConfigType() + "-broken");
        getApp().restart();

        await().atMost(ASSERT_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            getApp().given().get("/hello/message").then().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        });
    }

    protected abstract RestService getApp();

    protected abstract String getConfigType();

    protected static void loadDefaultConfigMap(Service service) {
        applyConfig(CONFIGMAP);
    }

    protected static void loadDefaultConfigSecret(Service service) {
        applyConfig(SECRET);
    }

    private static void applyConfig(String name) {
        applyConfig(name, openshift);
    }

    static void applyConfig(String name, OpenShiftClient openshift) {
        openshift.apply(Paths.get(new File("target/test-classes/" + name + ".yaml").toURI()));
    }
}
