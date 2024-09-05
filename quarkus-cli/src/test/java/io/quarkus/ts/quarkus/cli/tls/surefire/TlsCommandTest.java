package io.quarkus.ts.quarkus.cli.tls.surefire;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashSet;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.tls.TlsConfigurationRegistry;
import io.restassured.RestAssured;
import io.smallrye.config.SmallRyeConfig;

/**
 * This test is only support to run inside QuarkusCliTlsCommandIT.
 */
@QuarkusTest
public class TlsCommandTest {

    public static final String CN = "quarkus-qe-cn";
    public static final String CERT_NAME = "quarkus-qe-cert";
    public static final String PASSWORD = "quarkus-qe-password";
    public static final String TRUST_STORE_PATH = "quarkus-qe-test.trust-store-path";

    @Inject
    TlsConfigurationRegistry registry;

    @Inject
    SmallRyeConfig config;

    @TestHTTPResource(value = "/hello", tls = true)
    URL helloEndpointUrl;

    @Test
    void testKeystoreInDefaultTlsRegistry() throws KeyStoreException {
        var defaultRegistry = registry.getDefault()
                .orElseThrow(() -> new AssertionError("Default TLS Registry is not configured"));
        var ks = defaultRegistry.getKeyStore();
        var ksAliasesSet = new HashSet<String>();
        var ksAliases = ks.aliases();
        while (ksAliases.hasMoreElements()) {
            ksAliasesSet.add(ksAliases.nextElement());
        }
        assertTrue(ksAliasesSet.contains(CERT_NAME));
        assertTrue(ksAliasesSet.contains("issuer-" + CERT_NAME));

        try {
            var key = ks.getKey(CERT_NAME, PASSWORD.toCharArray());
            assertNotNull(key);
            // this is not set in the stone so we don't mind if it changes to something sensible
            // the point here is that we get Key and work with it ...
            assertEquals("RSA", key.getAlgorithm());
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCommunicationUsingGeneratedCerts() {
        try {
            // failure test: make sure that generated truststore is really required
            // so that we know that the truststore generated with Quarkus CLI TLS command works
            RestAssured
                    .given()
                    .get(helloEndpointUrl).then().statusCode(200);
            Assertions.fail("Truststore is not required, therefore we cannot verify generated truststore");
        } catch (Exception ignored) {
            // failure expected
        }

        var truststorePath = config.getValue(TRUST_STORE_PATH, String.class);
        RestAssured
                .given()
                .trustStore(new File(truststorePath), PASSWORD)
                .get(helloEndpointUrl).then().statusCode(200).body(is("Hello from Quarkus REST"));
    }

}
