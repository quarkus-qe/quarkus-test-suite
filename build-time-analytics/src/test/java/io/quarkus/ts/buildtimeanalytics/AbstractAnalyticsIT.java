package io.quarkus.ts.buildtimeanalytics;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.getAnalyticsPayloadFile;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.getPomFile;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsFilesUtils.getRemoteConfigFile;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.APP_NAME_WITH_DENIED_GROUP_ID;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.APP_NAME_WITH_NON_DENIED_GROUP_ID;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_FAKE_URI_BASE;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_IP;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_ANALYTICS_URI_BASE_PROPERTY;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.QUARKUS_EXTENSION_VERSION_PATTERN;
import static io.quarkus.ts.buildtimeanalytics.AnalyticsUtils.UNRECOGNIZED_PROPERTY_FORMAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Assertions;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliClient.Result;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.services.quarkus.model.QuarkusProperties;
import io.restassured.path.json.JsonPath;

public abstract class AbstractAnalyticsIT {
    @Inject
    static QuarkusCliClient cliClient;

    protected QuarkusCliRestService createAppDefault() {
        return createApp(APP_NAME_WITH_NON_DENIED_GROUP_ID);
    }

    protected QuarkusCliRestService createAppWithDeniedGroupId() {
        return createApp(APP_NAME_WITH_DENIED_GROUP_ID);
    }

    protected QuarkusCliRestService createAppWithExtensions(String... extensions) {
        return createApp(APP_NAME_WITH_NON_DENIED_GROUP_ID, extensions);
    }

    private QuarkusCliRestService createApp(String appName, String... extensions) {
        String gav = QuarkusProperties.PLATFORM_GROUP_ID.get() + ":quarkus-bom:" + QuarkusProperties.getVersion();
        QuarkusCliClient.CreateApplicationRequest createApplicationRequest = defaults()
                .withPlatformBom(gav)
                .withExtraArgs("--no-code", "--no-wrapper")
                .withExtensions(extensions);
        return cliClient.createApplication(appName, createApplicationRequest);
    }

    protected Result buildApp(Function<String[], Result> buildFunction, String... buildProperties) {
        Set<String> properties = new HashSet<>();
        // Always provide fake analytics URI to avoid sending data.
        properties.add(formatBuildProperty(QUARKUS_ANALYTICS_URI_BASE_PROPERTY, QUARKUS_ANALYTICS_FAKE_URI_BASE));
        properties.addAll(Arrays.asList(buildProperties));
        return buildFunction.apply(properties.toArray(new String[] {}));
    }

    protected String formatBuildProperty(String property, String value) {
        return String.format("-D%s=%s", property, value);
    }

    protected void verifyRemoteConfigRefreshed() {
        File remoteConfigFile = getRemoteConfigFile();
        assertTrue(remoteConfigFile.exists());
        try (var reader = new FileReader(remoteConfigFile)) {
            JsonPath json = JsonPath.from(reader);
            assertFalse(json.getList("deny_anonymous_ids", String.class).contains("fake-anonymous-id"));
        } catch (IOException e) {
            Assertions.fail("Failed to reader 'remoteConfigFile: " + e.getMessage());
        }
    }

    protected void verifyBuildSuccessful(Result buildResult) {
        assertTrue(buildResult.isSuccessful(), "The application didn't build on JVM. Output: " + buildResult.getOutput());
    }

    protected void verifyBuildPropertyRecognized(Result buildResult, String property) {
        assertFalse(buildResult.getOutput().contains(String.format(UNRECOGNIZED_PROPERTY_FORMAT, property)));
    }

    protected void verifyPayloadAbsent(QuarkusCliRestService app) {
        File payload = getAnalyticsPayloadFile(app);
        assertFalse(payload.exists());
    }

    protected void verifyValidPayloadPresent(QuarkusCliRestService app) {
        File payload = getAnalyticsPayloadFile(app);
        assertTrue(payload.exists());
        validatePayload(app, payload);
    }

    private void validatePayload(QuarkusCliRestService app, File payload) {
        try (var reader = new FileReader(payload)) {
            JsonPath json = JsonPath.from(reader);
            validateJsonValues(json);
            validateExtensions(app, json);
        } catch (IOException e) {
            Assertions.fail("Failed to read 'payload': " + e.getMessage());
        }
    }

    private void validateJsonValues(JsonPath json) {
        Map<String, String> values = Map.of(
                "event", getEventType(),
                "context.ip", QUARKUS_ANALYTICS_IP,
                "context.quarkus.version", QuarkusProperties.getVersion(),
                "context.java.vendor", System.getProperty("java.vendor"),
                "context.java.version", System.getProperty("java.version"),
                "context.os.name", System.getProperty("os.name"),
                "context.os.os_arch", System.getProperty("os.arch"),
                "context.os.version", System.getProperty("os.version"));
        values.forEach((key, value) -> assertEquals(value, json.getString(key)));
    }

    private void validateExtensions(QuarkusCliRestService app, JsonPath json) {
        List<Dependency> pomDependencies = getPomDependencies(app);
        List<String> pomDependencyGAs = pomDependencies.stream()
                .map(dependency -> mapToGA(dependency.getGroupId(), dependency.getArtifactId()))
                .collect(Collectors.toList());
        List<PayloadExtension> payloadExtensions = json.getList("properties.app_extensions", PayloadExtension.class);
        List<String> payloadExtensionGAs = payloadExtensions.stream()
                .map(payloadExtension -> mapToGA(payloadExtension.getGroupId(), payloadExtension.getArtifactId()))
                .collect(Collectors.toList());
        assertEquals(pomDependencyGAs, payloadExtensionGAs);

        // RHBQ doesn't guarantee the same version of the platform and core extensions
        if (!QuarkusProperties.isRHBQ()) {
            List<PayloadExtension> extensionsWithMismatchedVersion = payloadExtensions.stream()
                    .filter(extension -> !QUARKUS_EXTENSION_VERSION_PATTERN.matcher(extension.getVersion()).matches())
                    .collect(Collectors.toList());
            assertEquals(0, extensionsWithMismatchedVersion.size(),
                    String.format("All extensions versions must match pattern: '%s'. Offending extensions: %s",
                            QUARKUS_EXTENSION_VERSION_PATTERN.pattern(), extensionsWithMismatchedVersion));
        }
    }

    private List<Dependency> getPomDependencies(QuarkusCliRestService app) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (var fileReader = new FileReader(getPomFile(app))) {
            Model model = reader.read(fileReader);
            return model.getDependencies().stream()
                    .filter(dependency -> !Objects.equals(dependency.getScope(), "test"))
                    .collect(Collectors.toList());
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    private String mapToGA(String groupId, String artifactId) {
        return String.format("%s:%s", groupId, artifactId);
    }

    protected abstract String getEventType();
}
