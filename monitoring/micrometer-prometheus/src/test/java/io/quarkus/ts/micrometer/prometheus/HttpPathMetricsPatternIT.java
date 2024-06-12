package io.quarkus.ts.micrometer.prometheus;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-4430")
@QuarkusScenario
public class HttpPathMetricsPatternIT {

    private static final int ASSERT_METRICS_TIMEOUT_SECONDS = 30;
    private static final List<String> HTTP_SERVER_REQUESTS_METRICS_SUFFIX = Arrays.asList("count", "sum", "max");
    private static final String HTTP_SERVER_REQUESTS_METRICS_FORMAT = "http_server_requests_seconds_%s{method=\"GET\",outcome=\"%s\",status=\"%d\",uri=\"%s\"}";
    private static final String REDIRECT_ENDPOINT = "/test/redirect";
    private static final String NOT_FOUND_ENDPOINT = "/test/not-found";
    private static final String NOT_FOUND_URI_ENDPOINT = "/test/not-found/{uri}";
    private static final String MOVED_ENDPOINT = "/test/moved/{id}";
    private static final String EMPTY_ENDPOINT = "/test";

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    public void verifyNotFoundInMetrics() {
        whenCallNotFoundEndpoint();
        thenMetricIsExposedInServiceEndpoint(404, "CLIENT_ERROR", "NOT_FOUND");
    }

    @Test
    public void verifyUriNotFoundInMetrics() {
        whenCallNotFoundWithParamEndpoint("/url123");
        thenMetricIsExposedInServiceEndpoint(404, "CLIENT_ERROR", NOT_FOUND_URI_ENDPOINT);
    }

    @Test
    public void verifyRedirectionInMetrtics() {
        whenCallRedirectEndpoint();
        thenMetricIsExposedInServiceEndpoint(302, "REDIRECTION", "REDIRECTION");
    }

    @Test
    public void verifyEmptyPathInMetrics() {
        whenCallEmptyPathEndpoint();
        thenMetricIsExposedInServiceEndpoint(204, "SUCCESS", EMPTY_ENDPOINT);
    }

    @Test
    public void verifyDynamicRedirectionInMetrics() {
        String id = "41";
        whenCallDynamicSegmentEndpoint(id);
        thenMetricIsExposedInServiceEndpoint(301, "REDIRECTION", MOVED_ENDPOINT);
    }

    private void whenCallRedirectEndpoint() {
        given()
                .redirects().follow(false)
                .when().get(REDIRECT_ENDPOINT)
                .then().statusCode(302);
    }

    private void whenCallEmptyPathEndpoint() {
        given()
                .when().get("/test")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private void whenCallNotFoundEndpoint() {
        given()
                .when().get(NOT_FOUND_ENDPOINT)
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private void whenCallNotFoundWithParamEndpoint(String url) {
        given()
                .pathParam("uri", url)
                .redirects().follow(false)
                .when().get(NOT_FOUND_URI_ENDPOINT)
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private void whenCallDynamicSegmentEndpoint(String id) {
        given()
                .pathParam("id", id)
                .redirects().follow(false)
                .when().get(MOVED_ENDPOINT)
                .then().statusCode(HttpStatus.SC_MOVED_PERMANENTLY);
    }

    private void thenMetricIsExposedInServiceEndpoint(int statusCode, String outcome, String uri) {
        await().ignoreExceptions().atMost(ASSERT_METRICS_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAsserted(() -> {
            String metrics = app.given().get("/q/metrics").then()
                    .statusCode(HttpStatus.SC_OK).extract().asString();
            for (String metricSuffix : HTTP_SERVER_REQUESTS_METRICS_SUFFIX) {
                String metric = String.format(HTTP_SERVER_REQUESTS_METRICS_FORMAT, metricSuffix, outcome, statusCode, uri);

                assertTrue(metrics.contains(metric), "Expected metric : " + metric + ". Metrics: " + metrics);
            }
        });
    }

}
