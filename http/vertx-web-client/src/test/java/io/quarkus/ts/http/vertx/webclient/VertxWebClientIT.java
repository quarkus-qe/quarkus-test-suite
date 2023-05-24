package io.quarkus.ts.http.vertx.webclient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import java.io.File;
import java.net.HttpURLConnection;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.JaegerService;
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

    @JaegerContainer(useOtlpCollector = true, expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @Container(image = "${wiremock.image}", port = 8080, expectedLog = "verbose")
    static DefaultService wiremock = new DefaultService();

    @QuarkusApplication
    static RestService vertx = new RestService()
            .withProperty("chucknorris.api.domain", () -> wiremock.getHost() + ":" + wiremock.getPort())
            .withProperty("quarkus.otel.tracer.exporter.otlp.endpoint", jaeger::getCollectorUrl);

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
    public void getChuckJokeByJsonBodyCodec() throws InterruptedException {
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
            thenTraceSpansOperationNameMustBe(not(empty()));
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
            thenTraceSpansOperationNameMustBe(not(empty()));
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
                .queryParam("operation", operationName)
                .get(jaeger.getTraceUrl());
    }

    private void thenStatusCodeMustBe(int expectedStatusCode) {
        resp.then().statusCode(expectedStatusCode);
    }

    private void thenTraceDataSizeMustBe(Matcher<?> matcher) {
        resp.then().body("data.size()", matcher);
    }

    private void thenTraceSpanSizeMustBe(Matcher<?> matcher) {
        resp.then().body("data[0].spans.size()", matcher);
    }

    private void thenTraceSpanTagsSizeMustBe(Matcher<?> matcher) {
        resp.then().body("data[0].spans[0].tags.size()", matcher);
    }

    private void thenTraceSpansOperationNameMustBe(Matcher<?> matcher) {
        resp.then().body("data.spans.operationName", matcher);
    }

    private void thenCheckOperationNamesIsEqualTo(String expectedOperationName) {
        var body = resp.then().extract().jsonPath();
        IntStream
                .range(0, body.getList("data").size())
                .mapToObj(i -> body.getList("data[" + i + "].spans", Span.class))
                .map(TreeSet::new)
                .forEach(spans -> {
                    var prevSpan = requireNonNull(spans.pollFirst());
                    // assert that operation of the root span element is the expected one
                    Assertions.assertEquals(expectedOperationName, prevSpan.operationName);
                    // assert all other span elements are children (pingRequest endpoint calls pong endpoint and returns result)
                    for (Span span : spans) {
                        Assertions.assertTrue(prevSpan.hasChild(span));
                        prevSpan = span;
                    }
                });
    }

    @Test
    @EnabledOnNative
    public void checkNativeDebugSymbols() {
        final String debugSymbolsFileName = "vertx-web-client-1.0.0-SNAPSHOT-runner.debug";
        File debugFile = Paths.get("target", debugSymbolsFileName).toFile();
        Assertions.assertTrue(debugFile.exists(), "Missing debug symbols file: " + debugSymbolsFileName);
    }

    private void setupMockHttpServer() {
        wireMockClient().register(get(urlEqualTo("/jokes/random"))
                .willReturn(aResponse()
                        .withHeader("Accept", "application/json")
                        .withBody(String.format("{\"categories\":[]," +
                                "\"created_at\":\"2020-01-05 13:42:19.576875\"," +
                                "\"icon_url\":\"https://assets.chucknorris.host/img/avatar/chuck-norris.png\"," +
                                "\"id\":\"%s\"," +
                                "\"updated_at\":\"2020-01-05 13:42:19.576875\"," +
                                "\"url\":\"https://api.chucknorris.io/jokes/sC09X1xQQymE4SciIjyV0g\"," +
                                "\"value\":\"%s\"}", EXPECTED_ID, EXPECTED_VALUE))));
    }

    private String getServiceName() {
        return "vertx-web-client";
    }

    private WireMock wireMockClient() {
        return new WireMock(wiremock.getHost().substring("http://".length()), wiremock.getPort());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class Span implements Comparable<Span> {

        private static final String SPAN_ID = "spanID";

        @JsonProperty
        String operationName;

        @JsonProperty
        String spanID;

        private String parentSpanID;

        @JsonProperty("references")
        void setParentSpanID(List<Map<String, Object>> references) {
            // has reference to a parent element -> is not root element
            if (nonNull(references) && !references.isEmpty() && nonNull(references.get(0))) {
                parentSpanID = (String) references.get(0).get(SPAN_ID);
            }
        }

        @Override
        public int compareTo(Span otherSpan) {
            // leaf node > branch node > root
            if (isRootElement()) {
                return -1;
            }
            if (otherSpan.isRootElement()) {
                return 1;
            } else {
                return hasChild(otherSpan) ? -1 : 1;
            }
        }

        boolean hasChild(Span otherSpan) {
            return spanID.equals(otherSpan.parentSpanID);
        }

        private boolean isRootElement() {
            return isNull(parentSpanID);
        }

    }
}
