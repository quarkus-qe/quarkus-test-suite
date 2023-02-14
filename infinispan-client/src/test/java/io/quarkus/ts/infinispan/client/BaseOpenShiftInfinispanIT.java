package io.quarkus.ts.infinispan.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;

import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.utils.Command;

public abstract class BaseOpenShiftInfinispanIT {
    protected static final String TARGET_RESOURCES = "target/test-classes/";
    protected static final String CLUSTER_SECRET = "clientcert_secret.yaml";
    protected static final String CLUSTER_CONFIG = "infinispan_cluster_config.yaml";
    protected static final String CLUSTER_CONFIGMAP = "infinispan_cluster_configmap.yaml";
    protected static final String CONNECT_SECRET = "connect_secret.yaml";
    protected static final String TLS_SECRET = "tls_secret.yaml";
    protected static final String CLUSTER_NAMESPACE_NAME = "datagrid-cluster";
    protected static String NEW_CLUSTER_NAME = null;

    private static final String ORIGIN_CLUSTER_NAME = "totally-random-infinispan-cluster-name";

    @Inject
    static OpenShiftClient ocClient;

    @AfterAll
    public static void deleteInfinispanCluster() {
        deleteYaml(CLUSTER_CONFIGMAP);
        deleteYaml(CLUSTER_CONFIG);
        adjustYml(CLUSTER_CONFIG, NEW_CLUSTER_NAME, ORIGIN_CLUSTER_NAME);
        adjustYml(CLUSTER_CONFIGMAP, NEW_CLUSTER_NAME, ORIGIN_CLUSTER_NAME);
    }

    protected static void adjustYml(String yamlFile, String originString, String newString) {
        try {
            Path yamlPath = Paths.get(TARGET_RESOURCES + yamlFile);
            Charset charset = StandardCharsets.UTF_8;

            String yamlContent = new String(Files.readAllBytes(yamlPath), charset);
            yamlContent = yamlContent.replace(originString, newString);
            Files.write(yamlPath, yamlContent.getBytes(charset));
        } catch (IOException ex) {
            Assertions.fail("Fail to adjust YAML file. Caused by: " + ex.getMessage());
        }
    }

    /**
     * Apply the YAML file.
     */
    protected static void applyYaml(String yamlFile) {
        try {
            new Command("oc", "apply", "-f", Paths.get(TARGET_RESOURCES + yamlFile).toString()).runAndWait();
        } catch (Exception e) {
            Assertions.fail("Failed to apply YAML file. Caused by: " + e.getMessage());
        }
    }

    protected static void createInfinispanCluster() {
        applyYaml(CLUSTER_SECRET);
        applyYaml(CONNECT_SECRET);
        applyYaml(TLS_SECRET);

        // there should be unique name for every created infinispan cluster to be able parallel runs
        NEW_CLUSTER_NAME = ocClient.project() + "-infinispan-cluster";

        // rename infinispan cluster and configmap
        adjustYml(CLUSTER_CONFIG, ORIGIN_CLUSTER_NAME, NEW_CLUSTER_NAME);
        applyYaml(CLUSTER_CONFIG);
        adjustYml(CLUSTER_CONFIGMAP, ORIGIN_CLUSTER_NAME, NEW_CLUSTER_NAME);
        applyYaml(CLUSTER_CONFIGMAP);

        try {
            new Command("oc", "-n", CLUSTER_NAMESPACE_NAME, "wait", "--for", "condition=wellFormed", "--timeout=300s",
                    "infinispan/" + NEW_CLUSTER_NAME).runAndWait();
        } catch (Exception e) {
            deleteInfinispanCluster();
            Assertions.fail("Fail to wait Infinispan resources to start. Caused by: " + e.getMessage());
        }
    }

    /**
     *
     * Delete the YAML file.
     */
    private static void deleteYaml(String yamlFile) {
        try {
            new Command("oc", "delete", "-f", Paths.get(TARGET_RESOURCES + yamlFile).toString()).runAndWait();
        } catch (Exception e) {
            Assertions.fail("Failed to delete YAML file. Caused by: " + e.getMessage());
        }
    }
}
