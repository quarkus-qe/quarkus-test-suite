package io.quarkus.ts.mcp.app;

import java.nio.file.Path;

public final class Utils {
    private Utils() {
    }

    public static String getFileFolder(Class testClass) {
        return getFileFolder(testClass, "server");
    }

    public static String getFileFolder(Class testClass, String appName) {
        return Path.of("target")
                .resolve(testClass.getSimpleName())
                .resolve(appName)
                .toAbsolutePath().toString();
    }
}
