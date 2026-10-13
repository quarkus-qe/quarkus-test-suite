package io.quarkus.ts.security.pqc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.netty.handler.ssl.OpenSsl;
import io.quarkus.test.security.certificate.Certificate;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientBuilder;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyStoreOptionsBase;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientConfig;

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
        WebClientConfig config = new WebClientConfig();
        config.setSsl(true);
        // trustAll + verifyHost conflict in Vert.x 5: hostname verification requires
        // X509ExtendedTrustManager, but the trust-all manager doesn't implement it
        config.setVerifyHost(false);

        ClientSSLOptions sslOptions = new ClientSSLOptions();
        sslOptions.setKeyExchangeGroups(keyExchangeGroup);
        sslOptions.setTrustAll(true);
        var keystoreOpinion = createKeyStoreOpinion(certificate);
        if (keystoreOpinion != null) {
            sslOptions.setKeyCertOptions(keystoreOpinion);
        }

        HttpClientBuilder builder = vertx.httpClientBuilder()
                .with(config)
                .with(sslOptions);
        // When OpenSSLEngineOptions is set on older version of openssl the request fails with "OpenSSL is not available"
        if (isOpenSsl35Available()) {
            builder.with(new OpenSSLEngineOptions());
        }

        return WebClient.wrap(builder.build());
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

    public static boolean isOpenSsl35Available() {
        return OpenSsl.isAvailable() && OpenSsl.version() >= 0x30500000L;
    }
}
