package io.quarkus.ts.infinispan.client;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.Command;
import io.quarkus.ts.infinispan.client.serialized.ShopItem;
import io.restassured.response.Response;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OperatorOpenShiftInfinispanObjectsIT {

    private static final String ORIGIN_CLUSTER_NAME = "totally-random-infinispan-cluster-name";

    private static final String TARGET_RESOURCES = "target/test-classes/";
    private static final String CLUSTER_SECRET = "clientcert_secret.yaml";
    private static final String CLUSTER_CONFIG = "infinispan_cluster_config.yaml";
    private static final String CLUSTER_CONFIGMAP = "infinispan_cluster_configmap.yaml";
    private static final String CONNECT_SECRET = "connect_secret.yaml";
    private static final String TLS_SECRET = "tls_secret.yaml";

    private static final String CLUSTER_NAMESPACE_NAME = "datagrid-cluster";
    private static String NEW_CLUSTER_NAME = null;

    private static final int CACHE_ENTRY_MAX = 5;
    private static final int CACHE_LIFESPAN_SEC = 10;
    private static final int CACHE_IDLE_TIME_SEC = 10;
    private static final String ALL = null;

    private Response response;

    private List<ShopItem> maxThresholdItemList = Arrays.asList(
            new ShopItem("Item 1", 100, ShopItem.Type.ELECTRONIC),
            new ShopItem("Item 2", 200, ShopItem.Type.ELECTRONIC),
            new ShopItem("Item 3", 300, ShopItem.Type.ELECTRONIC),
            new ShopItem("Item 4", 400, ShopItem.Type.MECHANICAL),
            new ShopItem("Item 5", 500, ShopItem.Type.MECHANICAL));

    @Inject
    static OpenShiftClient ocClient;

    @QuarkusApplication
    static RestService one = new RestService()
            .onPreStart(OperatorOpenShiftInfinispanObjectsIT::createInfinispanCluster);

    @AfterEach
    public void afterEach() {
        clearCache();
    }

    /**
     * Test cache maximum entry size (5). By adding new elements to full cache the old ones will be removed.
     */
    @Test
    @Order(1)
    public void testCacheSizeEviction() {
        ShopItem additionalItem = new ShopItem("Item 6", 600, ShopItem.Type.MECHANICAL);
        whenAddCacheItems(maxThresholdItemList);
        whenAddCacheItems(Arrays.asList(additionalItem));
        whenQueryCachedItems(ALL);
        thenCacheSizeMustBe(is(CACHE_ENTRY_MAX));
        thenCacheBodyMust(containsString("Item 6"));
    }

    /**
     * Test that all items are removed from cache after lifespan time
     */
    @Test
    @Order(2)
    public void testCacheEvictionByLifespan() {
        whenAddCacheItemsWithLifespan(maxThresholdItemList, CACHE_LIFESPAN_SEC);

        await().atMost(Duration.ofSeconds(CACHE_LIFESPAN_SEC + 1)).untilAsserted(() -> {
            whenQueryCachedItems(ALL);
            thenCacheIsEmpty();
        });
    }

    /**
     * Test Querying of protobuf Serialized objects
     */
    @Test
    @Order(3)
    public void testQueryOnSerializedObjects() {
        whenAddCacheItems(maxThresholdItemList);
        whenQueryCachedItems("from quarkus_qe.ShopItem where type = \"ELEC\"");
        thenCacheSizeMustBe(is(3));
        thenCacheBodyMust(not(containsString("MECHANICAL")));
    }

    /**
     * TODO JIRA ISPN issue: https://issues.redhat.com/browse/ISPN-13292
     */
    @Test
    @Order(4)
    @Disabled
    public void testCacheEvictionByLifespanAndIdleTime() {
        whenAddCacheItemsWithLifespanAndIdleTime(maxThresholdItemList, CACHE_LIFESPAN_SEC + 20, CACHE_IDLE_TIME_SEC);
        await().pollDelay(Duration.ofSeconds(15)).atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            whenQueryCachedItems(ALL);
            thenCacheIsEmpty();
        });
    }

    @AfterAll
    public static void deleteInfinispanCluster() {
        deleteYaml(CLUSTER_CONFIGMAP);
        deleteYaml(CLUSTER_CONFIG);
        adjustYml(CLUSTER_CONFIG, NEW_CLUSTER_NAME, ORIGIN_CLUSTER_NAME);
        adjustYml(CLUSTER_CONFIGMAP, NEW_CLUSTER_NAME, ORIGIN_CLUSTER_NAME);
    }

    private void clearCache() {
        given().get("/items/clear-cache").then().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private void whenAddCacheItems(List<ShopItem> items) {
        items.forEach(item -> given()
                .header("Content-Type", "application/json")
                .body(item).when()
                .post("/items")
                .then().statusCode(HttpStatus.SC_OK));
    }

    private void whenAddCacheItemsWithLifespan(List<ShopItem> items, int lifespan) {
        items.forEach(item -> given()
                .header("Content-Type", "application/json")
                .queryParam("lifespan", lifespan)
                .body(item).when()
                .post("/items")
                .then().statusCode(HttpStatus.SC_OK));
    }

    private void whenAddCacheItemsWithLifespanAndIdleTime(List<ShopItem> items, int lifespan, int idleTime) {
        items.forEach(item -> given()
                .header("Content-Type", "application/json")
                .queryParam("lifespan", lifespan)
                .queryParam("maxIdleTime", idleTime)
                .body(item).when()
                .post("/items")
                .then().statusCode(HttpStatus.SC_OK));
    }

    private void whenQueryCachedItems(String query) {
        if (query != null) {
            response = given().queryParam("query", query).get("/items");
        } else {
            response = given().get("/items");
        }
    }

    private void thenCacheSizeMustBe(Matcher<?> matcher) {
        response.then().body("size()", matcher);
    }

    private void thenCacheBodyMust(Matcher<?> matcher) {
        response.then().body(matcher);
    }

    private void thenCacheIsEmpty() {
        response.then().body("isEmpty()", is(true));
    }

    private static void createInfinispanCluster(io.quarkus.test.bootstrap.Service service) {
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
            Assertions.fail("Fail to wait Infinispan resources to start. Caused by: " + e.getMessage());
        }
    }

    private static void adjustYml(String yamlFile, String originString, String newString) {
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
    private static void applyYaml(String yamlFile) {
        try {
            new Command("oc", "apply", "-f", Paths.get(TARGET_RESOURCES + yamlFile).toString()).runAndWait();
        } catch (Exception e) {
            Assertions.fail("Failed to apply YAML file. Caused by: " + e.getMessage());
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
