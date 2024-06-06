package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.builder.Version;
import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliClient.ListExtensionRequest;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.services.quarkus.model.QuarkusProperties;

/**
 * Note that the extensions list enhancements have been already reported as part
 * of https://github.com/quarkusio/quarkus/issues/18062 and https://github.com/quarkusio/quarkus/issues/18064.
 */
@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public class QuarkusCliExtensionsIT {

    static final String AGROAL_EXTENSION_NAME = "Agroal - Database connection pool";
    static final String AGROAL_EXTENSION_ARTIFACT = "quarkus-agroal";
    static final String QUARKUS_BOM = "quarkus-bom";
    static final String AGROAL_EXTENSION_GUIDE = "https://quarkus.io/guides/datasource";
    static final List<String> EXPECTED_PLATFORM_VERSIONS = Arrays.asList("2.0.0.Final", "2.1.0.Final");

    @Inject
    static QuarkusCliClient cliClient;

    private QuarkusCliClient.Result result;

    @Test
    public void shouldListExtensionsUsingDefaults() {
        result = cliClient.listExtensions();
        assertListDefaultOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingName() {
        result = cliClient.listExtensions("--name");
        assertListNameOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingOrigins() {
        result = cliClient.listExtensions("--origins");
        assertListOriginsOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingConcise() {
        result = cliClient.listExtensions("--concise");
        assertListConciseOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingFull() {
        result = cliClient.listExtensions("--full");
        assertListFullOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingOtherPlatformVersions() {
        for (String expectedVersion : EXPECTED_PLATFORM_VERSIONS) {
            result = cliClient.listExtensions("--origins", "--platform-bom=io.quarkus:quarkus-bom:" + expectedVersion);
            assertListOriginsOptionOutput();
        }
    }

    @Test
    public void shouldListExtensionsUsingPlatformBom() {
        result = cliClient.listExtensions("--platform-bom", "io.quarkus:quarkus-bom:" + Version.getVersion());
        assertListDefaultOptionOutput();
    }

    @DisabledOnQuarkusSnapshot(reason = "999-SNAPSHOT is not pushed into the platform site")
    @Test
    public void shouldListExtensionsUsingStream() {
        var req = ListExtensionRequest.withSetStream();
        result = cliClient.listExtensions(req, "--origins");
        if (QuarkusProperties.getVersion().contains("redhat")) {
            assertTrue(result.getOutput().contains("com.redhat.quarkus.platform:quarkus-bom:" + req.stream()));
        } else {
            assertTrue(result.getOutput().contains("io.quarkus.platform:quarkus-bom:" + req.stream()));
        }
    }

    @Test
    public void shouldListExtensionsUsingInstallable() {
        result = cliClient.listExtensions("--installable");
        assertListDefaultOptionOutput();
    }

    private void assertListDefaultOptionOutput() {
        assertTrue(result.getOutput().contains(AGROAL_EXTENSION_NAME)
                && result.getOutput().contains(AGROAL_EXTENSION_ARTIFACT)
                && !result.getOutput().contains(AGROAL_EXTENSION_GUIDE),
                "Default output is unexpected. Output: " + result.getOutput());
    }

    private void assertListOriginsOptionOutput() {
        assertTrue(result.getOutput().contains(QUARKUS_BOM)
                && result.getOutput().contains(AGROAL_EXTENSION_NAME)
                && result.getOutput().contains(AGROAL_EXTENSION_ARTIFACT)
                && !result.getOutput().contains(AGROAL_EXTENSION_GUIDE),
                "--origins option output is unexpected. Output: " + result.getOutput());
    }

    private void assertListNameOptionOutput() {
        // Concise shows only the artifact id
        assertTrue(result.getOutput().contains(AGROAL_EXTENSION_ARTIFACT)
                && result.getOutput().contains(AGROAL_EXTENSION_NAME),
                "--name option output is unexpected. Output: " + result.getOutput());
    }

    private void assertListConciseOptionOutput() {
        // Concise shows extension name ++ artifact id
        assertTrue(result.getOutput().contains(AGROAL_EXTENSION_NAME)
                && result.getOutput().contains(AGROAL_EXTENSION_ARTIFACT)
                && !result.getOutput().contains(AGROAL_EXTENSION_GUIDE),
                "--concise option output is unexpected. Output: " + result.getOutput());
    }

    private void assertListFullOptionOutput() {
        // Full should show also the origins. Reported by https://github.com/quarkusio/quarkus/issues/18062.
        assertTrue(result.getOutput().contains(AGROAL_EXTENSION_NAME)
                && result.getOutput().contains(AGROAL_EXTENSION_ARTIFACT)
                && result.getOutput().contains(AGROAL_EXTENSION_GUIDE),
                "--full option output is unexpected. Output: " + result.getOutput());
    }

}
