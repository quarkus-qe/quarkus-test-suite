package io.quarkus.ts.http.advanced;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;

@QuarkusScenario
@Tag("https://github.com/quarkusio/quarkus/issues/24739")
@Disabled("https://github.com/quarkusio/quarkus/issues/37319")
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Netty Native Transport not supported on Windows, see https://quarkus.io/guides/vertx-reference#native-transport")
@DisabledIfSystemProperty(named = "profile.id", matches = "native", disabledReason = "Only for JVM mode, error in native mode - https://github.com/quarkusio/quarkus/issues/25928")
public class DomainSocketIT {

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.http.host-enabled", "false")
            .withProperty("quarkus.http.domain-socket", "/tmp/io.quarkus.app.socket")
            .withProperty("quarkus.http.domain-socket-enabled", "true")
            .withProperty("quarkus.vertx.prefer-native-transport", "true");

    @Test
    public void ensureApplicationStartsWithDomainSocketConfigured() {
        assertTrue(app.isRunning(), "Application should start with domain socket configured");
        app.logs().assertContains("Listening on: unix:/tmp/io.quarkus.app.socket");
    }

    @Test
    public void ensureApplicationProvidesContent() throws InterruptedException {
        Vertx vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
        WebClient client = WebClient.create(vertx, new WebClientOptions().setFollowRedirects(false));
        SocketAddress serverAddress = SocketAddress.domainSocketAddress("/tmp/io.quarkus.app.socket");

        AtomicReference<String> response = new AtomicReference<>();
        client
                .request(
                        HttpMethod.GET,
                        serverAddress,
                        8080,
                        "localhost",
                        "/api/hello")
                .expect(ResponsePredicate.SC_OK)
                .as(BodyCodec.jsonObject())
                .send()
                .onSuccess(res -> response.set(res.body().toString()))
                .onFailure(err -> response.set(err.getMessage()));

        await().atMost(3, TimeUnit.SECONDS).until(() -> response.get() != null);
        assertEquals("{\"content\":\"Hello, World!\"}", response.get(), "Received body is different: " + response.get());
    }
}
