package io.quarkus.ts.messaging.infinispan.grpc.kafka;

import static io.quarkus.test.services.Certificate.Format.PKCS12;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.quarkus.test.security.certificate.Certificate;

public final class CertUtils {

    static final Path TARGET = Path.of("target");
    static final String KEYSTORE = "keystore.p12";
    static final String TRUSTSTORE = "truststore.p12";
    static final String PASSWORD = "password";

    private CertUtils() {
        // UTIL CLASS
    }

    static String getTruststorePath() {
        // Infinispan does not handle relative path well
        return TARGET.resolve(TRUSTSTORE).toAbsolutePath().toString();
    }

    static void prepareCerts() {
        // generate certs
        var cert = Certificate.of("infinispan-test", PKCS12, PASSWORD);

        // move certs to the target dir because that's where our framework looks for mounted secrets
        move(cert.keystorePath(), KEYSTORE);
        move(cert.truststorePath(), TRUSTSTORE);
    }

    private static void move(String source, String storeName) {
        try {
            Files.move(Path.of(source), TARGET.resolve(storeName), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move '%s' file from a temp dir to the target dir".formatted(source), e);
        }
    }
}
