package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;

@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@EnabledOnNative
public class QuarkusCliCreateNativeApplicationIT {

    @Inject
    static QuarkusCliClient cliClient;

    @Tag("QUARKUS-1071")
    @Tag("QUARKUS-1072")
    @Test
    public void shouldBuildApplicationOnNative() {
        // Create application
        QuarkusCliRestService app = cliClient.createApplication("app");

        // Should build on Native
        QuarkusCliClient.Result result = app.buildOnNative();
        assertTrue(result.isSuccessful(), "The application didn't build on Native. Output: " + result.getOutput());
    }
}
