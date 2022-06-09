package io.quarkus.ts.quarkus.cli;

import static io.quarkus.ts.quarkus.cli.QuarkusCliUtils.defaultNewExtensionArgsWithFixedStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliDefaultService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;

@QuarkusScenario
@DisabledIfSystemProperty(named = "profile.id", matches = "native", disabledReason = "Only for JVM mode")
// TODO https://github.com/quarkusio/quarkus/pull/25880#issuecomment-1149671224
@DisabledOnQuarkusSnapshot(reason = "quarkus-extension-maven-plugin introduced for Quarkus 2.10, re-enable once 2.10 is out")
public class QuarkusCliCreateExtensionIT {

    @Inject
    static QuarkusCliClient cliClient;

    @Test
    public void shouldCreateAndBuildExtension() {
        // Create extension
        QuarkusCliDefaultService app = cliClient.createExtension("extension-abc", defaultNewExtensionArgsWithFixedStream());

        // Should build on Jvm
        QuarkusCliClient.Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The extension build failed. Output: " + result.getOutput());
    }
}
