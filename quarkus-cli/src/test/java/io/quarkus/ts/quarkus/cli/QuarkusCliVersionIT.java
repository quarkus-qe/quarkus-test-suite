package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.builder.Version;
import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;

@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@Disabled("Move to Quarkus 2.7 instead of Quarkus main")
@DisabledOnQuarkusVersion(version = ".*redhat.*", reason = "Do not run CLI version check on productized bits")
@DisabledIfSystemProperty(named = "profile.id", matches = "native", disabledReason = "Only for JVM verification")
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
