package io.quarkus.ts.quarkus.cli;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest;
import static io.quarkus.test.bootstrap.QuarkusCliClient.Result;
import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;
import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static io.quarkus.ts.quarkus.cli.QuarkusCliUtils.defaultWithFixedStream;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.awaitility.core.ConditionTimeoutException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.quarkus.model.QuarkusProperties;

@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public class QuarkusCliCreateJvmApplicationIT {

    static final String RESTEASY_REACTIVE_EXTENSION = "quarkus-resteasy-reactive";
    static final String SMALLRYE_HEALTH_EXTENSION = "quarkus-smallrye-health";
    static final String SPRING_WEB_EXTENSION = "quarkus-spring-web";
    static final String RESTEASY_REACTIVE_JACKSON_EXTENSION = "quarkus-resteasy-reactive-jackson";
    static final String ROOT_FOLDER = "";
    static final String DOCKER_FOLDER = "/src/main/docker";
    static final String JDK_11 = "11";
    static final String JDK_17 = "17";
    static final String JDK_18 = "18";
    static final String DOCKERFILE_JVM = "Dockerfile.jvm";

    @Inject
    static QuarkusCliClient cliClient;

    @Tag("QUARKUS-1071")
    @Tag("QUARKUS-1072")
    @Test
    public void shouldCreateApplicationOnJvm() {
        // Create application
        QuarkusCliRestService app = cliClient.createApplication("app", defaultWithFixedStream());

        // Should build on Jvm
        Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());

        // Start using DEV mode
        app.start();
        app.given().get().then().statusCode(HttpStatus.SC_OK);
    }

    @Tag("QUARKUS-1472")
    @Test
    public void createAppShouldAutoDetectJavaVersion() {
        QuarkusCliRestService app = cliClient.createApplication("app", defaultWithFixedStream());
        assertExpectedJavaVersion(getFileFromApplication(app, ROOT_FOLDER, "pom.xml"), getSystemJavaVersion());
        assertDockerJavaVersion(getFileFromApplication(app, DOCKER_FOLDER, DOCKERFILE_JVM), getSystemJavaVersion());
    }

    @Tag("QUARKUS-1472")
    @Test
    public void shouldCreateAnApplicationForcingJavaVersion11() {
        CreateApplicationRequest args = defaultWithFixedStream().withExtraArgs("--java=" + JDK_11);
        QuarkusCliRestService app = cliClient.createApplication("app", args);
        assertExpectedJavaVersion(getFileFromApplication(app, ROOT_FOLDER, "pom.xml"), JDK_11);
        assertDockerJavaVersion(getFileFromApplication(app, DOCKER_FOLDER, DOCKERFILE_JVM), JDK_11);
    }

    @Tag("QUARKUS-1472")
    @Test
    public void shouldCreateAnApplicationForcingJavaVersion17() {
        CreateApplicationRequest args = defaultWithFixedStream().withExtraArgs("--java=" + JDK_17);
        QuarkusCliRestService app = cliClient.createApplication("app", args);
        assertExpectedJavaVersion(getFileFromApplication(app, ROOT_FOLDER, "pom.xml"), JDK_17);
        assertDockerJavaVersion(getFileFromApplication(app, DOCKER_FOLDER, DOCKERFILE_JVM), JDK_17);
    }

    @Test
    public void quarkusCreatedWithJava18ShouldUseJava17() {
        CreateApplicationRequest args = defaultWithFixedStream().withExtraArgs("--java=" + JDK_18);
        QuarkusCliRestService app = cliClient.createApplication("app", args);
        assertExpectedJavaVersion(getFileFromApplication(app, ROOT_FOLDER, "pom.xml"), JDK_17);
        assertDockerJavaVersion(getFileFromApplication(app, DOCKER_FOLDER, DOCKERFILE_JVM), JDK_17);
    }

    @Disabled("https://issues.redhat.com/browse/QUARKUS-3439")
    @Tag("QUARKUS-1071")
    @Test
    public void shouldCreateApplicationWithGradleOnJvm() {

        // Create application
        QuarkusCliRestService app = cliClient.createApplication("app", defaultWithFixedStream().withExtraArgs("--gradle"));

        // Run Gradle Daemon to avoid file lock on quarkus-cli-command.out when the daemon is started as part of 'app.buildOnJvm()'
        runGradleDaemon(app);
        // Should build on Jvm
        final String repository = System.getProperty("maven.repo.local");
        final Result result;
        if (repository == null) {
            result = app.buildOnJvm();
        } else {
            app.withProperty("maven.repo.local", repository);
            result = app.buildOnJvm("-Dmaven.repo.local=" + repository);
        }

        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());

        // Start using DEV mode
        app.start();
        app.given().get().then().statusCode(HttpStatus.SC_OK);

        // Stop Gradle Daemon to save resources
        stopGradleDaemon(app);
    }

    private void runGradleDaemon(QuarkusCliRestService app) {
        runGradleWrapper(app, "--daemon");
    }

    private void stopGradleDaemon(QuarkusCliRestService app) {
        runGradleWrapper(app, "--stop");
    }

    private static void runGradleWrapper(QuarkusCliRestService app, String command) {
        Path workingDirectory = app.getServiceFolder();
        ProcessBuilder builder = new ProcessBuilder();
        if (OS.current() == OS.WINDOWS) {
            builder.command("cmd.exe", "/c", "gradlew", command);
        } else {
            builder.command("sh", "-c", "./gradlew", command);
        }
        try {
            Process process = builder.redirectErrorStream(true)
                    .directory(workingDirectory.toFile())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Disabled("https://issues.redhat.com/browse/QUARKUS-3371")
    @Tag("QUARKUS-1071")
    @Test
    public void shouldCreateApplicationWithJbangOnJvm() {

        // Create application
        QuarkusCliRestService app = cliClient.createApplication("app", defaultWithFixedStream().withExtraArgs("--jbang"));

        // Should build on Jvm
        final String repository = System.getProperty("maven.repo.local");
        final Result result;
        if (repository == null) {
            result = app.buildOnJvm("--verbose");
        } else {
            result = app.buildOnJvm("--verbose", "--", "--repos", "local=file://" + repository);
        }

        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());

        // Start using DEV mode
        // https://github.com/quarkusio/quarkus/issues/18157
        // TODO Jbang doesn't support DevMode yet
        //app.start();
        //app.given().get().then().statusCode(HttpStatus.SC_OK);
    }

    // TODO: enable when Kogito for Quarkus 3 is available; currently there is no kogito-quarks-rules extension available for Quarkus 3 judging by the code.quarkus.io
    @Disabled("Disabled until Kogito extensions for Quarkus 3 are published in code.quarkus.io")
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
        QuarkusCliRestService app = cliClient.createApplication("app", defaultWithFixedStream().withExtensions(kogitoExtension,
                prettytimeExtension, RESTEASY_REACTIVE_EXTENSION, RESTEASY_REACTIVE_JACKSON_EXTENSION));

        // Should build on Jvm
        Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
        assertInstalledExtensions(app, kogitoExtension, prettytimeExtension, RESTEASY_REACTIVE_EXTENSION,
                RESTEASY_REACTIVE_JACKSON_EXTENSION);
    }

    @Tag("QUARKUS-1071")
    @Test
    public void shouldCreateApplicationWithCodeStarter() {
        // Create application with Resteasy Jackson + Spring Web (we need both for the app to run)
        QuarkusCliRestService app = cliClient.createApplication("app",
                defaultWithFixedStream().withExtensions(RESTEASY_REACTIVE_JACKSON_EXTENSION, SPRING_WEB_EXTENSION));

        // Verify By default, it installs only "quarkus-resteasy-reactive-jackson" and "quarkus-spring-web"
        assertInstalledExtensions(app, RESTEASY_REACTIVE_JACKSON_EXTENSION, SPRING_WEB_EXTENSION);

        // Start using DEV mode
        app.start();
        untilAsserted(() -> app.given().get("/greeting").then().statusCode(HttpStatus.SC_OK).and().body(is("Hello Spring")));
    }

    @Tag("QUARKUS-1071")
    @Test
    public void shouldAddAndRemoveExtensions() {
        // Create application
        String gav = QuarkusProperties.PLATFORM_GROUP_ID.get() + ":quarkus-bom:" + QuarkusProperties.getVersion();
        QuarkusCliRestService app = cliClient.createApplication("app", defaults()
                // HOTFIX: combination of --stream and --platform-bom exits without generating app
                .withStream(null)
                .withPlatformBom(gav));

        // By default, it installs only "quarkus-resteasy"
        assertInstalledExtensions(app, RESTEASY_REACTIVE_EXTENSION);

        // Let's install Quarkus SmallRye Health
        Result result = app.installExtension(SMALLRYE_HEALTH_EXTENSION);
        assertTrue(result.isSuccessful(), SMALLRYE_HEALTH_EXTENSION + " was not installed. Output: " + result.getOutput());

        // Verify both extensions now
        assertInstalledExtensions(app, RESTEASY_REACTIVE_EXTENSION, SMALLRYE_HEALTH_EXTENSION);

        // The health endpoint should be now available
        app.start();
        untilAsserted(() -> app.given().get("/q/health").then().statusCode(HttpStatus.SC_OK));
        app.stop();

        // Let's now remove the SmallRye Health extension
        result = app.removeExtension(SMALLRYE_HEALTH_EXTENSION);
        assertTrue(result.isSuccessful(), SMALLRYE_HEALTH_EXTENSION + " was not uninstalled. Output: " + result.getOutput());

        // The health endpoint should be now gone
        app.restartAndWaitUntilServiceIsStarted();
        untilAsserted(() -> app.given().get("/q/health").then().statusCode(HttpStatus.SC_NOT_FOUND));
    }

    @Tag("https://github.com/quarkusio/quarkus/issues/25184")
    @Test
    public void shouldKeepUsingTheSameQuarkusVersionAfterReload() {
        // Generate application using old community version
        QuarkusCliRestService app = cliClient.createApplication("app", defaults()
                // HOTFIX: combination of --stream and --platform-bom exits without generating app
                .withStream(null)
                .withPlatformBom("io.quarkus:quarkus-bom:3.0.0.Alpha4")
                .withExtensions(SMALLRYE_HEALTH_EXTENSION, RESTEASY_REACTIVE_EXTENSION));

        // Make sure version and groupId from the TS run is used
        app.withProperty(QuarkusProperties.PLATFORM_GROUP_ID.getPropertyKey(), QuarkusProperties.PLATFORM_GROUP_ID.get());
        app.withProperty(QuarkusProperties.PLATFORM_VERSION.getPropertyKey(), QuarkusProperties.getVersion());

        app.start();
        untilAsserted(() -> app.given().get("/q/health").then().statusCode(HttpStatus.SC_OK));

        Result result = app.removeExtension(SMALLRYE_HEALTH_EXTENSION);
        assertTrue(result.isSuccessful(), SMALLRYE_HEALTH_EXTENSION + " was not uninstalled. Output: " + result.getOutput());

        // Make sure application reloads properly without BUILD FAILURE of maven execution
        // and no "Hot deployment of the application is not supported when updating the Quarkus version" message in logs
        untilAsserted(() -> app.given().get("/q/health").then().statusCode(HttpStatus.SC_NOT_FOUND));
    }

    @Tag("QUARKUS-1255")
    @Test
    public void shouldCreateJacocoReportsFromApplicationOnJvm() {
        QuarkusCliRestService app = cliClient.createApplication("app-with-jacoco",
                defaultWithFixedStream().withExtensions("resteasy", "jacoco"));

        Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
        assertInstalledExtensions(app, "quarkus-jacoco");

        assertTrue(app.getServiceFolder().resolve("target/jacoco-report/index.html").toFile().exists(),
                "JaCoCo report directory doesn't exist");
        assertTrue(app.getServiceFolder().resolve("target/jacoco-quarkus.exec").toFile().exists(),
                "JaCoCo exec file doesn't exist");
    }

    @Tag("QUARKUS-1296")
    @Test
    public void verifyRestEasyReactiveAndClassicResteasyCollisionUserMsg() {
        QuarkusCliRestService app = cliClient.createApplication("dependencyCollision",
                defaultWithFixedStream().withExtensions("resteasy", "resteasy-reactive"));

        Result buildResult = app.buildOnJvm();

        assertFalse(buildResult.isSuccessful(),
                "Should fail because 'resteasy' and 'resteasy-reactive' extensions are not compatible");

        assertBuildError(buildResult, "Please make sure there is only one provider of the following capabilities");
        assertBuildError(buildResult, "io.quarkus:quarkus-resteasy-reactive");
        assertBuildError(buildResult, "io.quarkus:quarkus-resteasy");
    }

    @Test
    public void devModeIgnoresPomPackaging() throws IOException {
        QuarkusCliRestService app = cliClient.createApplication("pomApp", defaultWithFixedStream());
        {//set packaging to POM
            Path pom = getFileFromApplication(app, ROOT_FOLDER, "pom.xml").toPath();
            List<String> content = Files.readAllLines(pom);
            for (int i = 0; i < content.size(); i++) {
                String line = content.get(i);
                if (line.endsWith("<artifactId>pomApp</artifactId>")) {
                    content.set(i, line + "<packaging>pom</packaging>");
                    break;
                }
            }
            Files.write(pom, content);
        }
        // Start using DEV mode
        assertEquals(Duration.ofSeconds(2), app.getConfiguration().getAsDuration("startup.timeout", null));
        assertThrows(ConditionTimeoutException.class, app::start, "That application shouldn't start!");
        app.logs().assertContains("Type of the artifact is POM, skipping dev goal");
    }

    private void assertBuildError(Result result, String expectedError) {
        assertTrue(result.getOutput().contains(expectedError), "Unexpected build error message");
    }

    private void assertInstalledExtensions(QuarkusCliRestService app, String... expectedExtensions) {
        List<String> extensions = app.getInstalledExtensions();
        Stream.of(expectedExtensions).forEach(expectedExtension -> assertTrue(extensions.contains(expectedExtension),
                expectedExtension + " not found in " + extensions));
    }

    private void assertExpectedJavaVersion(File pomFile, String expectedJavaVersion) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (FileReader fileReader = new FileReader(pomFile)) {
            Model model = reader.read(fileReader);
            Assertions.assertEquals(model.getProperties().get("maven.compiler.release"), expectedJavaVersion,
                    "Unexpected Java version defined in maven.compiler.release property of pom.xml. " +
                            "Java support tool should detect host Java version or use " +
                            "the provided one by --java argument");
        } catch (IOException | XmlPullParserException e) {
            fail(e.getMessage());
        }
    }

    private void assertDockerJavaVersion(File dockerFile, String expectedVersion) {
        try (Scanner sc = new Scanner(dockerFile)) {
            String line = "";
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if (line.contains("openjdk-" + expectedVersion)) {
                    break;
                }
            }

            Assertions.assertTrue(line.contains("openjdk-" + expectedVersion),
                    DOCKERFILE_JVM + " doesn't contains expected version " + expectedVersion);
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        }
    }

    private String getSystemJavaVersion() {
        return StringUtils.substringBefore(System.getProperty("java.version"), ".");
    }

    private File getFileFromApplication(QuarkusCliRestService app, String subFolder, String fileName) {
        Path fileFolderPath = app.getServiceFolder();
        if (!StringUtils.isEmpty(subFolder)) {
            fileFolderPath = Path.of(fileFolderPath.toString(), subFolder);
        }

        return Arrays.stream(Objects.requireNonNull(fileFolderPath.toFile().listFiles()))
                .filter(f -> f.getName().equalsIgnoreCase(fileName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(fileName + " not found."));
    }
}
