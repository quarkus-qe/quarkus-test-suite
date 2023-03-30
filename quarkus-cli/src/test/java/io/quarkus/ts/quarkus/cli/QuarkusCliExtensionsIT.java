package io.quarkus.ts.quarkus.cli;

import static io.quarkus.ts.quarkus.cli.QuarkusCliUtils.getCurrentStreamVersion;
import static io.quarkus.ts.quarkus.cli.QuarkusCliUtils.isUpstream;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.builder.Version;
import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.quarkus.model.QuarkusProperties;

/**
 * Note that the extensions list enhancements have been already reported as part
 * of https://github.com/quarkusio/quarkus/issues/18062 and https://github.com/quarkusio/quarkus/issues/18064.
 */
@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledIfSystemProperty(named = "profile.id", matches = "native", disabledReason = "Only for JVM verification")
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
        whenGetListExtensions(getDefaultAppArgs().toArray(new String[0]));
        assertListDefaultOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingName() {
        List<String> args = getDefaultAppArgs();
        args.add("--name");
        whenGetListExtensions(args.toArray(new String[0]));
        assertListNameOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingOrigins() {
        List<String> args = getDefaultAppArgs();
        args.add("--origins");
        whenGetListExtensions(args.toArray(new String[0]));
        assertListOriginsOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingConcise() {
        List<String> args = getDefaultAppArgs();
        args.add("--concise");
        whenGetListExtensions(args.toArray(new String[0]));
        assertListConciseOptionOutput();
    }

    @Test
    public void shouldListExtensionsUsingFull() {
        List<String> args = getDefaultAppArgs();
        args.add("--full");
        whenGetListExtensions(args.toArray(new String[0]));
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
        assertListDefaultOptionOutput();
    }

    @DisabledOnQuarkusSnapshot(reason = "999-SNAPSHOT is not pushed into the platform site")
    @DisabledOnQuarkusVersion(version = "2.7.5.Final", reason = "Quarkus 2.7 stream was removed from code.quarkus.io")
    //TODO Currently code.quarkus and quarkusCli are pointing to the same set of defined streams.
    // ZULIP ref: https://quarkusio.zulipchat.com/#narrow/stream/191168-core-team/topic/streams.20on.20registry.20.2F.20code.2Equarkus/near/280456392
    @Test
    public void shouldListExtensionsUsingStream() {
        String streamVersion = getCurrentStreamVersion();
        whenGetListExtensions("--stream", streamVersion, "--origins");
        if (QuarkusProperties.getVersion().contains("redhat")) {
            assertTrue(result.getOutput().contains("com.redhat.quarkus.platform:quarkus-bom:" + streamVersion));
        } else {
            assertTrue(result.getOutput().contains("io.quarkus.platform:quarkus-bom:" + streamVersion));
        }
    }

    @Test
    public void shouldListExtensionsUsingInstallable() {
        List<String> args = getDefaultAppArgs();
        args.add("--installable");
        whenGetListExtensions(args.toArray(new String[0]));
        assertListDefaultOptionOutput();
    }

    private void whenGetListExtensions(String... extraArgs) {
        List<String> args = new ArrayList<>();
        args.add("extension");
        args.add("list");
        args.addAll(Arrays.asList(extraArgs));

        result = cliClient.run(args.toArray(new String[args.size()]));
        assertResultIsSuccessful();
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

    private void assertResultIsSuccessful() {
        assertTrue(result.isSuccessful(), "Extensions list command didn't work. Output: " + result.getOutput());
    }

    private List<String> getDefaultAppArgs() {
        String version = getCurrentStreamVersion();
        List<String> args = new ArrayList<>();
        if (!isUpstream(version)) {
            args.addAll(Arrays.asList("--stream", version));
        }

        return args;
    }
}
