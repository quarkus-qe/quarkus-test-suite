package io.quarkus.ts.quarkus.cli;

import static io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshotCondition.isQuarkusSnapshotVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.builder.Version;
import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;

@DisabledIf(value = "isQuarkusSnapshotRunOnWindows", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/1464")
@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnQuarkusVersion(version = ".*redhat.*", reason = "Do not run CLI version check on productized bits")
@DisabledOnNative // Only for JVM verification
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

    static boolean isQuarkusSnapshotRunOnWindows() {
        return isQuarkusSnapshotVersion() && OS.current() == OS.WINDOWS;
    }
}
