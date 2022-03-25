package io.quarkus.ts.quarkus.cli;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;
import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;

@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnQuarkusVersion(version = "1\\..*", reason = "Quarkus CLI has been reworked in 2.x")
@DisabledIfSystemProperty(named = "profile.id", matches = "native", disabledReason = "Only for JVM verification")
public class QuarkusCliCreateJvmApplicationIT {

    static final String RESTEASY_EXTENSION = "quarkus-resteasy";
    static final String SMALLRYE_HEALTH_EXTENSION = "quarkus-smallrye-health";
    static final String SPRING_WEB_EXTENSION = "quarkus-spring-web";
    static final String RESTEASY_JACKSON_EXTENSION = "quarkus-resteasy-jackson";

    @Inject
    static QuarkusCliClient cliClient;

    @Tag("QUARKUS-1071")
    @Tag("QUARKUS-1072")
    @Tag("QUARKUS-1472")
    @Test
    public void shouldCreateApplicationOnJvm() {
        // Create application
        QuarkusCliRestService app = cliClient.createApplication("app");

        // Should build on Jvm
        QuarkusCliClient.Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());

        // Start using DEV mode
        app.start();
        app.given().get().then().statusCode(HttpStatus.SC_OK);
        assertExpectedJavaVersion(getPomFileFromMavenApplication(app));
    }

    @Tag("QUARKUS-1071")
    @Test
    @DisabledOnQuarkusVersion(version = ".*redhat.*", reason = "Do not run on productized bits - https://issues.redhat.com/browse/QUARKUS-1740")
    public void shouldCreateApplicationWithGradleOnJvm() {

        // Create application
        QuarkusCliRestService app = cliClient.createApplication("app", defaults().withExtraArgs("--gradle"));

        // Should build on Jvm
        QuarkusCliClient.Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());

        // Start using DEV mode
        app.start();
        app.given().get().then().statusCode(HttpStatus.SC_OK);
    }

    @Tag("QUARKUS-1071")
    // TODO https://github.com/quarkusio/quarkus/issues/22964
    @Disabled("There is an issue related to Jbang and maven local repository on github actions")
    @Test
    public void shouldCreateApplicationWithJbangOnJvm() {

        // Create application
        QuarkusCliRestService app = cliClient.createApplication("app", defaults().withExtraArgs("--jbang"));

        // Should build on Jvm
        QuarkusCliClient.Result result = app.buildOnJvm("--verbose");
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());

        // Start using DEV mode
        // https://github.com/quarkusio/quarkus/issues/18157
        // TODO Jbang doesn't support DevMode yet
        //app.start();
        //app.given().get().then().statusCode(HttpStatus.SC_OK);
    }

    @Tag("QUARKUS-1073")
    @Tag("QUARKUS-1070")
    @Test
    public void shouldCreateApplicationOnJvmFromMultipleBoms() {
        // Create application using:
        // 1. Kogito dependencies
        // 2. Prettytime dependencies
        // It will result into several boms added: quarkus-bom and kogito-bom.
        // Also, it verifies that quarkiverse dependencies can be added too.
        final String kogitoExtension = "kogito-quarkus-rules";
        final String prettytimeExtension = "quarkus-prettytime";
        QuarkusCliRestService app = cliClient.createApplication("app", defaults().withExtensions(kogitoExtension,
                prettytimeExtension, RESTEASY_EXTENSION, RESTEASY_JACKSON_EXTENSION));

        // Should build on Jvm
        QuarkusCliClient.Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
        assertInstalledExtensions(app, kogitoExtension, prettytimeExtension, RESTEASY_EXTENSION, RESTEASY_JACKSON_EXTENSION);
    }

    @Tag("QUARKUS-1071")
    @Test
    public void shouldCreateApplicationWithCodeStarter() {
        // Create application with Resteasy Jackson + Spring Web (we need both for the app to run)
        QuarkusCliRestService app = cliClient.createApplication("app",
                defaults().withExtensions(RESTEASY_JACKSON_EXTENSION, SPRING_WEB_EXTENSION));

        // Verify By default, it installs only "quarkus-resteasy-jackson" and "quarkus-spring-web"
        assertInstalledExtensions(app, RESTEASY_JACKSON_EXTENSION, SPRING_WEB_EXTENSION);

        // Start using DEV mode
        app.start();
        untilAsserted(() -> app.given().get("/greeting").then().statusCode(HttpStatus.SC_OK).and().body(is("Hello Spring")));
    }

    @Tag("QUARKUS-1071")
    @Test
    public void shouldAddAndRemoveExtensions() {
        // Create application
        QuarkusCliRestService app = cliClient.createApplication("app");

        // By default, it installs only "quarkus-resteasy"
        assertInstalledExtensions(app, RESTEASY_EXTENSION);

        // Let's install Quarkus Smallrye Health
        QuarkusCliClient.Result result = app.installExtension(SMALLRYE_HEALTH_EXTENSION);
        assertTrue(result.isSuccessful(), SMALLRYE_HEALTH_EXTENSION + " was not installed. Output: " + result.getOutput());

        // Verify both extensions now
        assertInstalledExtensions(app, RESTEASY_EXTENSION, SMALLRYE_HEALTH_EXTENSION);

        // The health endpoint should be now available
        app.start();
        untilAsserted(() -> app.given().get("/q/health").then().statusCode(HttpStatus.SC_OK));
        app.stop();

        // Let's now remove the Smallrye Health extension
        result = app.removeExtension(SMALLRYE_HEALTH_EXTENSION);
        assertTrue(result.isSuccessful(), SMALLRYE_HEALTH_EXTENSION + " was not uninstalled. Output: " + result.getOutput());

        // The health endpoint should be now gone
        app.restart();
        untilAsserted(() -> app.given().get("/q/health").then().statusCode(HttpStatus.SC_NOT_FOUND));
    }

    @Tag("QUARKUS-1255")
    @Test
    public void shouldCreateJacocoReportsFromApplicationOnJvm() {
        QuarkusCliRestService app = cliClient.createApplication("app-with-jacoco", defaults().withExtensions("jacoco"));

        QuarkusCliClient.Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
        assertInstalledExtensions(app, "quarkus-jacoco");

        assertTrue(app.getServiceFolder().resolve("target/jacoco-report/index.html").toFile().exists(),
                "JaCoCo report directory doesn't exist");
        assertTrue(app.getServiceFolder().resolve("target/jacoco-quarkus.exec").toFile().exists(),
                "JaCoCo exec file doesn't exist");
    }

    private void assertInstalledExtensions(QuarkusCliRestService app, String... expectedExtensions) {
        List<String> extensions = app.getInstalledExtensions();
        Stream.of(expectedExtensions).forEach(expectedExtension -> assertTrue(extensions.contains(expectedExtension),
                expectedExtension + " not found in " + extensions));
    }

    private void assertExpectedJavaVersion(File pomFile) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        String javaVersion = getSystemJavaVersion();
        try {
            Model model = reader.read(new FileReader(pomFile));
            Assertions.assertEquals(model.getProperties().get("maven.compiler.release"), javaVersion,
                    "Unexpected Java version. Java support tool should detect host Java version");
        } catch (IOException | XmlPullParserException e) {
            fail(e.getMessage());
        }
    }

    private String getSystemJavaVersion() {
        return StringUtils.substringBefore(System.getProperty("java.version"), ".");
    }

    private File getPomFileFromMavenApplication(QuarkusCliRestService app) {
        return Arrays.stream(Objects.requireNonNull(app.getServiceFolder().toFile().listFiles()))
                .filter(f -> f.getName().equalsIgnoreCase("pom.xml"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Malformed Maven Quarkus application. Missing pom.xml"));
    }
}
