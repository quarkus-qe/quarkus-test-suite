package io.quarkus.ts.cache.spring;

import static io.quarkus.ts.cache.spring.ServiceWithCacheResource.APPLICATION_SCOPE_SERVICE_PATH;
import static io.quarkus.ts.cache.spring.ServiceWithCacheResource.REQUEST_SCOPE_SERVICE_PATH;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class ServiceWithCacheResourceIT {

    private static final String SERVICE_APPLICATION_SCOPE_PATH = "/services/" + APPLICATION_SCOPE_SERVICE_PATH;
    private static final String SERVICE_REQUEST_SCOPE_PATH = "/services/" + REQUEST_SCOPE_SERVICE_PATH;
    private static final String REST_CONTROLLER_API_PATH = "/api";

    private static final String PREFIX_ONE = "prefix1";
    private static final String PREFIX_TWO = "prefix2";

    /**
     * Check whether the `@Cacheable` annotation works when used in a service.
     */
    @ParameterizedTest
    @ValueSource(strings = { SERVICE_APPLICATION_SCOPE_PATH, SERVICE_REQUEST_SCOPE_PATH, REST_CONTROLLER_API_PATH })
    public void shouldGetTheSameValueAlwaysWhenGettingValueFromPath(String path) {
        // We call the service endpoint
        String value = getFromPath(path);

        // At this point, the cache is populated and we should get the same value from the cache
        assertEquals(value, getFromPath(path), "Value was different which means cache is not working");
    }

    /**
     * Check whether the `@CacheEvict` annotation invalidates the cache when used in a service.
     */
    @ParameterizedTest
    @ValueSource(strings = { SERVICE_APPLICATION_SCOPE_PATH, SERVICE_REQUEST_SCOPE_PATH, REST_CONTROLLER_API_PATH })
    public void shouldGetDifferentValueWhenInvalidateCacheFromPath(String path) {
        // We call the service endpoint
        String value = getFromPath(path);

        // invalidate the cache
        invalidateCacheFromPath(path);

        // Then the value should be different as we have invalidated the cache.
        assertNotEquals(value, getFromPath(path), "Value was equal which means cache invalidate didn't work");
    }

    /**
     * Check whether the `@Cacheable` annotation works when used in a service.
     */
    @ParameterizedTest
    @ValueSource(strings = { SERVICE_APPLICATION_SCOPE_PATH, SERVICE_REQUEST_SCOPE_PATH, REST_CONTROLLER_API_PATH })
    public void shouldGetTheSameValueForSamePrefixesWhenGettingValueFromPath(String path) {
        // We call the service endpoint
        String value = getValueFromPathUsingPrefix(path, PREFIX_ONE);

        // At this point, the cache is populated and we should get the same value from the cache
        assertEquals(value, getValueFromPathUsingPrefix(path, PREFIX_ONE),
                "Value was different which means cache is not working");
        // But different value using another prefix
        assertNotEquals(value, getValueFromPathUsingPrefix(path, PREFIX_TWO),
                "Value was equal which means @CacheKey didn't work");
    }

    /**
     * Check whether the `@CacheEvict` annotation does not invalidate all the caches
     */
    @ParameterizedTest
    @ValueSource(strings = { SERVICE_APPLICATION_SCOPE_PATH, SERVICE_REQUEST_SCOPE_PATH, REST_CONTROLLER_API_PATH })
    public void shouldGetTheSameValuesEvenAfterCallingToCacheInvalidateFromPath(String path) {
        // We call the service endpoints
        String valueOfPrefix1 = getValueFromPathUsingPrefix(path, PREFIX_ONE);
        String valueOfPrefix2 = getValueFromPathUsingPrefix(path, PREFIX_TWO);

        // invalidate the cache: this should not invalidate all the keys
        invalidateCacheFromPath(path);

        // At this point, the cache is populated and we should get the same value for both prefixes
        assertEquals(valueOfPrefix1, getValueFromPathUsingPrefix(path, PREFIX_ONE));
        assertEquals(valueOfPrefix2, getValueFromPathUsingPrefix(path, PREFIX_TWO));
    }

    /**
     * Check whether the `@CacheEvict` annotation work as expected using a key.
     */
    @ParameterizedTest
    @ValueSource(strings = { SERVICE_APPLICATION_SCOPE_PATH, SERVICE_REQUEST_SCOPE_PATH, REST_CONTROLLER_API_PATH })
    public void shouldGetDifferentValueWhenInvalidateCacheOnlyForOnePrefixFromPath(String path) {
        // We call the service endpoints
        String valueOfPrefix1 = getValueFromPathUsingPrefix(path, PREFIX_ONE);
        String valueOfPrefix2 = getValueFromPathUsingPrefix(path, PREFIX_TWO);

        // invalidate the cache: this should not invalidate all the keys
        invalidateCacheWithPrefixFromPath(path, PREFIX_ONE);

        // The cache was invalidated only for prefix1, so the value should be different
        assertNotEquals(valueOfPrefix1, getValueFromPathUsingPrefix(path, PREFIX_ONE));
        // The cache was not invalidated for prefix2, so the value should be the same
        assertEquals(valueOfPrefix2, getValueFromPathUsingPrefix(path, PREFIX_TWO));
    }

    /**
     * Check whether the `@CacheEvict(allEntries=true)` annotation works as expected.
     */
    @ParameterizedTest
    @ValueSource(strings = { SERVICE_APPLICATION_SCOPE_PATH, SERVICE_REQUEST_SCOPE_PATH, REST_CONTROLLER_API_PATH })
    public void shouldGetDifferentValueWhenInvalidateAllTheCacheFromPath(String path) {
        // We call the service endpoints
        String value = getFromPath(path);
        String valueOfPrefix1 = getValueFromPathUsingPrefix(path, PREFIX_ONE);
        String valueOfPrefix2 = getValueFromPathUsingPrefix(path, PREFIX_TWO);

        // invalidate all the cache
        invalidateCacheAllFromPath(path);

        // Then, all the values should be different:
        assertNotEquals(value, getFromPath(path));
        assertNotEquals(valueOfPrefix1, getValueFromPathUsingPrefix(path, PREFIX_ONE));
        assertNotEquals(valueOfPrefix2, getValueFromPathUsingPrefix(path, PREFIX_TWO));
    }

    /**
     * Check whether the `@CachePut` annotation works as expected.
     */
    @ParameterizedTest
    @ValueSource(strings = { SERVICE_APPLICATION_SCOPE_PATH, SERVICE_REQUEST_SCOPE_PATH, REST_CONTROLLER_API_PATH })
    public void shouldPutAnEntryInCache(String path) {
        // Overwrite value
        resetValueInCache(path);

        // Then, the value should be the one from the reset cache endpoint
        assertEquals(BaseServiceWithCache.DEFAULT_CACHE_VALUE, getFromPath(path));
    }

    /**
     * Check whether the `@CachePut` annotation works as expected using the prefix endpoints
     */
    @ParameterizedTest
    @ValueSource(strings = { SERVICE_APPLICATION_SCOPE_PATH, SERVICE_REQUEST_SCOPE_PATH, REST_CONTROLLER_API_PATH })
    public void shouldPutAnEntryUsingPrefixInCache(String path) {
        // Overwrite value
        resetValueInCacheFromPath(path, PREFIX_ONE);

        // Then, the value should be the one from the put value endpoint
        assertEquals(BaseServiceWithCache.DEFAULT_CACHE_VALUE, getValueFromPathUsingPrefix(path, PREFIX_ONE));
    }

    private void resetValueInCacheFromPath(String path, String prefix) {
        resetValueInCache(path + "/using-prefix/" + prefix);
    }

    private void resetValueInCache(String path) {
        postFromPath(path + "/cache/reset");
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
        return given()
                .when().get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }

    private void postFromPath(String path) {
        given().when().post(path);
    }

}
