package io.quarkus.ts.security.https.secured;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.ts.security.https.utils.Certificates;
import io.quarkus.ts.security.https.utils.HttpsAssertions;

import javax.net.ssl.SSLContext;

public abstract class BaseAuthzHttpsSecurityIT {
    // not using RestAssured because we want 100% control over certificate & hostname verification

    static final char[] CLIENT_PASSWORD = "client-password".toCharArray();

    @Test
    public void httpsAuthenticatedAndAuthorizedClient() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType(Certificates.PKCS12)
                .loadKeyMaterial(new File(Certificates.CLIENT_KEYSTORE), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .loadTrustMaterial(new File(Certificates.CLIENT_TRUSTSTORE), CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = httpClient(sslContext)) {
            Executor executor = Executor.newInstance(httpClient);

            assertEquals("Hello CN=client, HTTPS: true, isUser: true, isGuest: false",
                    executor.execute(Request.Get(url(Protocol.HTTPS))).returnContent().asString());

            assertEquals("Client certificate: CN=client",
                    executor.execute(Request.Get(urlWithAuthz())).returnContent().asString());
        }
    }

    @Test
    public void httpsAuthenticatedButUnauthorizedClient() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType(Certificates.PKCS12)
                .loadKeyMaterial(new File(Certificates.GUESS_CLIENT_KEYSTORE), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .loadTrustMaterial(new File(Certificates.CLIENT_TRUSTSTORE), CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = httpClient(sslContext)) {
            Executor executor = Executor.newInstance(httpClient);

            assertEquals("Hello CN=guest-client, HTTPS: true, isUser: false, isGuest: true",
                    executor.execute(Request.Get(url(Protocol.HTTPS))).returnContent().asString());

            HttpResponse response = executor.execute(Request.Get(urlWithAuthz())).returnResponse();
            assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void httpsServerCertificateUnknownToClient() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType(Certificates.PKCS12)
                .loadKeyMaterial(new File(Certificates.CLIENT_KEYSTORE), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = httpClient(sslContext)) {
            HttpsAssertions.assertTlsHandshakeError(() -> {
                Executor.newInstance(httpClient).execute(Request.Get(url(Protocol.HTTPS)));
            });
        }
    }

    @Test
    @DisabledOnNative(reason = "Takes too much time to validate this test on Native")
    public void httpsClientCertificateUnknownToServer() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType(Certificates.PKCS12)
                .loadKeyMaterial(new File(Certificates.UNKNOWN_CLIENT_KEYSTORE), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .loadTrustMaterial(new File(Certificates.CLIENT_TRUSTSTORE), CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = httpClient(sslContext)) {
            Executor executor = Executor.newInstance(httpClient);

            HttpsAssertions.assertTls13OnlyHandshakeError(() -> {
                String response = executor.execute(Request.Get(url(Protocol.HTTPS))).returnContent().asString();
                assertEquals("Hello <anonymous>, HTTPS: true, isUser: false, isGuest: false", response);
            });

            HttpsAssertions.assertTls13OnlyHandshakeError(() -> {
                HttpResponse response = executor.execute(Request.Get(urlWithAuthz())).returnResponse();
                assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
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
                .setDefaultRequestConfig(RequestConfig.custom().setExpectContinueEnabled(true).build())
                .build()) {

            HttpsAssertions.assertTlsHandshakeError(() -> {
                Executor.newInstance(httpClient).execute(Request.Get(url(Protocol.HTTPS)));
            });
        }
    }

    @Test
    public void http() throws IOException {
        String response = Request.Get(url(Protocol.HTTP)).execute().returnContent().asString();
        assertEquals("Hello <anonymous>, HTTPS: false, isUser: false, isGuest: false", response);
    }

    protected abstract RestService getApp();

    private CloseableHttpClient httpClient(SSLContext sslContext) {
        return HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new DefaultHostnameVerifier())
                .setDefaultRequestConfig(RequestConfig.custom().setExpectContinueEnabled(true).build())
                .build();
    }

    private String url(Protocol protocol) {
        return getApp().getHost(protocol) + ":" + getApp().getPort(protocol) + "/hello/full";
    }

    private String urlWithAuthz() {
        return getApp().getHost(Protocol.HTTPS) + ":" + getApp().getPort(Protocol.HTTPS) + "/secured";
    }
}
