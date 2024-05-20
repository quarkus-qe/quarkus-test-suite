package io.quarkus.ts.security.bouncycastle.fips.jsse;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import jakarta.inject.Inject;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.runtime.util.ClassPathUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.KeyStoreOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.ext.web.client.WebClient;

@QuarkusTest
public class BouncyCastleFipsJsseTest {

    private static final String PASSWORD = "password";
    private static final String BCFIPS = BouncyCastleFipsProvider.PROVIDER_NAME;
    private static final String APPLICATION_ENDPOINT = "https://localhost:8444/api/listProviders";
    private static final String BCJSSE = "BCJSSE";
    private static final String KS_TYPE = "BCFKS";

    @Inject
    Vertx vertx;

    @Tag("QUARKUS-2749")
    @Test
    public void verifyBouncyCastleFipsAndJsseProviderAvailability() throws Exception {
        WebClient webClient = WebClient.create(new io.vertx.mutiny.core.Vertx(vertx), createWebClientOptions());
        String body = webClient
                .getAbs(APPLICATION_ENDPOINT)
                .sendAndAwait().bodyAsString();

        String expectedResp = String.join(",", List.of(BCFIPS, BCJSSE));
        assertThat(body, containsString(expectedResp));
    }

    private WebClientOptions createWebClientOptions() throws Exception {
        WebClientOptions webClientOptions = new WebClientOptions();

        byte[] keyStoreData = getFileContent(Paths.get("src", "test", "resources", "client-keystore.jks"));
        KeyStoreOptions keyStoreOptions = new KeyStoreOptions()
                .setPassword(PASSWORD)
                .setValue(Buffer.buffer(keyStoreData))
                .setType(KS_TYPE)
                .setProvider(BCFIPS);
        webClientOptions.setKeyCertOptions(keyStoreOptions);

        byte[] trustStoreData = getFileContent(Paths.get("src", "test", "resources", "client-truststore.jks"));
        KeyStoreOptions trustStoreOptions = new KeyStoreOptions()
                .setPassword(PASSWORD)
                .setValue(Buffer.buffer(trustStoreData))
                .setType(KS_TYPE)
                .setProvider(BCFIPS);
        webClientOptions.setVerifyHost(false).setTrustAll(true).setTrustOptions(trustStoreOptions);

        return webClientOptions;
    }

    private static byte[] getFileContent(Path path) throws IOException {
        byte[] data;
        final InputStream resource = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(ClassPathUtils.toResourceName(path));
        if (resource != null) {
            try (InputStream is = resource) {
                data = doRead(is);
            }
        } else {
            try (InputStream is = Files.newInputStream(path)) {
                data = doRead(is);
            }
        }
        return data;
    }

    private static byte[] doRead(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int r;
        while ((r = is.read(buf)) > 0) {
            out.write(buf, 0, r);
        }
        return out.toByteArray();
    }
}
