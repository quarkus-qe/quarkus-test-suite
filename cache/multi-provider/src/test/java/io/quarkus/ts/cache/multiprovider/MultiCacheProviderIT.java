package io.quarkus.ts.cache.multiprovider;

import static io.quarkus.ts.cache.multiprovider.resources.CacheServicesWithCacheResource.APPLICATION_SCOPE_SERVICE_PATH;
import static io.quarkus.ts.cache.multiprovider.resources.CacheServicesWithCacheResource.REQUEST_SCOPE_SERVICE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MultiCacheProviderIT {

    private static final String SERVICE_REDIS_PATH = "/services/redis/";
    private static final String SERVICE_CAFFEINE_PATH = "/services/caffeine/";

    private static final String PREFIX_ONE = "prefix1";
    private static final String PREFIX_TWO = "prefix2";

    private static final int REDIS_PORT = 6379;

    // The redis.expire-after-write is set to 10s, so the 15s timeout should be enough
    private static final int REDIS_CACHE_TIMEOUT = 15000;

    @Container(image = "${redis.image}", port = REDIS_PORT, expectedLog = "Ready to accept connections")
    static DefaultService redis = new DefaultService().withProperty("ALLOW_EMPTY_PASSWORD", "YES");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.redis.hosts",
                    () -> {
                        String redisHost = redis.getURI().withScheme("redis").getRestAssuredStyleUri();
                        return String.format("%s:%d", redisHost, redis.getURI().getPort());
                    });

    /**
     * Check whether the `@CacheResult` annotation works when used in a service.
     */
    @ParameterizedTest
    @ValueSource(strings = { APPLICATION_SCOPE_SERVICE_PATH, REQUEST_SCOPE_SERVICE_PATH })
    public void shouldGetTheSameValueAlwaysWhenGettingValueFromPath(String path) {
        String caffeinePath = SERVICE_CAFFEINE_PATH + path;
        String redisPath = SERVICE_REDIS_PATH + path;
        String caffeineValue = getFromPath(caffeinePath);
        String redisValue = getFromPath(redisPath);

        // At this point, the cache is populated, and we should get the same value from the cache
        assertEquals(caffeineValue, getFromPath(caffeinePath), "Value was different which means Caffeine cache is not working");
        assertEquals(redisValue, getFromPath(redisPath), "Value was different which means Redis cache is not working");

        // The Redis should invalidate the cache after 10s, but tha Caffeine should stay same
        Awaitility.await().atMost(REDIS_CACHE_TIMEOUT, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            assertNotEquals(redisValue, getFromPath(redisPath),
                    "Value didn't change after the Redis cache was automatically invalidated");
        });
        assertEquals(caffeineValue, getFromPath(caffeinePath), "Value was different which means Caffeine cache is not working");
    }

    /**
     * Check whether the `@CacheInvalidate` annotation invalidates the cache when used in a service.
     */
    @ParameterizedTest
    @ValueSource(strings = { APPLICATION_SCOPE_SERVICE_PATH, REQUEST_SCOPE_SERVICE_PATH })
    public void shouldGetDifferentValueWhenInvalidateCacheFromPath(String path) {
        String caffeinePath = SERVICE_CAFFEINE_PATH + path;
        String redisPath = SERVICE_REDIS_PATH + path;
        String caffeineValue = getFromPath(caffeinePath);
        String redisValue = getFromPath(redisPath);

        // invalidate the cache
        invalidateCacheFromPath(caffeinePath);

        // The Caffeine cache should be invalidated and the Redis cache should not be affected
        assertEquals(redisValue, getFromPath(redisPath),
                "Value was different which means the invalidate Caffeine cache also invalidated Redis cache");
        assertNotEquals(caffeineValue, getFromPath(caffeinePath),
                "Value was equal which means cache invalidate for Caffeine didn't work");
    }

    /**
     * Check whether the `@CacheResult` and `@CacheKey` annotation works when used in a service.
     */
    @ParameterizedTest
    @ValueSource(strings = { APPLICATION_SCOPE_SERVICE_PATH, REQUEST_SCOPE_SERVICE_PATH })
    public void shouldGetTheSameValueForSamePrefixesWhenGettingValueFromPath(String path) {
        String caffeinePath = SERVICE_CAFFEINE_PATH + path;
        String redisPath = SERVICE_REDIS_PATH + path;
        String caffeineValue = getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE);
        String redisValue = getValueFromPathUsingPrefix(redisPath, PREFIX_ONE);

        // The result should be cached for Redis with prefix one
        assertEquals(redisValue, getValueFromPathUsingPrefix(redisPath, PREFIX_ONE),
                "Value was different which means Redis cache is not working with @CacheKey");
        // But using another prefix the value should not be cached
        assertNotEquals(redisValue, getValueFromPathUsingPrefix(redisPath, PREFIX_TWO),
                "Value was equal which means @CacheKey didn't work for redis");

        // The Redis expiration should still invalidate the value after 10s
        Awaitility.await().atMost(REDIS_CACHE_TIMEOUT, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            assertNotEquals(redisValue, getValueFromPathUsingPrefix(redisPath, PREFIX_ONE),
                    "Value didn't change after the Redis cache was automatically invalidated with @CacheKey");
        });

        // Same for Caffeine cache
        assertEquals(caffeineValue, getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE),
                "Value was different which means Caffeine cache is not working with @CacheKey");
        assertNotEquals(caffeineValue, getValueFromPathUsingPrefix(caffeinePath, PREFIX_TWO),
                "Value was equal which means @CacheKey didn't work for caffeine");
    }

    /**
     * Check whether the `@CacheInvalidate` and `@CacheKey` annotations work as expected on named cache.
     */
    @ParameterizedTest
    @ValueSource(strings = { APPLICATION_SCOPE_SERVICE_PATH, REQUEST_SCOPE_SERVICE_PATH })
    public void shouldGetDifferentValueWhenInvalidateRedisCacheOnlyForOnePrefixFromPath(String path) {
        String caffeinePath = SERVICE_CAFFEINE_PATH + path;
        String redisPath = SERVICE_REDIS_PATH + path;
        String caffeineValueOfPrefix1 = getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE);
        String caffeineValueOfPrefix2 = getValueFromPathUsingPrefix(caffeinePath, PREFIX_TWO);
        String redisValueOfPrefix1 = getValueFromPathUsingPrefix(redisPath, PREFIX_ONE);
        String redisValueOfPrefix2 = getValueFromPathUsingPrefix(redisPath, PREFIX_TWO);

        // invalidate the cache (this should not invalidate all the keys)
        invalidateCacheWithPrefixFromPath(redisPath, PREFIX_ONE);

        // The Redis cache was invalidated only for prefix1, so the value should be different
        assertNotEquals(redisValueOfPrefix1, getValueFromPathUsingPrefix(redisPath, PREFIX_ONE),
                "Value was equal which means Redis cache wasn't invalidated when using @CacheKey");
        // The Redis cache was not invalidated for prefix2, so the value should be the same
        assertEquals(redisValueOfPrefix2, getValueFromPathUsingPrefix(redisPath, PREFIX_TWO),
                "Value was different which means Redis cache was invalidated when using @CacheKey");
        // The Redis cache still should be invalidated after 10s
        Awaitility.await().atMost(REDIS_CACHE_TIMEOUT, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            assertNotEquals(redisValueOfPrefix2, getValueFromPathUsingPrefix(redisPath, PREFIX_TWO),
                    "Value didn't change after the Redis cache was automatically invalidated");
        });

        // The Caffeine cache wasn't invalidated, so the value shouldn't be different
        assertEquals(caffeineValueOfPrefix1, getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE),
                "Value was different which means Caffeine cache was invalidated when using @CacheKey and invalidating Redis cache");
        assertEquals(caffeineValueOfPrefix2, getValueFromPathUsingPrefix(caffeinePath, PREFIX_TWO),
                "Value was different which means Caffeine cache was invalidated when using @CacheKey and invalidating Redis cache");
    }

    /**
     * Check whether the `@CacheInvalidate` and `@CacheKey` annotations work as expected with default cache.
     */
    @ParameterizedTest
    @ValueSource(strings = { APPLICATION_SCOPE_SERVICE_PATH, REQUEST_SCOPE_SERVICE_PATH })
    public void shouldGetDifferentValueWhenInvalidateCaffeineCacheOnlyForOnePrefixFromPath(String path) {
        String caffeinePath = SERVICE_CAFFEINE_PATH + path;
        String redisPath = SERVICE_REDIS_PATH + path;
        String caffeineValueOfPrefix1 = getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE);
        String caffeineValueOfPrefix2 = getValueFromPathUsingPrefix(caffeinePath, PREFIX_TWO);
        String redisValueOfPrefix1 = getValueFromPathUsingPrefix(redisPath, PREFIX_ONE);
        String redisValueOfPrefix2 = getValueFromPathUsingPrefix(redisPath, PREFIX_TWO);

        // invalidate the cache: this should not invalidate all the keys
        invalidateCacheWithPrefixFromPath(caffeinePath, PREFIX_ONE);

        // The Redis cache wasn't invalidated, so the value shouldn't be different
        assertEquals(redisValueOfPrefix1, getValueFromPathUsingPrefix(redisPath, PREFIX_ONE),
                "Value was different which means Redis cache was invalidated when using @CacheKey and invalidating Caffeine cache");
        assertEquals(redisValueOfPrefix2, getValueFromPathUsingPrefix(redisPath, PREFIX_TWO),
                "Value was different which means Redis cache was invalidated when using @CacheKey and invalidating Caffeine cache");

        // The Caffeine cache was invalidated only for prefix1, so the value should be different
        assertNotEquals(caffeineValueOfPrefix1, getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE),
                "Value was equal which means Caffeine cache wasn't invalidated when using @CacheKey");
        // The Caffeine cache was not invalidated for prefix2, so the value should be the same
        assertEquals(caffeineValueOfPrefix2, getValueFromPathUsingPrefix(caffeinePath, PREFIX_TWO),
                "Value was different which means Caffeine cache was invalidated when using @CacheKey");
    }

    /**
     * Check whether the `@CacheInvalidate` annotation does not invalidate all the caches
     */
    @ParameterizedTest
    @ValueSource(strings = { APPLICATION_SCOPE_SERVICE_PATH, REQUEST_SCOPE_SERVICE_PATH })
    public void shouldGetTheSameValuesEvenAfterCallingToCacheInvalidateFromPath(String path) {
        String caffeinePath = SERVICE_CAFFEINE_PATH + path;
        String redisPath = SERVICE_REDIS_PATH + path;
        String caffeineValue = getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE);
        String redisValue = getValueFromPathUsingPrefix(redisPath, PREFIX_ONE);

        // invalidate the cache: this should not invalidate all the keys
        invalidateCacheFromPath(caffeinePath);
        invalidateCacheFromPath(redisPath);

        // At this point, the cache is populated, and we should get the same value for both caches
        assertEquals(redisValue, getValueFromPathUsingPrefix(redisPath, PREFIX_ONE),
                "Redis cache for @CacheKey should not be invalidated");
        assertEquals(caffeineValue, getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE),
                "Caffeine cache for @CacheKey should not be invalidated");
    }

    /**
     * Check whether the `@CacheInvalidateAll` annotation works as expected on named cache.
     */
    @ParameterizedTest
    @ValueSource(strings = { APPLICATION_SCOPE_SERVICE_PATH, REQUEST_SCOPE_SERVICE_PATH })
    public void shouldGetDifferentValueWhenInvalidateAllRedisCacheFromPath(String path) {
        String caffeinePath = SERVICE_CAFFEINE_PATH + path;
        String redisPath = SERVICE_REDIS_PATH + path;
        String caffeineValue = getFromPath(caffeinePath);
        String caffeineValueOfPrefix1 = getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE);
        String caffeineValueOfPrefix2 = getValueFromPathUsingPrefix(caffeinePath, PREFIX_TWO);
        String redisValue = getFromPath(redisPath);
        String redisValueOfPrefix1 = getValueFromPathUsingPrefix(redisPath, PREFIX_ONE);
        String redisValueOfPrefix2 = getValueFromPathUsingPrefix(redisPath, PREFIX_TWO);

        // invalidate all the Redis cache
        invalidateCacheAllFromPath(redisPath);

        // Then, all the Redis values should be different:
        assertNotEquals(redisValue, getFromPath(redisPath),
                "Value was equal, which mean that it wasn't invalidated using @CacheInvalidateAll on Redis cache");
        assertNotEquals(redisValueOfPrefix1, getValueFromPathUsingPrefix(redisPath, PREFIX_ONE),
                "Value was equal, which mean that it wasn't invalidated using @CacheInvalidateAll for Redis cache");
        assertNotEquals(redisValueOfPrefix2, getValueFromPathUsingPrefix(redisPath, PREFIX_TWO),
                "Value was equal, which mean that it wasn't invalidated using @CacheInvalidateAll on Redis cache");
        // and all Caffeine values should be still cached
        assertEquals(caffeineValue, getFromPath(caffeinePath),
                "The Caffeine cache should not be invalidated when using @CacheInvalidateAll on Redis cache");
        assertEquals(caffeineValueOfPrefix1, getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE),
                "The Caffeine cache should not be invalidated when using @CacheInvalidateAll on Redis cache");
        assertEquals(caffeineValueOfPrefix2, getValueFromPathUsingPrefix(caffeinePath, PREFIX_TWO),
                "The Caffeine cache should not be invalidated when using @CacheInvalidateAll on Redis cache");
    }

    /**
     * Check whether the `@CacheInvalidateAll` annotation works as expected.
     */
    @ParameterizedTest
    @ValueSource(strings = { APPLICATION_SCOPE_SERVICE_PATH, REQUEST_SCOPE_SERVICE_PATH })
    public void shouldGetDifferentValueWhenInvalidateAllCaffeineCacheFromPath(String path) {
        String caffeinePath = SERVICE_CAFFEINE_PATH + path;
        String redisPath = SERVICE_REDIS_PATH + path;
        String caffeineValue = getFromPath(caffeinePath);
        String caffeineValueOfPrefix1 = getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE);
        String caffeineValueOfPrefix2 = getValueFromPathUsingPrefix(caffeinePath, PREFIX_TWO);
        String redisValue = getFromPath(redisPath);
        String redisValueOfPrefix1 = getValueFromPathUsingPrefix(redisPath, PREFIX_ONE);
        String redisValueOfPrefix2 = getValueFromPathUsingPrefix(redisPath, PREFIX_TWO);

        // invalidate all the Redis cache
        invalidateCacheAllFromPath(caffeinePath);

        // Then, all the Redis values should be still cached
        assertEquals(redisValue, getFromPath(redisPath),
                "The Redis cache should not be invalidated when using @CacheInvalidateAll on Caffeine cache");
        assertEquals(redisValueOfPrefix1, getValueFromPathUsingPrefix(redisPath, PREFIX_ONE),
                "The Redis cache should not be invalidated when using @CacheInvalidateAll on Caffeine cache");
        assertEquals(redisValueOfPrefix2, getValueFromPathUsingPrefix(redisPath, PREFIX_TWO),
                "The Redis cache should not be invalidated when using @CacheInvalidateAll on Caffeine cache");
        // and all Caffeine values should be different
        assertNotEquals(caffeineValue, getFromPath(caffeinePath),
                "Value was equal, which mean that it wasn't invalidated using @CacheInvalidateAll on Caffeine cache");
        assertNotEquals(caffeineValueOfPrefix1, getValueFromPathUsingPrefix(caffeinePath, PREFIX_ONE),
                "Value was equal, which mean that it wasn't invalidated using @CacheInvalidateAll on Caffeine cache");
        assertNotEquals(caffeineValueOfPrefix2, getValueFromPathUsingPrefix(caffeinePath, PREFIX_TWO),
                "Value was equal, which mean that it wasn't invalidated using @CacheInvalidateAll on Caffeine cache");
    }

    private void invalidateCacheAllFromPath(String path) {
        postFromPath(path + "/invalidate-cache-all");
    }

    private void invalidateCacheWithPrefixFromPath(String path, String prefix) {
        postFromPath(path + "/using-prefix/" + prefix + "/invalidate-cache");
    }

    private void invalidateCacheFromPath(String path) {
        postFromPath(path + "/invalidate-cache");
    }

    private String getValueFromPathUsingPrefix(String path, String prefix) {
        return getFromPath(path + "/using-prefix/" + prefix);
    }

    private String getFromPath(String path) {
        return app.given()
                .when().get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }

    private void postFromPath(String path) {
        app.given()
                .when().post(path)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

}
