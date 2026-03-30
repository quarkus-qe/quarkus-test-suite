package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliDefaultService;
import io.quarkus.test.bootstrap.QuarkusVersionAwareCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.TestQuarkusCli;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@QuarkusScenario
@DisabledOnNative(reason = "Only for JVM verification")
public class QuarkusCliCreateExtensionIT {

    @TestQuarkusCli
    // TODO enable when Quarkus 3.35 is out
    @Disabled("https://github.com/quarkusio/quarkus/pull/53251 introduces breaking change in main tests compared to Quarkus release in registry")
    public void shouldCreateAndBuildExtension(QuarkusVersionAwareCliClient cliClient) {
        // Create extension
        QuarkusCliDefaultService app = cliClient.createExtension("extension-abc");

        // Should build on Jvm
        QuarkusCliClient.Result result = app.buildOnJvm("-Dinsecure.repositories=WARN");
        assertTrue(result.isSuccessful(), "The extension build failed. Output: " + result.getOutput());
    }
}
