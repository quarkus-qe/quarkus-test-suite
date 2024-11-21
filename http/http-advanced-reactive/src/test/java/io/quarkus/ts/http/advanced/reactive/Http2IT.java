package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.CustomFramesResource.PING_DATA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.URILike;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.JksOptions;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@QuarkusScenario
@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Http2IT {
    @QuarkusApplication(ssl = true, classes = { MorningResource.class,
            CustomFramesResource.class }, properties = "http2.properties", certificates = @Certificate(configureKeystore = true, configureHttpServer = true, useTlsRegistry = false))
    static RestService app = new RestService();

    private static URILike baseUri;
    private static final String BASE_ENDPOINT = "/morning";
    private static final String GREETING = "Buenos dias";

    private static HttpClient httpClient;

    @BeforeEach
    void setUp() {
        baseUri = app.getURI();
    }

    @Test
    @Order(1)
    @DisplayName("HttpClient Vertx compatibility HTTP/1.1 Test")
    public void httpClientVertxHttp1(Vertx vertx, VertxTestContext vertxTestContext) {

        Checkpoint requestCheckpoint = vertxTestContext.checkpoint(2);
        httpClient = vertx.createHttpClient();
        assertNotNull(httpClient);
        vertxTestContext.verify(() -> httpClient.request(HttpMethod.GET, baseUri.getPort(), baseUri.getHost(), BASE_ENDPOINT)
                .compose(req -> req.send()
                        .compose(httpClientResponse -> {
                            assertEquals(HttpStatus.SC_OK, httpClientResponse.statusCode());
                            assertEquals(HttpVersion.HTTP_1_1, httpClientResponse.version());
                            requestCheckpoint.flag();
                            return httpClientResponse.body();
                        }))
                .onSuccess(body -> {
                    assertThat("Body response", body.toString().contains(GREETING));
                    requestCheckpoint.flag();
                }).onFailure(Throwable::printStackTrace));

    }

    @Test
    @Order(2)
    @DisplayName("HttpClient Vertx HTTP/2 protocol test with ssl, alpn, trustAll true and verifyHost false")
    public void httpClientVertxHttp2(Vertx vertx, VertxTestContext vertxTestContext) {
        Checkpoint requestCheckpoint = vertxTestContext.checkpoint(2);
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setSsl(true)
                .setUseAlpn(true)
                .setTrustOptions(new JksOptions().setPath(getTruststorePath()).setPassword("password"))
                .setProtocolVersion(HttpVersion.HTTP_2);
        httpClient = vertx.httpClientBuilder().with(httpClientOptions).build();

        assertNotNull(vertx);
        assertNotNull(httpClient);
        vertxTestContext
                .verify(() -> {
                    httpClient.request(HttpMethod.GET, app.getURI(Protocol.HTTPS).getPort(), baseUri.getHost(), BASE_ENDPOINT)
                            .compose(req -> req.send()
                                    .compose(httpClientResponse -> {
                                        requestCheckpoint.flag();
                                        assertEquals(HttpStatus.SC_OK, httpClientResponse.statusCode());
                                        assertEquals(HttpVersion.HTTP_2, httpClientResponse.version());
                                        return httpClientResponse.body();
                                    }))
                            .onSuccess(body -> {
                                assertThat("Body response", body.toString().contains(GREETING));
                                requestCheckpoint.flag();
                            }).onFailure(throwable -> vertxTestContext.failNow(throwable.getMessage()));
                });
    }

    @Test
    @Order(3)
    @DisplayName("HttpClient Vertx just with HTTP/2 protocol option set and default port")
    void http2ProtocolTest(Vertx vertx, VertxTestContext vertxTestContext) {
        HttpClientOptions httpClientOptions = new HttpClientOptions()
                .setTrustOptions(new JksOptions().setPath(getTruststorePath()).setPassword("password"))
                .setProtocolVersion(HttpVersion.HTTP_2);
        httpClient = vertx.httpClientBuilder().with(httpClientOptions).build();
        httpClient.request(HttpMethod.GET, baseUri.getPort(), baseUri.getHost(), BASE_ENDPOINT)
                .compose(request -> request.send()
                        .compose(httpClientResponse -> {
                            assertEquals(HttpStatus.SC_OK, httpClientResponse.statusCode());
                            assertEquals(HttpVersion.HTTP_2, httpClientResponse.version());
                            return httpClientResponse.body();
                        }))
                .onSuccess(body -> vertxTestContext.verify(() -> {
                    assertThat("Body response", body.toString().contains(GREETING));
                    vertxTestContext.completeNow();
                })).onFailure(Throwable::printStackTrace);

    }

    @Test
    @DisplayName("Test max header size exceeded error message")
    @Order(5)
    void testMaxHeaderSizeExceeded(Vertx vertx, VertxTestContext testContext) {
        HttpClientOptions options = new HttpClientOptions()
                .setSsl(true)
                .setUseAlpn(true)
                .setTrustOptions(new JksOptions().setPath(getTruststorePath()).setPassword("password"))
                .setProtocolVersion(HttpVersion.HTTP_2);
        httpClient = vertx.httpClientBuilder().with(options).build();
        // Define the expected maximum header size limit
        // Note: Consider the configured limit, which is 2048 representing the total size of all headers generated,
        // although it may be considerably lower in practice to do it the test pass getting 'Header size exceeded max allowed size' message.
        int expectedLimitInBytes = 2048;
        String headers = generateHeaders(expectedLimitInBytes);

        httpClient.request(HttpMethod.GET, app.getURI(Protocol.HTTPS).getPort(), baseUri.getHost(), BASE_ENDPOINT)
                .compose(request -> {
                    request.putHeader("Generated-Headers", headers);
                    return request.send();
                })
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        testContext.failNow("Request succeeded unexpectedly");
                    } else {
                        String errorMessage = ar.cause().getMessage();
                        if (errorMessage.contains("Header size exceeded max allowed size")) {
                            testContext.completeNow();
                        } else {
                            testContext.failNow("Unexpected error message: " + errorMessage);
                        }
                    }
                });
    }

    private String generateHeaders(int headerCount) {

        StringBuilder headers = new StringBuilder();
        for (int i = 0; i < headerCount; i++) {
            String headerName = "Header" + i;
            headers.append(headerName);
        }
        return headers.toString();
    }

    @Test
    @Order(4)
    @DisplayName("Verify X-Content-Type-Options Header")
    void verifyXContentTypeOptionsHeader(Vertx vertx, VertxTestContext vertxTestContext) {
        Checkpoint checkpoint = vertxTestContext.checkpoint(2);
        HttpClientOptions options = new HttpClientOptions()
                .setSsl(true)
                .setUseAlpn(true)
                .setTrustOptions(new JksOptions().setPath(getTruststorePath()).setPassword("password"))
                .setProtocolVersion(HttpVersion.HTTP_2);
        httpClient = vertx.httpClientBuilder().with(options).build();
        httpClient.request(HttpMethod.GET, app.getURI(Protocol.HTTPS).getPort(), baseUri.getHost(), BASE_ENDPOINT)
                .compose(request -> request.send()
                        .compose(httpClientResponse -> {
                            assertEquals(HttpStatus.SC_OK, httpClientResponse.statusCode());
                            String contentTypeOptionsHeader = httpClientResponse.getHeader("X-Content-Type-Options");
                            assertNotNull(contentTypeOptionsHeader);
                            assertEquals("nosniff", contentTypeOptionsHeader);
                            assertEquals(HttpVersion.HTTP_2, httpClientResponse.version());
                            checkpoint.flag();
                            return httpClientResponse.body();
                        }))
                .onSuccess(body -> {
                    assertThat("Body response", body.toString().contains(GREETING));
                    checkpoint.flag();
                })
                .onFailure(vertxTestContext::failNow);
    }

    @Test
    @Order(6)
    @DisplayName("Test sending and receiving custom frames over HTTP/2")
    void sendCustomFramesTest(Vertx vertx, VertxTestContext context) {
        Checkpoint checkpoint = context.checkpoint(2);
        CompletableFuture<String> result = new CompletableFuture<>();

        HttpClientOptions options = new HttpClientOptions()
                .setSsl(true)
                .setUseAlpn(true)
                .setProtocolVersion(HttpVersion.HTTP_2)
                .setTrustOptions(new JksOptions().setPath(getTruststorePath()).setPassword("password"))
                .setVerifyHost(false);
        httpClient = vertx.httpClientBuilder().with(options).build();

        httpClient.request(HttpMethod.GET, app.getURI(Protocol.HTTPS).getPort(), baseUri.getHost(), "/ping")
                .onFailure(context::failNow)
                .flatMap(request -> request.send().flatMap(HttpClientResponse::body))
                .onSuccess(buffer -> {
                    String response = buffer.toString();
                    result.complete(response);
                    checkpoint.flag();
                })
                .onFailure(result::completeExceptionally);

        try {
            // Wait for the result or timeout
            String response = result.get(10, TimeUnit.SECONDS);
            assertEquals(PING_DATA, response);
            checkpoint.flag();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            context.failNow(e);
        }
    }

    @AfterEach
    public void closeConnections() {
        httpClient.close();
    }

    private String getTruststorePath() {
        return app
                .<CertificateBuilder> getPropertyFromContext(CertificateBuilder.INSTANCE_KEY)
                .certificates()
                .get(0)
                .truststorePath();
    }
}
