package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@Tag("QUARKUS-6771")
@QuarkusScenario
@ExtendWith(VertxExtension.class)
public class Http2GoAwayIT {

    private static final String RESPONSE_BODY = "OK";
    private static HttpServer goAwayServer;
    private static int serverPort;

    @BeforeAll
    static void startGoAwayServer(Vertx vertx, VertxTestContext testContext) {
        vertx.createHttpServer(new HttpServerOptions().setPort(0))
                .requestHandler(event -> {
                    event.connection().goAway(0, Integer.MAX_VALUE);
                    event.response()
                            .putHeader("content-type", "application/json")
                            .end(RESPONSE_BODY);
                })
                .listen()
                .onSuccess(server -> {
                    goAwayServer = server;
                    serverPort = server.actualPort();
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @AfterAll
    static void stopGoAwayServer() {
        if (goAwayServer != null) {
            goAwayServer.close();
        }
    }

    @Test
    void succeedsAfterGoAway(Vertx vertx, VertxTestContext testContext) {
        vertx.createHttpClient(new HttpClientOptions()
                .setProtocolVersion(HttpVersion.HTTP_2)
                .setHttp2ClearTextUpgrade(false))
                .request(HttpMethod.GET, serverPort, "localhost", "/")
                .compose(httpClientRequest -> httpClientRequest.send().compose(HttpClientResponse::body))
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(RESPONSE_BODY, response.toString());
                    testContext.completeNow();
                }))
                .onFailure(testContext::failNow);

    }
}
