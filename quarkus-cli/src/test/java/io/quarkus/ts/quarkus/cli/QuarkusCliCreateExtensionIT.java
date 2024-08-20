package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliDefaultService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public class QuarkusCliCreateExtensionIT {

    @Inject
    static QuarkusCliClient cliClient;

    @Test
    public void shouldCreateAndBuildExtension() {
        // Create extension
        QuarkusCliDefaultService app = cliClient.createExtension("extension-abc");

        // Should build on Jvm
        QuarkusCliClient.Result result = app.buildOnJvm("-Dinsecure.repositories=WARN");
        assertTrue(result.isSuccessful(), "The extension build failed. Output: " + result.getOutput());
    }
}
