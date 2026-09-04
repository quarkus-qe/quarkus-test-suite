package io.quarkus.ts.security.pqc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.quarkus.test.security.certificate.Certificate;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyStoreOptionsBase;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class BaseVertX {

    public static final String CLIENT_CN = "client";
    public static final String CERT_PASSWORD = "password";

    private static Vertx vertx;

    @BeforeAll
    static void setup() {
        vertx = Vertx.vertx();
    }

    @AfterAll
    static void closeVertx() {
        if (vertx != null) {
            vertx.close();
        }
    }

    public void sendRequest(List<String> keyExchangeGroup, String requestUrl) {
        sendRequest(keyExchangeGroup, requestUrl, null);
    }

    public void sendRequest(List<String> keyExchangeGroup, String requestUrl, Certificate certificate) {
        var client = createWebClient(keyExchangeGroup, certificate);
        try {
            HttpResponse<Buffer> response = client.getAbs(requestUrl)
                    .send().toCompletionStage().toCompletableFuture().join();

            assertEquals(200, response.statusCode());
            assertEquals("Hello from RESTEasy Reactive", response.bodyAsString());
        } finally {
            client.close();
        }
    }

    public void sendRequestAndExpectFailure(List<String> keyExchangeGroup, String requestUrl) {
        sendRequestAndExpectFailure(keyExchangeGroup, requestUrl, null);
    }

    public void sendRequestAndExpectFailure(List<String> keyExchangeGroup, String requestUrl, Certificate certificate) {

        var client = createWebClient(keyExchangeGroup, certificate);

        assertThrows(CompletionException.class, () -> {
            client.getAbs(requestUrl)
                    .send().toCompletionStage().toCompletableFuture().join();
        }, "Server must reject/fail handshake as client have " + keyExchangeGroup.toString()
                + " exchange groups as server not allow them");

        client.close();
    }

    public WebClient createWebClient(List<String> keyExchangeGroup, Certificate certificate) {
        WebClientOptions options = new WebClientOptions();
        options.setSsl(true);
        options.setSslEngineOptions(new OpenSSLEngineOptions());
        options.getSslOptions().setKeyExchangeGroups(keyExchangeGroup);
        options.setTrustAll(true);
        var keystoreOpinion = createKeyStoreOpinion(certificate);
        if (keystoreOpinion != null) {
            options.setKeyCertOptions(keystoreOpinion);
        }

        return WebClient.create(vertx, options);
    }

    public KeyStoreOptionsBase createKeyStoreOpinion(Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        var clientCertificate = certificate.getClientCertificateByCn(CLIENT_CN);
        if (clientCertificate == null) {
            return null;
        }

        if (certificate.format().equals("PKCS12")) {
            return new PfxOptions()
                    .setPath(clientCertificate.keystorePath())
                    .setPassword(CERT_PASSWORD);
        } else if (certificate.format().equals("JKS")) {
            return new JksOptions()
                    .setPath(clientCertificate.keystorePath())
                    .setPassword(CERT_PASSWORD);
        }

        return null;
    }
}
