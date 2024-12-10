package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnNative // Only for JVM verification
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

    @Inject
    static QuarkusCliClient cliClient;

    @Test
    public void shouldCreateApplicationOnJvmWithSpaces() {
        assertCreateJavaApplicationAtFolder(FOLDER_WITH_SPACES);
    }

    @DisabledOnOs(OS.WINDOWS) // TODO: enable me when https://github.com/quarkusio/quarkus/issues/35913 gets fixed
    @Test
    public void shouldCreateApplicationOnJvmWithSpecialChars() {
        assertCreateJavaApplicationAtFolder(FOLDER_WITH_SPECIAL_CHARS);
    }

    @Test
    @DisabledIfSystemProperty(named = "gh-action-disable-on-win", matches = "true", disabledReason = "Some windows don't have all language pack/locales so it causing it fail")
    public void shouldCreateApplicationOnJvmWithDiacritics() {
        assertCreateJavaApplicationAtFolder(FOLDER_WITH_DIACRITICS);
    }

    @Test
    @DisabledIfSystemProperty(named = "gh-action-disable-on-win", matches = "true", disabledReason = "Some windows don't have all language pack/locales so it causing it fail")
    public void shouldCreateApplicationOnJvmWithJapanese() {
        assertCreateJavaApplicationAtFolder(FOLDER_WITH_JAPANESE);
    }

    @Test
    @DisabledIfSystemProperty(named = "gh-action-disable-on-win", matches = "true", disabledReason = "Some windows don't have all language pack/locales so it causing it fail")
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
        var app = cliClient.createApplicationAt(ARTIFACT_ID, folder);

        // Should be able to build the app
        var result = app.buildOnNative();
        thenBuildOutputIsSuccessful(result);
    }

    private void assertCreateJavaApplicationAtFolder(String folder) {
        // Should create app in a folder
        var app = cliClient.createApplicationAt(ARTIFACT_ID, folder);

        // Should be able to build the app
        var result = app.buildOnJvm();
        thenBuildOutputIsSuccessful(result);
    }

    private static void thenBuildOutputIsSuccessful(QuarkusCliClient.Result result) {
        assertTrue(result.getOutput().contains(EXPECTED_BUILD_OUTPUT), "Unexpected output content: " + result.getOutput());
    }
}
