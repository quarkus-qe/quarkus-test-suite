package io.quarkus.ts.http.advanced;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnQuarkusVersion;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.advanced.clients.HealthClientService;
import io.quarkus.ts.http.advanced.clients.HttpVersionClientService;
import io.quarkus.ts.http.advanced.clients.HttpVersionClientServiceAsync;
import io.quarkus.ts.http.advanced.clients.RestClientServiceBuilder;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicateResult;

@QuarkusScenario
public class HttpAdvancedIT {

    private static final String REALM_DEFAULT = "test-realm";
    private static final String ROOT_PATH = "/api";
    private static final int TIMEOUT_SEC = 3;
    private static final int RETRY = 3;
    private static final String PASSWORD = "password";
    private static final String KEY_STORE_PATH = "META-INF/resources/server.keystore";
    private static final int KEYCLOAK_PORT = 8080;
    private static final int ASSERT_TIMEOUT_SECONDS = 10;

    @Container(image = "quay.io/keycloak/keycloak:14.0.0", expectedLog = "Admin console listening", port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService("/keycloak-realm.json", REALM_DEFAULT);

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService().withProperty("quarkus.oidc.auth-server-url",
            keycloak::getRealmUrl);

    @Test
    @DisplayName("Http/1.1 Server test")
    public void httpServer() {
        app.given().get("/api/hello")
                .then().statusLine("HTTP/1.1 200 OK").statusCode(HttpStatus.SC_OK)
                .body("content", is("Hello, World!"));
    }

    @Test
    @DisplayName("GRPC Server test")
    public void testGrpc() {
        app.given().when().get("/api/grpc/trinity").then().statusCode(HttpStatus.SC_OK).body(is("Hello trinity"));
    }

    @Test
    @DisplayName("Http/2 Server test")
    public void http2Server() throws InterruptedException, URISyntaxException {
        CountDownLatch done = new CountDownLatch(1);
        Uni<JsonObject> content = WebClient.create(Vertx.vertx(), defaultVertxHttpClientOptions())
                .getAbs(getAppEndpoint() + "/hello").expect(ResponsePredicate.create(this::isHttp2x))
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
            app.given().redirects().follow(false).get(ROOT_PATH + endpoint)
                    .then().statusCode(HttpStatus.SC_MOVED_PERMANENTLY)
                    .and().header("Location", containsString("/q" + endpoint));

            app.given().get(ROOT_PATH + endpoint)
                    .then().statusCode(in(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT)));
        }
    }

    @Test
    @Disabled("blocked by: https://issues.redhat.com/browse/QUARKUS-781")
    public void microprofileHttpClientRedirection() throws Exception {
        HealthClientService healthHttpClient = new RestClientServiceBuilder<HealthClientService>(getAppEndpoint())
                .withHostVerified(true).withPassword(PASSWORD).withKeyStorePath(KEY_STORE_PATH)
                .build(HealthClientService.class);

        assertThat(HttpStatus.SC_OK, equalTo(healthHttpClient.health().getStatus()));
    }

    @Test
    @EnabledOnQuarkusVersion(version = "1\\..*", reason = "Redirection is no longer supported in 2.x")
    public void vertxHttpClientRedirection() throws InterruptedException, URISyntaxException {
        CountDownLatch done = new CountDownLatch(1);
        Uni<Integer> statusCode = WebClient.create(Vertx.vertx(), defaultVertxHttpClientOptions())
                .getAbs(getAppEndpoint() + "/health").send().map(HttpResponse::statusCode).ifNoItem()
                .after(Duration.ofSeconds(TIMEOUT_SEC)).fail().onFailure().retry().atMost(RETRY);

        statusCode.subscribe().with(httpStatusCode -> {
            assertEquals(HttpStatus.SC_OK, httpStatusCode);
            done.countDown();
        });

        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertThat(done.getCount(), equalTo(0L));
    }

    protected Protocol getProtocol() {
        return Protocol.HTTPS;
    }

    private String getAppEndpoint() {
        return app.getHost(getProtocol()) + ":" + app.getPort(getProtocol()) + ROOT_PATH;
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
}
