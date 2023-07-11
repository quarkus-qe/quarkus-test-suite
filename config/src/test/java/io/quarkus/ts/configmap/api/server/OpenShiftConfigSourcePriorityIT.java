package io.quarkus.ts.configmap.api.server;

import static io.quarkus.ts.configmap.api.server.OpenShiftBaseConfigIT.CONFIGMAP;
import static io.quarkus.ts.configmap.api.server.OpenShiftBaseConfigIT.SECRET;
import static io.quarkus.ts.configmap.api.server.OpenShiftBaseConfigIT.applyConfig;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftConfigSourcePriorityIT {

    private static final String CONFIGMAP_YAML = CONFIGMAP + "-yaml";
    private static final String CONFIGMAP_YML = CONFIGMAP + "-yml";
    private static final String SECRET_YML = SECRET + "-yml";

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("config-source-priority.properties")
            .onPreStart(OpenShiftConfigSourcePriorityIT::loadConfigSource);

    @Inject
    static OpenShiftClient openShiftClient;

    @Test
    public void testConfigSourcePriorities() {
        final Response response = app.given()
                .get("/hello/properties");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        final HelloConfigProperties configProperties = response.as(HelloConfigProperties.class);
        assertNotNull(configProperties);

        // ConfigMap has higher priority than application properties, and it's possible to create ConfigMap from application.yml.
        assertEquals(format("epilogue %s", CONFIGMAP_YML), configProperties.epilogue);

        // Last ConfigMap has higher priority than ConfigMaps defined earlier, and it's possible to create ConfigMap from application.yaml.
        assertEquals(format("preamble %s", CONFIGMAP_YAML), configProperties.preamble);

        // Last Secret has higher priority than Secrets/ConfigMaps defined earlier.
        assertEquals(format("message %s", SECRET_YML), configProperties.message.orElse(null));

        // Environment variable has the highest priority.
        assertEquals("side note environment variable", configProperties.sideNote);
    }

    @Test
    void configIsIdempotent() throws IOException {
        Predicate<String> containsCommit = line -> line.contains("app.quarkus.io/commit-id");
        Predicate<String> containsTimestamp = line -> line.contains("app.quarkus.io/build-timestamp");

        // todo change to custom path when https://github.com/quarkusio/quarkus/issues/34673 will be fixed
        Path folder = app.getServiceFolder().resolve("target/kubernetes").toAbsolutePath();
        assertTrue(Files.exists(folder), "Folder " + folder + " should exist!");

        // we do not use idempotent mode for kubernetes, so it should stay as before
        Path kubernetesYaml = folder.resolve("kubernetes.yml");
        List<String> kubernetes = Files.readAllLines(kubernetesYaml);
        assertNotEquals(0, kubernetes.size(), "File " + kubernetesYaml + " should exist and have content!");
        assertTrue(kubernetes.stream().anyMatch(containsTimestamp), "File " + kubernetesYaml + " should contain timestamps!");
        //todo https://github.com/quarkusio/quarkus/issues/34749
        //assertTrue(kubernetes.stream().anyMatch(containsCommit), "File " + kubernetesYaml + " should contain commit ids!");

        Path openshiftYaml = folder.resolve("openshift.yml");
        List<String> openshift = Files.readAllLines(openshiftYaml);
        assertNotEquals(0, openshift.size(), "File " + openshiftYaml + " should exist and have content!");

        // openshift uses idempotent mode
        List<String> offendingLines = openshift.stream()
                .filter(containsTimestamp.or(containsCommit))
                .collect(Collectors.toList());
        assertEquals(Collections.emptyList(), offendingLines, "Non-idempotent lines found!");

    }

    private static void loadConfigSource(Service service) {
        // create Secrets and ConfigMaps
        applyConfig(CONFIGMAP_YAML, openShiftClient);
        applyConfig(CONFIGMAP_YML, openShiftClient);
        applyConfig(CONFIGMAP, openShiftClient);
        applyConfig(SECRET, openShiftClient);
        applyConfig(SECRET_YML, openShiftClient);
    }
}
