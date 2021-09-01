package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.builder.Version;
import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;

/**
 * Note that the extensions list enhancements have been already reported as part
 * of https://github.com/quarkusio/quarkus/issues/18062 and https://github.com/quarkusio/quarkus/issues/18064.
 */
@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnQuarkusVersion(version = "1\\..*", reason = "Quarkus CLI has been reworked in 2.x")
@DisabledIfSystemProperty(named = "profile.id", matches = "native", disabledReason = "Only for JVM verification")
public class QuarkusCliExtensionsIT {

    static final String AGROAL_EXTENSION_NAME = "Agroal - Database connection pool";
    static final String AGROAL_EXTENSION_ARTIFACT = "quarkus-agroal";
    static final String AGROAL_EXTENSION_GUIDE = "https://quarkus.io/guides/datasource";
    static final List<String> EXPECTED_PLATFORM_VERSIONS = Arrays.asList("2.0.0.Final", "2.1.0.Final");

    @Inject
    static QuarkusCliClient cliClient;

    private QuarkusCliClient.Result result;

    @Test
    public void shouldListExtensionsUsingDefaults() {
        // Current default option behaves as `--origins` which seems wrong to me.
        // Reported by https://github.com/quarkusio/quarkus/issues/18062.
        whenGetListExtensions();
        assertListOriginsOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingName() {
        whenGetListExtensions("--name");
        assertListNameOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingOrigins() {
        whenGetListExtensions("--origins");
        assertListOriginsOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingConcise() {
        whenGetListExtensions("--concise");
        assertListConciseOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingFull() {
        whenGetListExtensions("--full");
        assertListFullOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingOtherPlatformVersions() {
        for (String expectedVersion : EXPECTED_PLATFORM_VERSIONS) {
            whenGetListExtensions("--origins", "--platform-bom=io.quarkus:quarkus-bom:" + expectedVersion);
            assertListOriginsOptionOutput();
        }
    }

    @Test
    public void shouldListExtensionsUsingPlatformBom() {
        whenGetListExtensions("--platform-bom", "io.quarkus:quarkus-bom:" + Version.getVersion());
        assertListOriginsOptionOutput();
    }

    @DisabledOnQuarkusSnapshot(reason = "999-SNAPSHOT is not pushed into the platform site")
    @Test
    public void shouldListExtensionsUsingStream() {
        String streamVersion = getCurrentStreamVersion();
        whenGetListExtensions("--stream", streamVersion);
        assertTrue(result.getOutput().contains("io.quarkus.platform:quarkus-bom::pom:" + streamVersion));
    }

    @Test
    public void shouldListExtensionsUsingInstallable() {
        whenGetListExtensions("--installable");
        assertListOriginsOptionOutput();
    }

    private void whenGetListExtensions(String... extraArgs) {
        List<String> args = new ArrayList<>();
        args.add("extension");
        args.add("list");
        args.addAll(Arrays.asList(extraArgs));

        result = cliClient.run(args.toArray(new String[args.size()]));
        assertResultIsSuccessful();
    }

    private void assertListOriginsOptionOutput() {
        // Origins only shows extension name ++ version. Reported by https://github.com/quarkusio/quarkus/issues/18062.
        assertTrue(result.getOutput().contains(AGROAL_EXTENSION_NAME)
                && !result.getOutput().contains(AGROAL_EXTENSION_ARTIFACT)
                && !result.getOutput().contains(AGROAL_EXTENSION_GUIDE),
                "--origins option output is unexpected. Output: " + result.getOutput());
    }

    private void assertListNameOptionOutput() {
        // Concise shows only the artifact id
        assertTrue(result.getOutput().contains(AGROAL_EXTENSION_ARTIFACT)
                && !result.getOutput().contains(AGROAL_EXTENSION_NAME),
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

    private void assertResultIsSuccessful() {
        assertTrue(result.isSuccessful(), "Extensions list command didn't work. Output: " + result.getOutput());
    }

    private String getCurrentStreamVersion() {
        String[] version = Version.getVersion().split(Pattern.quote("."));
        return String.format("%s.%s", version[0], version[1]);
    }
}
