package io.quarkus.ts.sb.reactive;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.test.utils.Command;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtensionAndDockerBuildStrategy)
public class OpenShiftPostgreSqlReactiveSbIT {

    private static final String PG_CLUSTER_YML = "pg-cluster.yml";

    @Inject
    static OpenShiftClient ocClient;

    @QuarkusApplication
    static RestService app = new RestService()
            .onPreStart(s -> createPostgresCluster());

    @AfterAll
    public static void tearDown() {
        deleteCustomResourceDefinition();
    }

    /**
     * This test verifies the application deploys successfully. If the binding fails, the application will not be
     * available.
     */
    @Test
    public void verifyPostgresServiceBoundToApplication() {
        app.given()
                .get("/todo/1/")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", Matchers.equalTo("Finish the blog post"));
    }

    private static boolean areRequiredOperatorsInstalled() {
        List<String> output = new ArrayList<>();
        try {
            new Command("oc", "get", "csv").outputToLines(output).runAndWait();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        String outputString = output.stream().collect(Collectors.joining(System.lineSeparator()));
        return (outputString.contains("postgresoperator") && outputString.contains("service-binding-operator"));
    }

    private static boolean arePodsInstalled() {
        try {
            List<String> output = new ArrayList<>();
            new Command("oc", "get", "pods").outputToLines(output).runAndWait();
            return output.stream()
                    .anyMatch(line -> line.contains("hippo"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void createPostgresCluster() {
        // this wait is necessary as it takes some time for API to populate new namespace with objects
        AwaitilityUtils
                .untilIsTrue(OpenShiftPostgreSqlReactiveSbIT::areRequiredOperatorsInstalled,
                        AwaitilityUtils.AwaitilitySettings.using(Duration.ofSeconds(10),
                                Duration.ofSeconds(60)));
        applyCustomResourceDefinition();
        // sometimes operator takes a while to create an object
        AwaitilityUtils
                .untilIsTrue(OpenShiftPostgreSqlReactiveSbIT::arePodsInstalled,
                        AwaitilityUtils.AwaitilitySettings.using(Duration.ofSeconds(2),
                                Duration.ofSeconds(30)));
        try {
            new Command("oc", "-n", ocClient.project(), "wait", "--for", "condition=Ready", "--timeout=300s",
                    "pods", "--all").runAndWait();
        } catch (Exception e) {
            deleteCustomResourceDefinition();
            Assertions.fail("PostgresCluster did not form correctly. Caused by: " + e.getMessage());
        }
    }

    private static void applyCustomResourceDefinition() {
        try {
            new Command("oc", "apply", "-f", Paths.get("target/test-classes/" + PG_CLUSTER_YML).toString()).runAndWait();
        } catch (Exception e) {
            Assertions.fail("Failed to apply YAML file. Caused by: " + e.getMessage());
        }
    }

    private static void deleteCustomResourceDefinition() {
        try {
            new Command("oc", "delete", "-f", Paths.get("target/test-classes/" + PG_CLUSTER_YML).toString()).runAndWait();
        } catch (Exception e) {
            Assertions.fail("Failed to delete YAML file. Caused by: " + e.getMessage());
        }
    }

}
