package io.quarkus.ts.cache.caffeine.cache.caffeine;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.cache.caffeine.keys.CollisionTestKey;
import io.restassured.response.Response;

@QuarkusScenario
@Tag("QUARKUS-4541")
public class UniFailureCacheIT {

    public static final String RESOURCE_REACTIVE_FAILURE_API_PATH = "/api/reactive-failure";

    @BeforeEach
    public void setUp() {
        // Reset counters before each test
        given()
                .when()
                .post(RESOURCE_REACTIVE_FAILURE_API_PATH + "/reset")
                .then()
                .statusCode(204);
    }

    @Test
    public void testFailedUniNotCachedWithNoArguments() {
        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/no-args")
                .then()
                .statusCode(500);

        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/no-args")
                .then()
                .statusCode(200)
                .body(equalTo("Success Service ready - no args"));

        // Third call should return the cached success
        Response cachedResponse = given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/no-args")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertEquals("Success Service ready - no args", cachedResponse.getBody().asString(),
                "cachedResponse body is not the expected");

        int callCount = getCallCount("no-args");
        assertEquals(2, callCount, "Method should be called twice (fail + success), third call should use cache");
    }

    @Test
    public void testGlobalFailureCounterNotCached() {
        String path = RESOURCE_REACTIVE_FAILURE_API_PATH + "/failure/key-failure";

        // First call to register the failure
        given()
                .when().get(path)
                .then()
                .statusCode(500);

        // second call should be success
        Response response = given()
                .when().get(path)
                .then()
                .extract()
                .response();

        assertNotEquals(500, response.statusCode(), "The failure has been cached and should not be");
        assertEquals("Success for key: key-failure", response.asString());

    }

    @Test
    public void testFailedUniNotCachedWithSingleArgument() {
        String key = "user-service";

        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/single-arg/" + key)
                .then()
                .statusCode(500);

        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/single-arg/" + key)
                .then()
                .statusCode(200)
                .body(equalTo("Success for key: " + key));

        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/single-arg/" + key)
                .then()
                .statusCode(200)
                .body(equalTo("Success for key: " + key));

        // Different key should also fail
        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/single-arg/different-service")
                .then().statusCode(500);

        int callCount = getCallCount(key);
        assertEquals(2, callCount, "Method should be called twice");
    }

    @Test
    public void testFailedUniNotCachedWithMultipleArguments() {
        given()
                .queryParam("param1", "users")
                .queryParam("param2", "active")
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/multi-args")
                .then()
                .statusCode(500);

        given()
                .queryParam("param1", "users")
                .queryParam("param2", "active")
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/multi-args")
                .then()
                .statusCode(200)
                .body(equalTo("Result for: users:active"));
    }

    @Test
    public void testSizeBasedEviction() {
        String cacheName = "eviction-test-cache";
        String evictionEndpoint = RESOURCE_REACTIVE_FAILURE_API_PATH + "/eviction/";

        // It should be empty before any calls.
        given()
                .when()
                .get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/cache-keys/" + cacheName)
                .then()
                .statusCode(200)
                .body("$", empty());

        given()
                .when()
                .get(evictionEndpoint + "A")
                .then()
                .statusCode(200);
        given()
                .when()
                .get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/cache-keys/" + cacheName)
                .then()
                .statusCode(200)
                .body("$", containsInAnyOrder("A"));

        // cache holds 2 entries, which is its maximum size.
        given()
                .when()
                .get(evictionEndpoint + "B")
                .then()
                .statusCode(200);
        given()
                .when()
                .get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/cache-keys/" + cacheName)
                .then()
                .statusCode(200)
                .body("$", containsInAnyOrder("A", "B"));

        given()
                .when()
                .get(evictionEndpoint + "A")
                .then()
                .statusCode(200);
        // Adding "C" should evict the LRU entry, which is "B".
        given()
                .when()
                .get(evictionEndpoint + "C")
                .then()
                .statusCode(200);

        // cache should now contain "A" and "C". The key "B" has been evicted.
        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/cache-keys/" + cacheName)
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("$", containsInAnyOrder("A", "C"));
    }

    @Test
    public void testFailedUniNotCachedWithCacheKeyAnnotation() {
        String apiKey = "customer-api-key";

        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/cache-key/" + apiKey + "/users")
                .then()
                .statusCode(500);

        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/cache-key/" + apiKey + "/users")
                .then()
                .statusCode(200)
                .body(equalTo("API response for key: " + apiKey));

        // Same API key but different endpoint returns cached value
        // apiKey is used for cache (endpoint is ignored)
        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/cache-key/" + apiKey + "/products")
                .then()
                .statusCode(200)
                .body(equalTo("API response for key: " + apiKey));
    }

    @Test
    public void testNullValuesCached() {
        String id = "empty-resource";

        given()
                .when()
                .get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/null-value/" + id)
                .then()
                .statusCode(204);

        int count1 = getCallCount("null-" + id);
        assertEquals(1, count1);

        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/null-value/" + id)
                .then().statusCode(204);

        // Verify method was NOT called again
        int count2 = getCallCount("null-" + id);
        assertEquals(1, count2, "Null value should be cached");
    }

    @Test
    public void testConcurrentAccessAndRecovery() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        String key = "shared-resource-" + System.currentTimeMillis();
        int threadCount = 5;
        try {
            List<CompletableFuture<Response>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> given()
                        .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/single-arg/" + key)
                        .then().extract().response(),
                        executorService);
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
        int countAfterConcurrentPhase = getCallCount(key);
        assertTrue(countAfterConcurrentPhase <= 2,
                "Cache lock should prevent excessive calls. Expected <= 2, but was " + countAfterConcurrentPhase);

        given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/single-arg/" + key)
                .then()
                .statusCode(200)
                .body(equalTo("Success for key: " + key));

        int callCountAfterRecovery = getCallCount(key);
        assertEquals(2, callCountAfterRecovery,
                "Subsequent calls should use cache, not increment counter");
    }

    @Test
    public void testCacheInvalidationAfterFailure() {
        String key = "config-service";

        given()
                .when()
                .get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/single-arg/" + key)
                .then()
                .statusCode(500);
        given()
                .when()
                .get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/single-arg/" + key)
                .then()
                .statusCode(200);

        int countBefore = getCallCount(key);
        assertEquals(2, countBefore);

        // Let's clear cache
        given()
                .when()
                .post(RESOURCE_REACTIVE_FAILURE_API_PATH + "/invalidate/" + key)
                .then()
                .statusCode(204);

        given()
                .when()
                .get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/single-arg/" + key)
                .then()
                .statusCode(200);

        int countAfter = getCallCount(key);
        assertEquals(3, countAfter, "Method should be called again after invalidation");
    }

    @Test
    public void testCacheKeyHashCodeCollision() {
        CollisionTestKey keyA = new CollisionTestKey("A");
        CollisionTestKey keyB = new CollisionTestKey("B");

        assertEquals(keyA.hashCode(), keyB.hashCode(), "Keys should have the same hash code");

        String valueA = given()
                .when()
                .post(RESOURCE_REACTIVE_FAILURE_API_PATH + "/collision/A")
                .then()
                .statusCode(200)
                .extract().asString();

        String valueB = given()
                .when()
                .post(RESOURCE_REACTIVE_FAILURE_API_PATH + "/collision/B")
                .then()
                .statusCode(200)
                .extract().asString();

        assertNotEquals(valueA, valueB, "Cache should not collide keys with the same hashCode but different equals");

        String cachedValueA = given()
                .when()
                .post(RESOURCE_REACTIVE_FAILURE_API_PATH + "/collision/A")
                .then()
                .statusCode(200)
                .extract().asString();

        assertEquals(valueA, cachedValueA, "The original value for key A should still be cached");

        String cachedValueB = given()
                .when()
                .post(RESOURCE_REACTIVE_FAILURE_API_PATH + "/collision/B")
                .then()
                .statusCode(200)
                .extract().asString();
        assertEquals(valueB, cachedValueB, "The original value for key B should also be cached");
    }

    private int getCallCount(String key) {
        return given()
                .when().get(RESOURCE_REACTIVE_FAILURE_API_PATH + "/call-count/" + key)
                .then()
                .statusCode(200)
                .extract().as(Integer.class);
    }

}
