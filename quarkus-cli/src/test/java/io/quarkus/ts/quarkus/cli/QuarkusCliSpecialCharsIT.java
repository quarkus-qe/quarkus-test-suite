package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import javax.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.utils.FileUtils;

@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnQuarkusVersion(version = "1\\..*", reason = "Quarkus CLI has been reworked in 2.x")
public class QuarkusCliSpecialCharsIT {

    static final String FOLDER_WITH_SPACES = "s p a c e s";
    static final String FOLDER_WITH_SPECIAL_CHARS = ",;~!@#$%^&()";
    static final String FOLDER_WITH_DIACRITICS = "ěščřžýáíéůú";
    static final String FOLDER_WITH_JAPANESE = "元気かい";
    static final String FOLDER_WITH_INTERNATIONALIZATION = "Îñţérñåţîöñåļîžåţîờñ";

    static final String ARTIFACT_ID = "app";
    static final String EXPECTED_BUILD_OUTPUT = "BUILD SUCCESS";
    static final String PROFILE_ID = "profile.id";
    static final String NATIVE = "native";
    static final Path TARGET = Path.of("target");

    @Inject
    static QuarkusCliClient cliClient;

    private QuarkusCliClient.Result result;

    @Test
    public void shouldCreateApplicationOnJvmWithSpaces() {
        assertCreateJavaApplicationAtFolder(FOLDER_WITH_SPACES);
    }

    @Test
    public void shouldCreateApplicationOnJvmWithSpecialChars() {
        assertCreateJavaApplicationAtFolder(FOLDER_WITH_SPECIAL_CHARS);
    }

    @Test
    public void shouldCreateApplicationOnJvmWithDiacritics() {
        assertCreateJavaApplicationAtFolder(FOLDER_WITH_DIACRITICS);
    }

    @Test
    public void shouldCreateApplicationOnJvmWithJapanese() {
        assertCreateJavaApplicationAtFolder(FOLDER_WITH_JAPANESE);
    }

    @Test
    public void shouldCreateApplicationOnJvmWithInternationalization() {
        assertCreateJavaApplicationAtFolder(FOLDER_WITH_INTERNATIONALIZATION);
    }

    @Test
    @EnabledIfSystemProperty(named = PROFILE_ID, matches = NATIVE)
    public void shouldCreateApplicationOnNativeWithSpaces() {
        assertCreateNativeApplicationAtFolder(FOLDER_WITH_SPACES);
    }

    @Test
    @EnabledIfSystemProperty(named = PROFILE_ID, matches = NATIVE)
    public void shouldCreateApplicationOnNativeWithSpecialChars() {
        assertCreateNativeApplicationAtFolder(FOLDER_WITH_SPECIAL_CHARS);
    }

    @Test
    @EnabledIfSystemProperty(named = PROFILE_ID, matches = NATIVE)
    public void shouldCreateApplicationOnNativeWithDiacritics() {
        assertCreateNativeApplicationAtFolder(FOLDER_WITH_DIACRITICS);
    }

    @Test
    @EnabledIfSystemProperty(named = PROFILE_ID, matches = NATIVE)
    public void shouldCreateApplicationOnNativeWithJapanese() {
        assertCreateNativeApplicationAtFolder(FOLDER_WITH_JAPANESE);
    }

    @Test
    @EnabledIfSystemProperty(named = PROFILE_ID, matches = NATIVE)
    public void shouldCreateApplicationOnNativeWithInternationalization() {
        assertCreateNativeApplicationAtFolder(FOLDER_WITH_INTERNATIONALIZATION);
    }

    private void assertCreateNativeApplicationAtFolder(String folder) {
        // Should create app in a folder
        whenCreateAppAt(folder);
        thenResultIsSuccessful();

        // Should be able to build the app
        whenBuildOnNativeAppAt(folder);
        thenBuildOutputIsSuccessful();

        // Clean Up
        deleteFolder(folder);
    }

    private void assertCreateJavaApplicationAtFolder(String folder) {
        // Should create app in a folder
        whenCreateAppAt(folder);
        thenResultIsSuccessful();

        // Should be able to build the app
        whenBuildOnJvmAppAt(folder);
        thenBuildOutputIsSuccessful();

        // Clean Up
        deleteFolder(folder);
    }

    private void whenBuildOnNativeAppAt(String folder) {
        result = cliClient.run(TARGET.resolve(folder + "/" + ARTIFACT_ID), "build", "--native");
    }

    private void whenBuildOnJvmAppAt(String folder) {
        result = cliClient.run(TARGET.resolve(folder + "/" + ARTIFACT_ID), "build");
    }

    private void whenCreateAppAt(String folder) {
        result = cliClient.run("create", "app", "--output-directory=" + folder, ARTIFACT_ID);
    }

    private void thenResultIsSuccessful() {
        assertTrue(result.isSuccessful(), "Something failed, output: " + result.getOutput());
    }

    private void thenBuildOutputIsSuccessful() {
        thenResultIsSuccessful();
        assertTrue(result.getOutput().contains(EXPECTED_BUILD_OUTPUT), "Unexpected output content: " + result.getOutput());
    }

    private void deleteFolder(String folder) {
        FileUtils.deletePath(TARGET.resolve(folder));
    }

}
