package io.quarkus.ts.http.advanced;

import static io.quarkus.ts.http.advanced.HelloResource.EVENT_PROPAGATION_WAIT_MS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnQuarkusVersion;
import io.quarkus.ts.http.advanced.clients.HttpVersionClientService;
import io.quarkus.ts.http.advanced.clients.HttpVersionClientServiceAsync;
import io.quarkus.ts.http.advanced.clients.RestClientServiceBuilder;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicateResult;

public abstract class BaseHttpAdvancedIT {

    protected static final String REALM_DEFAULT = "test-realm";
    private static final String ROOT_PATH = "/api";
    private static final int TIMEOUT_SEC = 3;
    private static final int RETRY = 3;
    private static final String PASSWORD = "password";
    private static final String KEY_STORE_PATH = "META-INF/resources/server.keystore";
    private static final int ASSERT_TIMEOUT_SECONDS = 10;
    private static final String SSE_ERROR_MESSAGE = "java.lang.ClassNotFoundException: Provider for jakarta.ws.rs.sse.SseEventSource.Builder cannot be found";

    protected abstract RestService getApp();

    @Test
    @DisplayName("Http/1.1 Server test")
    public void httpServer() {
        getApp().given().get("/api/hello")
                .then().statusLine("HTTP/1.1 200 OK").statusCode(HttpStatus.SC_OK)
                .body("content", is("Hello, World!"));
    }

    @Test
    public void serverHostAddress(TestInfo testInfo) {
        boolean isQuarkusScenario = testInfo.getTestClass().get().getAnnotation(QuarkusScenario.class) != null;
        boolean isOpenShiftScenario = testInfo.getTestClass().get().getAnnotation(OpenShiftScenario.class) != null;

        String responseBody = getApp().given().get("/api/details/server/address")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        if (isQuarkusScenario) {
            assertThat(responseBody, containsString("127.0.0.1")); // used on bare metal testing
            assertThat(responseBody, containsString(":")); // assert IP:PORT format
            int port = Integer.parseInt(responseBody.substring(responseBody.indexOf(':') + 1));
            assertThat(port, greaterThan(1000)); // TS sets custom ports
        } else if (isOpenShiftScenario) {
            assertThat(responseBody, containsString("10.")); // OpenShift uses private network 10.x.x.x
            assertThat(responseBody, containsString(":8080")); // fixed port 8080 is used
        } else {
            fail("Check is not implemented for this kind of scenario");
        }
    }

    @Test
    public void clientHostAddress(TestInfo testInfo) {
        boolean isQuarkusScenario = testInfo.getTestClass().get().getAnnotation(QuarkusScenario.class) != null;
        boolean isOpenShiftScenario = testInfo.getTestClass().get().getAnnotation(OpenShiftScenario.class) != null;
        String responseBody = getApp().given().get("/api/details/client/address")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertThat(responseBody, containsString(":")); // assert IP:PORT format
        int port = Integer.parseInt(responseBody.substring(responseBody.indexOf(':') + 1));
        assertThat(port, greaterThan(32767)); // ephemeral ports - Linux 32768-61000, Windows 49152-65535

        if (isQuarkusScenario) {
            assertThat(responseBody, containsString("127.0.0.1")); // used on bare metal testing
        } else if (isOpenShiftScenario) {
            assertThat(responseBody, containsString("10.")); // OpenShift uses private network 10.x.x.x
        } else {
            fail("Check is not implemented for this kind of scenario");
        }
    }

    @Test
    @DisplayName("GRPC Server test")
    public void testGrpc() {
        getApp().given().when().get("/api/grpc/trinity").then().statusCode(HttpStatus.SC_OK).body(is("Hello trinity"));
    }

