package io.quarkus.ts.quarkus.cli;

import static io.quarkus.ts.quarkus.cli.QuarkusCliUtils.defaultWithFixedStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.QuarkusScenario;

@Disabled("https://github.com/quarkusio/quarkus/issues/32219")
@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@EnabledIfSystemProperty(named = "profile.id", matches = "native", disabledReason = "Only for Native verification")
public class QuarkusCliCreateNativeApplicationIT {

    @Inject
    static QuarkusCliClient cliClient;

    @Tag("QUARKUS-1071")
    @Tag("QUARKUS-1072")
    @Test
    public void shouldBuildApplicationOnNative() {
        // Create application
        QuarkusCliRestService app = cliClient.createApplication("app", defaultWithFixedStream());

        // Should build on Native
        QuarkusCliClient.Result result = app.buildOnNative();
        assertTrue(result.isSuccessful(), "The application didn't build on Native. Output: " + result.getOutput());
    }
}
