package io.quarkus.ts.security.https.redirect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

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
import io.quarkus.ts.security.https.utils.Certificates;
import io.quarkus.ts.security.https.utils.HttpsAssertions;

import javax.net.ssl.SSLContext;

public abstract class BaseRedirectHttpsSecurityIT {
    // not using RestAssured because we want 100% control over certificate & hostname verification

    static final char[] CLIENT_PASSWORD = "client-password".toCharArray();

    @Test
    public void https() throws IOException, GeneralSecurityException {
        SSLContext sslContext = SSLContexts.custom()
                .setKeyStoreType(Certificates.PKCS12)
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
    public void httpsServerCertificateUnknownToClient() throws IOException {
        SSLContext sslContext = SSLContexts.createDefault();
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
    public void httpRedirect() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .disableRedirectHandling()
                .build()) {

            HttpResponse response = Executor.newInstance(httpClient)
                    .execute(Request.Get(url(Protocol.HTTP)))
                    .returnResponse();
            assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, response.getStatusLine().getStatusCode());
            assertEquals(url(Protocol.HTTPS), response.getFirstHeader("Location").getValue());
        }
    }

    protected abstract RestService getApp();

    private String url(Protocol protocol) {
        return getApp().getHost(protocol) + ":" + getApp().getPort(protocol) + "/hello/simple";
    }
}