    @Test
    @DisplayName("Http/2 Server test")
    public void http2Server() throws InterruptedException, URISyntaxException {
        CountDownLatch done = new CountDownLatch(1);
        Uni<JsonObject> content = getApp().mutiny(defaultVertxHttpClientOptions())
                .getAbs(getAppEndpoint() + "/hello")
                .expect(ResponsePredicate.create(this::isHttp2x))
                .expect(ResponsePredicate.status(Response.Status.OK.getStatusCode())).send()
                .map(HttpResponse::bodyAsJsonObject).ifNoItem().after(Duration.ofSeconds(TIMEOUT_SEC)).fail()
                .onFailure().retry().atMost(RETRY);

        content.subscribe().with(body -> {
            assertEquals(body.getString("content"), "Hello, World!");
            done.countDown();
        });

        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertThat(done.getCount(), equalTo(0L));
    }

    @Test
    @DisplayName("Http/2 Client Sync test")
    @Disabled("blocked by: https://issues.redhat.com/browse/QUARKUS-658")
    public void http2ClientSync() throws Exception {
        HttpVersionClientService versionHttpClient = new RestClientServiceBuilder<HttpVersionClientService>(
                getAppEndpoint()).withHostVerified(true).withPassword(PASSWORD).withKeyStorePath(KEY_STORE_PATH)
                .build(HttpVersionClientService.class);

        Response resp = versionHttpClient.getClientHttpVersion();
        assertEquals(HttpStatus.SC_OK, resp.getStatus());
        assertEquals(HttpVersion.HTTP_2.name(), resp.getHeaderString(HttpClientVersionResource.HTTP_VERSION));
    }

    @Test
    @DisplayName("Http/2 Client Async test")
    @Disabled("blocked by: https://issues.redhat.com/browse/QUARKUS-658")
    public void http2ClientAsync() throws Exception {
        HttpVersionClientServiceAsync clientServiceAsync = new RestClientServiceBuilder<HttpVersionClientServiceAsync>(
                getAppEndpoint()).withHostVerified(true).withPassword(PASSWORD).withKeyStorePath(KEY_STORE_PATH)
                .build(HttpVersionClientServiceAsync.class);

        Response resp = clientServiceAsync.getClientHttpVersion().await().atMost(Duration.ofSeconds(ASSERT_TIMEOUT_SECONDS));

        assertEquals(HttpStatus.SC_OK, resp.getStatus());
        assertEquals(HttpVersion.HTTP_2.name(), resp.getHeaderString(HttpClientVersionResource.HTTP_VERSION));
    }

