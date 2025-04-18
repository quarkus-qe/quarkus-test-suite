package io.quarkus.ts.websockets.next;

import static io.quarkus.test.services.Certificate.Format.PKCS12;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * All TLS related stuff for websocket is moved to separate child class, since it does not work on OCP
 * TODO: add these tests to OCP once done: https://github.com/quarkus-qe/quarkus-test-framework/issues/1052
 */
@QuarkusScenario
public class TlsWebSocketIT extends BaseWebSocketIT {
    private static final String TRUST_STORE_PASSWORD = "redhat";

    private static final String CERT_PREFIX = "websocket-next-server";

    @QuarkusApplication(ssl = true, certificates = @Certificate(prefix = CERT_PREFIX, format = PKCS12, configureKeystore = true, configureTruststore = true, password = "redhat", tlsConfigName = "tls-server", configureHttpServer = true))
    protected static final RestService server = new RestService()
            .withProperties("tls.properties")
            .withProperty("quarkus.tls.tls-client.trust-store.p12.path", TlsWebSocketIT::getTrustStoreFilename)
            .withProperty("quarkus.tls.tls-client.trust-store.p12.password", TRUST_STORE_PASSWORD);

    @Override
    protected RestService getServer() {
        return server;
    }

    @Test
    public void tlsSecuredTest() throws URISyntaxException, InterruptedException, CertificateException, KeyStoreException,
            IOException, NoSuchAlgorithmException, KeyManagementException {
        URI securedURI = new URI(server.getURI(Protocol.WSS).toString()).resolve("/chat/alice");

        // setup SSL context to trust custom certificate
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new X509TrustManager[] { generateTrustManager() }, null);

        // connect local client
        Client client = new Client(securedURI);
        client.setSocketFactory(sslContext.getSocketFactory());
        client.connectBlocking();

        // connect server-side client
        server.given().queryParam("username", "bob").get("/tlsChatRes/connect");

        try {
            // send message from local client and validate both clients got it
            client.send("Hi");
            assertMessage("alice: Hi", client);
            assertEquals("alice: Hi", server.given().get("/tlsChatRes/getLastMessage").asString());

            // send message from server-side client
            server.given().queryParam("message", "hello").get("/tlsChatRes/sendMessage");
            assertMessage("bob: hello", client);
        } finally {
            // disconnect server-side client
            server.given().get("/tlsChatRes/disconnect");
        }
    }

    private X509TrustManager generateTrustManager()
            throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        InputStream myKeys = new FileInputStream(getTrustStoreFilename());
        KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        myTrustStore.load(myKeys, TRUST_STORE_PASSWORD.toCharArray());
        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(myTrustStore);

        X509TrustManager myTrustManager = null;
        for (TrustManager tm : trustManagerFactory.getTrustManagers()) {
            if (tm instanceof X509TrustManager x509TrustManager) {
                myTrustManager = x509TrustManager;
                break;
            }
        }
        return myTrustManager;
    }

    private static String getTrustStoreFilename() {
        return server.getProperty("quarkus.tls.tls-server.trust-store.p12.path").orElseThrow();
    }
}
