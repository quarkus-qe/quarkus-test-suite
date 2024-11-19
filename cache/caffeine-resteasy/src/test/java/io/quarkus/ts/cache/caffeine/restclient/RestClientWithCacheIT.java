package io.quarkus.ts.cache.caffeine.restclient;

import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class RestClientWithCacheIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @BeforeEach
    public void resetCounterAndInvalidateCache() {
        invalidateCache("xml");
        invalidateCache("json");
        resetCounter("xml");
        resetCounter("json");
    }

    @Test
    @Tag("QUARKUS-5068")
    public void shouldGetBookFromRestClientXmlWithCache() {
        // Check if request is cached
        sendRequestAndReturnResponseTime("/client/book/xml-cache",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><book><title>Title in Xml with counter equal to 1</title></book>");
        sendRequestAndReturnResponseTime("/client/book/xml-cache",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><book><title>Title in Xml with counter equal to 1</title></book>");

        // Invalidate cache
        invalidateCache("xml");

        // The request shouldn't be cached and counter should be different then previous requests
        sendRequestAndReturnResponseTime("/client/book/xml-cache",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><book><title>Title in Xml with counter equal to 2</title></book>");
    }

    @Test
    @Tag("QUARKUS-5068")
    public void shouldGetBookFromRestClientJsonWithCache() {
        sendRequestAndReturnResponseTime("/client/book/json-cache",
                "{\"title\":\"Title in Json with counter equal to 1\"}");
        sendRequestAndReturnResponseTime("/client/book/json-cache",
                "{\"title\":\"Title in Json with counter equal to 1\"}");

        // Invalidate cache
        invalidateCache("json");

        // The request shouldn't be cached and counter should be different then previous requests
        sendRequestAndReturnResponseTime("/client/book/json-cache",
                "{\"title\":\"Title in Json with counter equal to 2\"}");
    }

    @Test
    public void testExpireAfterWritePropertyWithSpecificName() throws InterruptedException {
        sendRequestAndReturnResponseTime("/client/book/json-cache",
                "{\"title\":\"Title in Json with counter equal to 1\"}");
        sendRequestAndReturnResponseTime("/client/book/json-cache",
                "{\"title\":\"Title in Json with counter equal to 1\"}");

        // Sleep same time as the cache expiration time + adding 100ms as safe buffer
        Thread.sleep(ConfigProvider.getConfig().getValue("cache.expire.json.time", Integer.class) + 100);

        // The request shouldn't be cached and counter should be different then previous requests
        sendRequestAndReturnResponseTime("/client/book/json-cache",
                "{\"title\":\"Title in Json with counter equal to 2\"}");
    }

    @Test
    public void testExpireAfterWritePropertyWith() throws InterruptedException {
        sendRequestAndReturnResponseTime("/client/book/xml-cache",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><book><title>Title in Xml with counter equal to 1</title></book>");
        sendRequestAndReturnResponseTime("/client/book/xml-cache",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><book><title>Title in Xml with counter equal to 1</title></book>");

        // Sleep same time as the cache expiration time + adding 100ms as safe buffer
        Thread.sleep(ConfigProvider.getConfig().getValue("cache.expire.time", Integer.class) + 100);

        // The request shouldn't be cached and counter should be different then previous requests
        sendRequestAndReturnResponseTime("/client/book/xml-cache",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><book><title>Title in Xml with counter equal to 2</title></book>");
    }

    public void sendRequestAndReturnResponseTime(String path, String expectedBody) {
        app.given()
                .get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(expectedBody));
    }

    public void invalidateCache(String cacheName) {
        app.given()
                .get("/client/book/invalidate-" + cacheName)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(cacheName + " cache was invalidated"));
    }

    public void resetCounter(String fileType) {
        app.given()
                .get("/book/reset-counter-" + fileType)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Counter reset"));
    }
}
