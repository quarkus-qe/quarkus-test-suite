package io.quarkus.ts.infinispan.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;

import io.quarkus.test.bootstrap.DataGridInfinispanInstance;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.bootstrap.service.DataGridInfinispan;

public abstract class BaseOpenShiftInfinispanIT {
    protected static final String TARGET_RESOURCES = "target/test-classes/";
    protected static final String CLUSTER_SECRET = TARGET_RESOURCES + "clientcert_secret.yaml";
    protected static final String CLUSTER_CONFIG = TARGET_RESOURCES + "infinispan_cluster_config.yaml";
    protected static final String CLUSTER_CONFIGMAP = TARGET_RESOURCES + "infinispan_cluster_configmap.yaml";
    protected static final String CONNECT_SECRET = TARGET_RESOURCES + "connect_secret.yaml";
    protected static final String TLS_SECRET = TARGET_RESOURCES + "tls_secret.yaml";
    protected static final String CLUSTER_NAMESPACE_NAME = "datagrid-cluster";

    @Inject
    static OpenShiftClient ocClient;

    @DataGridInfinispan(clusterSecret = CLUSTER_SECRET, clusterConfig = CLUSTER_CONFIG, clusterConfigMap = CLUSTER_CONFIGMAP, connectSecret = CONNECT_SECRET, tlsSecret = TLS_SECRET)
    static DataGridInfinispanInstance dataGridInfinispan = new DataGridInfinispanInstance();

    protected static void adjustYml(String yamlFile, String originString, String newString) {
        try {
            Path yamlPath = Paths.get(yamlFile);
            Charset charset = StandardCharsets.UTF_8;

            String yamlContent = Files.readString(yamlPath, charset);
            yamlContent = yamlContent.replace(originString, newString);
            Files.writeString(yamlPath, yamlContent, charset);
        } catch (IOException ex) {
            Assertions.fail("Fail to adjust YAML file. Caused by: " + ex.getMessage());
        }
    }
}
