package io.quarkus.ts.security.https.secured;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.https.utils.HttpsAssertions;

@QuarkusScenario
public class AuthzHttpsSecurityIT {
    // not using RestAssured because we want 100% control over certificate & hostname verification

    static final char[] CLIENT_PASSWORD = "client-password".toCharArray();

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService();

    @Test
    public void httpsAuthenticatedAndAuthorizedClient() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType("pkcs12")
                .loadKeyMaterial(new File("target/client-keystore.pkcs12"), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .loadTrustMaterial(new File("target/client-truststore.pkcs12"), CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new DefaultHostnameVerifier())
                .build()) {

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
                .setKeyStoreType("pkcs12")
                .loadKeyMaterial(new File("target/guest-client-keystore.pkcs12"), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .loadTrustMaterial(new File("target/client-truststore.pkcs12"), CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new DefaultHostnameVerifier())
                .build()) {

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
                .setKeyStoreType("pkcs12")
                .loadKeyMaterial(new File("target/client-keystore.pkcs12"), CLIENT_PASSWORD, CLIENT_PASSWORD)
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
                .setKeyStoreType("pkcs12")
                .loadKeyMaterial(new File("target/unknown-client-keystore.pkcs12"), CLIENT_PASSWORD, CLIENT_PASSWORD)
                .loadTrustMaterial(new File("target/client-truststore.pkcs12"), CLIENT_PASSWORD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new DefaultHostnameVerifier())
                .build()) {

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
                .setKeyStoreType("pkcs12")
                .loadKeyMaterial(new File("target/unknown-client-keystore.pkcs12"), CLIENT_PASSWORD, CLIENT_PASSWORD)
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
        assertEquals("Hello <anonymous>, HTTPS: false, isUser: false, isGuest: false", response);
    }

    private String url(Protocol protocol) {
        return app.getHost(protocol) + ":" + app.getPort(protocol) + "/hello/full";
    }

    private String urlWithAuthz() {
        return app.getHost(Protocol.HTTPS) + ":" + app.getPort(Protocol.HTTPS) + "/secured";
    }
}
