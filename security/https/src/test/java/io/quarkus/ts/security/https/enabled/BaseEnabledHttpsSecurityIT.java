package io.quarkus.ts.security.https.enabled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.security.https.utils.Certificates;
import io.quarkus.ts.security.https.utils.HttpsAssertions;

public abstract class BaseEnabledHttpsSecurityIT {
    // not using RestAssured because we want 100% control over certificate & hostname verification

    static final char[] CLIENT_PASSWORD = "client-password".toCharArray();

    @Test
    public void https() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType(Certificates.PKCS12)
                .loadKeyMaterial(new File(Certificates.CLIENT_KEYSTORE), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .loadTrustMaterial(new File(Certificates.CLIENT_TRUSTSTORE), CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new DefaultHostnameVerifier())
                .build()) {

            String response = Executor.newInstance(httpClient)
                    .execute(Request.Get(url(Protocol.HTTPS)))
                    .returnContent().asString();
            assertEquals("Hello, use SSL true", response);
        }
    }

    @Test
    public void httpsServerCertificateUnknownToClient() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType(Certificates.PKCS12)
                .loadKeyMaterial(new File(Certificates.CLIENT_KEYSTORE), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new DefaultHostnameVerifier())
                .build()) {

            HttpsAssertions.assertTlsHandshakeError(() -> {
                Executor.newInstance(httpClient).execute(Request.Get(url(Protocol.HTTPS)));
            });
        }
    }

    @Test
    public void httpsClientCertificateUnknownToServer() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType(Certificates.PKCS12)
                .loadKeyMaterial(new File(Certificates.UNKNOWN_CLIENT_KEYSTORE), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .loadTrustMaterial(new File(Certificates.CLIENT_TRUSTSTORE), CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new DefaultHostnameVerifier())
                .build()) {

            HttpsAssertions.assertTlsHandshakeError(() -> {
                Executor.newInstance(httpClient).execute(Request.Get(url(Protocol.HTTPS)));
            });
        }
    }

    @Test
    public void httpsServerCertificateUnknownToClientClientCertificateUnknownToServer()
            throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType(Certificates.PKCS12)
                .loadKeyMaterial(new File(Certificates.UNKNOWN_CLIENT_KEYSTORE), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new DefaultHostnameVerifier())
                .build()) {

            HttpsAssertions.assertTlsHandshakeError(() -> {
                Executor.newInstance(httpClient).execute(Request.Get(url(Protocol.HTTPS)));
            });
        }
    }

    @Test
    public void http() throws IOException {
        String response = Request.Get(url(Protocol.HTTP)).execute().returnContent().asString();
        assertEquals("Hello, use SSL false", response);
    }

    protected abstract RestService getApp();

    private String url(Protocol protocol) {
        return getApp().getHost(protocol) + ":" + getApp().getPort(protocol) + "/hello/simple";
    }
}
