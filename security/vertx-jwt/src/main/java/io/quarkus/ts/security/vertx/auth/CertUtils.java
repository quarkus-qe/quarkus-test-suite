package io.quarkus.ts.security.vertx.auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CertUtils {

    private CertUtils() {
        // UTIL CLASS
    }

    public static String loadKey(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract key from path %s".formatted(path), e);
        }
    }
}
