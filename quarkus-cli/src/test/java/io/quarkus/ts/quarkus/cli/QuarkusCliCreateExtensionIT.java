package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliDefaultService;
import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
@DisabledIfSystemProperty(named = "profile.id", matches = "native", disabledReason = "Only for JVM mode")
public class QuarkusCliCreateExtensionIT {

    @Inject
    static QuarkusCliClient cliClient;

    @Test
    public void shouldCreateAndBuildExtension() {
        // Create extension
        QuarkusCliDefaultService app = cliClient.createExtension("extension-abc");

        // Should build on Jvm
        QuarkusCliClient.Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The extension build failed. Output: " + result.getOutput());
    }
}