    @Test
    @DisplayName("Non-application endpoint move to /q/")
    @EnabledOnQuarkusVersion(version = "1\\..*", reason = "Redirection is no longer supported in 2.x")
    public void nonAppRedirections() {
        List<String> endpoints = Arrays.asList("/openapi", "/swagger-ui", "/metrics/base", "/metrics/application",
                "/metrics/vendor", "/metrics", "/health/group", "/health/well", "/health/ready", "/health/live",
                "/health");

        for (String endpoint : endpoints) {
            getApp().given().redirects().follow(false).get(ROOT_PATH + endpoint)
                    .then().statusCode(HttpStatus.SC_MOVED_PERMANENTLY)
                    .and().header("Location", containsString("/q" + endpoint));

            getApp().given().get(ROOT_PATH + endpoint)
                    .then().statusCode(in(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT)));
        }
    }

    @Test
    public void microprofileHttpClientRedirection() {
        io.restassured.response.Response health = getApp().given().get("api/client");
        assertEquals(HttpStatus.SC_OK, health.statusCode());
    }

    @Test
    @EnabledOnQuarkusVersion(version = "1\\..*", reason = "Redirection is no longer supported in 2.x")
    public void vertxHttpClientRedirection() throws InterruptedException, URISyntaxException {
        CountDownLatch done = new CountDownLatch(1);
        Uni<Integer> statusCode = getApp().mutiny(defaultVertxHttpClientOptions())
                .getAbs(getAppEndpoint() + "/health").send()
                .map(HttpResponse::statusCode).ifNoItem()
                .after(Duration.ofSeconds(TIMEOUT_SEC)).fail().onFailure().retry().atMost(RETRY);

        statusCode.subscribe().with(httpStatusCode -> {
            assertEquals(HttpStatus.SC_OK, httpStatusCode);
            done.countDown();
        });

        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertThat(done.getCount(), equalTo(0L));
    }

    @Test
    @Tag("QUARKUS-2004")
    public void constraintsExist() throws JsonProcessingException {
        io.restassured.response.Response response = getApp().given().get("/q/openapi");
        assertEquals(HttpStatus.SC_OK, response.statusCode());

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode body = mapper.readTree(response.body().asString());

        JsonNode validation = body.get("components").get("schemas").get("Hello").get("properties").get("content");

        assertEquals(4, validation.get("maxLength").asInt());
        assertEquals(1, validation.get("minLength").asInt());
        assertEquals("^[A-Za-z]+$", validation.get("pattern").asText());
    }

    @Test
    @Tag("QUARKUS-2785")
    public void keepRequestScopeValuesAfterEventPropagation() {
        final String requestScopeValue = "myValue";
        getApp().given().when().put("/api/hello/local-context/" + requestScopeValue).then().statusCode(204);
        // Please be sure that awaitTime is greater than 'helloResource.EVENT_PROPAGATION_WAIT_MS'
        int awaitTime = EVENT_PROPAGATION_WAIT_MS + EVENT_PROPAGATION_WAIT_MS;
        wait(Duration.ofMillis(awaitTime));
        io.restassured.response.Response resp = getApp().given().when()
                .get("/api/hello/local-context/" + requestScopeValue)
                .then().extract().response();

        assertEquals(200, resp.statusCode(), "RequestScope custom context has been removed with the event propagation");
        Assertions.assertTrue(resp.asString().equalsIgnoreCase(requestScopeValue),
                "Unexpected requestScope custom context value");
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/issues/36402")
    public void sseConnectionTest() {
        String response = getApp().given().get("/api/sse/client").thenReturn().body().asString();

        assertFalse(response.contains(SSE_ERROR_MESSAGE),
                "SSE failed, https://github.com/quarkusio/quarkus/issues/36402 not fixed");
        assertTrue(response.contains("event: test234 test"), "SSE failed, unknown bug. Response: " + response);
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/pull/36664")
    public void interceptedTest() {
        // make server to generate a response so interceptors might intercept it
        // ignore response, we will read interceptors result later
        getApp().given()
                .get(ROOT_PATH + "/intercepted")
                .thenReturn();

        String response = getApp().given()
                .get(ROOT_PATH + "/intercepted/messages")
                .thenReturn().getBody().asString();

        Assertions.assertTrue(response.contains("Unconstrained"), "Unconstrained interceptor should be invoked");
        Assertions.assertTrue(response.contains("Server"), "Server interceptor should be invoked");
        Assertions.assertFalse(response.contains("Client"), "Client interceptor should not be invoked");
    }

    protected Protocol getProtocol() {
        return Protocol.HTTPS;
    }

    private String getAppEndpoint() {
        return getApp().getURI(getProtocol()).withPath(ROOT_PATH).toString();
    }

    private ResponsePredicateResult isHttp2x(HttpResponse<Void> resp) {
        return (resp.version().compareTo(HttpVersion.HTTP_2) == 0) ? ResponsePredicateResult.success()
                : ResponsePredicateResult.failure("Expected HTTP/2");
    }

    private WebClientOptions defaultVertxHttpClientOptions() throws URISyntaxException {
        return new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2).setSsl(true).setVerifyHost(false)
                .setUseAlpn(true)
                .setTrustStoreOptions(new JksOptions().setPassword(PASSWORD).setPath(defaultTruststore()));
    }

    private String defaultTruststore() throws URISyntaxException {
        URL res = getClass().getClassLoader().getResource(KEY_STORE_PATH);
        return Paths.get(res.toURI()).toFile().getAbsolutePath();
    }

    private void wait(Duration timeout) {
        try {
            Thread.sleep(timeout.toMillis());
        } catch (Exception e) {
        }
    }
}
