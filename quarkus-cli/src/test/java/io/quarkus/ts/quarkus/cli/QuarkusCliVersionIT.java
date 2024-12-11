package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.builder.Version;
import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;

@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnQuarkusVersion(version = ".*redhat.*", reason = "Do not run CLI version check on productized bits")
@DisabledOnNative // Only for JVM verification
@DisabledIfSystemProperty(named = "gh-action-disable-on-win", matches = "true", disabledReason = "We setting `-Dfile.encoding=UTF8` on GH windows action. This causing the output the `picked up java_tool_options` which is not expected in this test.") // TODO remove when We test with JDK 21+
public class QuarkusCliVersionIT {

    @Inject
    static QuarkusCliClient cliClient;

    @Test
    public void shouldVersionMatchQuarkusVersion() {
        // Using option
        assertEquals(Version.getVersion(), cliClient.run("version").getOutput());

        // Using shortcut
        assertEquals(Version.getVersion(), cliClient.run("-v").getOutput());
    }
}
