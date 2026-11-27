package io.quarkus.ts.http.vertx.webclient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.HttpURLConnection;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.client.WireMock;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class VertxWebClientIT {
    static final String EXPECTED_ID = "aBanNLDwR-SAz7iMHuCiyw";
    static final String EXPECTED_VALUE = "Chuck Norris has already been to mars; that why there's no signs of life";
    static final int DELAY = 3500; // must be greater than vertx.webclient.timeout-sec
    private static final String TRACE_PING_PATH = "/trace/ping";
    private static final String TRACE_PING_OPERATION_NAME = "GET " + TRACE_PING_PATH;

    private Response resp;

    @JaegerContainer()
    static final JaegerService jaeger = new JaegerService();

    @Container(image = "${wiremock.image}", port = 8080, expectedLog = "verbose")
    static DefaultService wiremock = new DefaultService();

    @QuarkusApplication
    static RestService vertx = new RestService()
            .withProperty("chucknorris.api.domain", () -> wiremock.getURI(Protocol.HTTP).toString())
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d")
            .withProperty("quarkus.otel.bsp.schedule.delay", "700ms");
    private String testStartTime;

    @BeforeEach
    void setUp() {
        testStartTime = Instant.now().toString();
    }

    @Test
    @DisplayName("Vert.x WebClient [flavor: mutiny] -> Map json response body to POJO")
    public void getChuckJokeAsJSON() {
        setupMockHttpServer();
        Response response = vertx.given().get("/chuck/");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertEquals(EXPECTED_ID, response.body().jsonPath().getString("id"));
        Assertions.assertEquals(EXPECTED_VALUE, response.body().jsonPath().getString("value"));
    }

    @Test
    @DisplayName("Vert.x WebClient [flavor: mutiny] -> Mapped json response by 'as' mutiny method.")
    public void getChuckJokeByJsonBodyCodec() {
        setupMockHttpServer();
        vertx.given()
                .when()
                .get("/chuck/bodyCodec/")
                .then()
                .statusCode(HttpURLConnection.HTTP_OK)
                .body("id", equalToIgnoringCase(EXPECTED_ID))
                .body("value", equalToIgnoringCase(EXPECTED_VALUE));
    }

    @Test
    @DisplayName("Vert.x WebClient [flavor: mutiny] -> If third party server exceed http client timeout, then throw a timeout exception.")
    public void getTimeoutWhenResponseItsTooSlow() {
        wireMockClient().register(get(urlEqualTo("/jokes/random"))
                .willReturn(aResponse()
                        .withHeader("Accept", "application/json")
                        .withFixedDelay(DELAY)));

        vertx.given()
                .when()
                .get("/chuck/bodyCodec/")
                .then()
                .statusCode(HttpURLConnection.HTTP_CLIENT_TIMEOUT);
    }

    @Test
    public void endpointShouldTrace() {
        final int pageLimit = 50;
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            whenIMakePingRequest();
            thenRetrieveTraces(pageLimit, "1h", getServiceName(), TRACE_PING_OPERATION_NAME);
            thenStatusCodeMustBe(HttpStatus.SC_OK);
            thenTraceDataSizeMustBe(greaterThan(0));
            thenTraceSpanSizeMustBe(greaterThan(0));
            thenTraceSpanTagsSizeMustBe(greaterThan(0));
            thenCheckOperationNamesIsEqualTo(TRACE_PING_OPERATION_NAME);
        });
    }

    @Test
    public void httpClientShouldHaveHisOwnSpan() {
        final int pageLimit = 50;
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            whenIMakePingRequest();
            thenRetrieveTraces(pageLimit, "1h", getServiceName(), TRACE_PING_OPERATION_NAME);
            thenStatusCodeMustBe(HttpStatus.SC_OK);
            thenTraceDataSizeMustBe(greaterThan(0));
            thenTraceSpanSizeMustBe(greaterThan(1));
            thenTraceSpanTagsSizeMustBe(greaterThan(0));
            thenCheckOperationNamesIsEqualTo(TRACE_PING_OPERATION_NAME);
        });
    }

    private void whenIMakePingRequest() {
        given().when()
                .get(TRACE_PING_PATH)
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalToIgnoringCase("ping-pong"));
    }

    private void thenRetrieveTraces(int pageLimit, String lookBack, String serviceName, String operationName) {
        resp = given().when()
                .queryParam("limit", pageLimit)
                .queryParam("lookback", lookBack)
                .queryParam("service", serviceName)
                .queryParam("query.service_name", serviceName)
                .queryParam("query.operation_name", operationName)
                .queryParam("query.start_time_min", testStartTime)
                .queryParam("query.start_time_max", Instant.now().toString())
                .get(jaeger.getTraceV3Url());
    }

    private void thenStatusCodeMustBe(int expectedStatusCode) {
        resp.then().statusCode(expectedStatusCode);
    }

    private void thenTraceDataSizeMustBe(Matcher<?> matcher) {
        resp.then().body("result.resourceSpans.size()", matcher);
    }

    private void thenTraceSpanSizeMustBe(Matcher<?> matcher) {
        resp.then().body("result.resourceSpans[0].scopeSpans[0].spans.size()", matcher);
    }

    private void thenTraceSpanTagsSizeMustBe(Matcher<?> matcher) {
        resp.then().body("result.resourceSpans[0].scopeSpans[0].spans.attributes.size()", matcher);
    }

    private void thenCheckOperationNamesIsEqualTo(String expectedOperationName) {
        List<String> operationNames = resp.jsonPath().getList("result.resourceSpans.scopeSpans.spans.name.flatten()");
        assertTrue(operationNames.contains(expectedOperationName),
                expectedOperationName + " is not found in " + operationNames);
    }

    @Test
    @EnabledOnNative
    public void checkNativeDebugSymbols() {
        final String debugSymbolsFileName = "vertx-web-client-1.0.0-SNAPSHOT-runner.debug";
        File debugFile = Paths.get("target", debugSymbolsFileName).toFile();
        assertTrue(debugFile.exists(), "Missing debug symbols file: " + debugSymbolsFileName);
    }

    private void setupMockHttpServer() {
        wireMockClient().register(get(urlEqualTo("/jokes/random"))
                .willReturn(aResponse()
                        .withHeader("Accept", "application/json")
                        .withBody(String.format("{\"categories\":[]," +
                                "\"created_at\":\"2020-01-05 13:42:19.576875\"," +
                                "\"icon_url\":\"https://assets.chucknorris.fake/img/avatar/chuck-norris.png\"," +
                                "\"id\":\"%s\"," +
                                "\"updated_at\":\"2020-01-05 13:42:19.576875\"," +
                                "\"url\":\"https://api.chucknorris.fake/jokes/sC09X1xQQymE4SciIjyV0g\"," +
                                "\"value\":\"%s\"}", EXPECTED_ID, EXPECTED_VALUE))));
    }

    private String getServiceName() {
        return "vertx-web-client";
    }

    private WireMock wireMockClient() {
        return new WireMock(wiremock.getURI(Protocol.HTTP).getHost(), wiremock.getURI(Protocol.HTTP).getPort());
    }
}
