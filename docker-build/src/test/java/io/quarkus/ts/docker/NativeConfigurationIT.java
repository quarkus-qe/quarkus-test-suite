package io.quarkus.ts.docker;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;

@QuarkusScenario
@EnabledOnNative
public class NativeConfigurationIT {

    private static final String TARGET_NAME = "target";
    private static final String NATIVE_APP_SOURCES_PARENT_FOLDER_NAME = "docker-build-1.0.0-SNAPSHOT-native-image-source-jar";
    private static final String CUSTOM_SOURCE_FOLDER_NAME = "test";

    @Tag("QUARKUS-1532")
    @Test
    public void verifyNativeSourceFolder() {
        Path sourceFolder = Paths.get(TARGET_NAME, NATIVE_APP_SOURCES_PARENT_FOLDER_NAME, CUSTOM_SOURCE_FOLDER_NAME);
        Assertions.assertTrue(sourceFolder.toFile().exists(), "sources folder must exist on debug mode");
    }
}