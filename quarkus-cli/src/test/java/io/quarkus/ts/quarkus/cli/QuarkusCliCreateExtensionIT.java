package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliDefaultService;
import io.quarkus.test.bootstrap.QuarkusVersionAwareCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.TestQuarkusCli;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public class QuarkusCliCreateExtensionIT {

    @TestQuarkusCli
    public void shouldCreateAndBuildExtension(QuarkusVersionAwareCliClient cliClient) {
        // Create extension
        QuarkusCliDefaultService app = cliClient.createExtension("extension-abc");

        // Should build on Jvm
        QuarkusCliClient.Result result = app.buildOnJvm("-Dinsecure.repositories=WARN");
        assertTrue(result.isSuccessful(), "The extension build failed. Output: " + result.getOutput());
    }
}
