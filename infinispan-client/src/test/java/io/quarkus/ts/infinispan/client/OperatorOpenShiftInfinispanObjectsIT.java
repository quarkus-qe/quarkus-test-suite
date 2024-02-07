package io.quarkus.ts.infinispan.client;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.infinispan.client.serialized.ShopItem;
import io.restassured.response.Response;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OperatorOpenShiftInfinispanObjectsIT extends BaseOpenShiftInfinispanIT {

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

    @QuarkusApplication
    static RestService one = new RestService()
            .onPreStart(s -> createInfinispanCluster());

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

    @Test
    @Order(4)
    public void testCacheEvictionByLifespanAndIdleTime() {
        whenAddCacheItemsWithLifespanAndIdleTime(maxThresholdItemList, CACHE_LIFESPAN_SEC + 20, CACHE_IDLE_TIME_SEC);
        await().pollDelay(Duration.ofSeconds(15)).atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            whenQueryCachedItems(ALL);
            thenCacheIsEmpty();
        });
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

}
