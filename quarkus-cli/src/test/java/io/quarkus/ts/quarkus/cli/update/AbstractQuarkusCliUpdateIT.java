package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;
import static io.quarkus.test.bootstrap.QuarkusCliClient.UpdateApplicationRequest.defaultUpdate;
import static io.quarkus.test.util.QuarkusCLIUtils.addDependenciesToPom;
import static io.quarkus.test.util.QuarkusCLIUtils.addPluginsToPom;
import static io.quarkus.test.util.QuarkusCLIUtils.getDependencies;
import static io.quarkus.test.util.QuarkusCLIUtils.getPlugins;
import static io.quarkus.test.util.QuarkusCLIUtils.getQuarkusAppVersion;
import static io.quarkus.test.util.QuarkusCLIUtils.readPropertiesFile;
import static io.quarkus.test.util.QuarkusCLIUtils.readPropertiesYamlFile;
import static io.quarkus.test.util.QuarkusCLIUtils.writePropertiesToPropertiesFile;
import static io.quarkus.test.util.QuarkusCLIUtils.writePropertiesToYamlFile;
import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.quarkus.CliDevModeVersionLessQuarkusApplicationManagedResource;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public abstract class AbstractQuarkusCliUpdateIT {
    @Inject
    static QuarkusCliClient cliClient;

    protected final DefaultArtifactVersion oldVersion;
    protected final DefaultArtifactVersion newVersion;

    public AbstractQuarkusCliUpdateIT(DefaultArtifactVersion oldVersion, DefaultArtifactVersion newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    /**
     * Perform basic update to new stream and check version in pom.xml matches
     */
    @Test
    public void versionUpdateTest() throws IOException, XmlPullParserException {
        QuarkusCliRestService app = createAppBeforeUpdate();

        updateAppToNewStream(app);

        DefaultArtifactVersion updatedVersion = getQuarkusAppVersion(app);
        assertEquals(newVersion.getMajorVersion(), updatedVersion.getMajorVersion(),
                "Major version for app updated to " + newVersion + "should be " + newVersion.getMajorVersion());
        assertEquals(newVersion.getMinorVersion(), updatedVersion.getMinorVersion(),
                "Minor version for app updated to " + newVersion + " should be " + newVersion.getMinorVersion());

        Log.info("Starting updated app");
        // start the updated app and verify that basic /hello endpoint works
        app.start();
        untilAsserted(() -> app.given().get("/hello").then().statusCode(HttpStatus.SC_OK),
                AwaitilityUtils.AwaitilitySettings.usingTimeout(Duration.ofSeconds(10))
                        .timeoutMessage("Updated app failed to expose working /hello endpoint"));
    }

    protected void checkPropertiesUpdate(Properties oldProperties, Properties expectedNewProperties) throws IOException {
        QuarkusCliRestService app = createAppBeforeUpdate();
        writePropertiesToPropertiesFile(app, oldProperties);

        updateAppToNewStream(app);
        Properties newProperties = readPropertiesFile(app);

        verifyProperties(newProperties, oldProperties, expectedNewProperties);
    }

    protected void checkYamlPropertiesUpdate(Properties oldProperties, Properties expectedNewProperties) throws IOException {
        // create app with yaml extension
        QuarkusCliRestService app = cliClient.createApplication("app", defaults()
                .withPlatformBom(null)
                .withExtensions("quarkus-config-yaml")
                .withStream(oldVersion.toString()));

        // write properties to yaml
        writePropertiesToYamlFile(app, oldProperties);

        updateAppToNewStream(app);

        Properties properties = readPropertiesYamlFile(app);
        verifyProperties(properties, oldProperties, expectedNewProperties);
    }

    private void verifyProperties(Properties actualProperties, Properties oldProperties, Properties expectedNewProperties) {
        for (Map.Entry<Object, Object> entry : expectedNewProperties.entrySet()) {
            assertTrue(actualProperties.containsKey(entry.getKey()),
                    "Properties after update does not contain " + entry.getKey());
            assertEquals(entry.getValue(), actualProperties.get(entry.getKey()),
                    "Property " + entry.getKey() + " does not match after update");
        }

        for (Map.Entry<Object, Object> entry : oldProperties.entrySet()) {
            assertFalse(actualProperties.containsKey(entry.getKey()),
                    "Properties after update should not contain " + entry.getKey());
        }
    }

    protected void checkDependenciesUpdate(List<Dependency> oldDependencies, List<Dependency> newDependencies)
            throws XmlPullParserException, IOException {
        QuarkusCliRestService app = createAppBeforeUpdate();
        addDependenciesToPom(app, oldDependencies);

        updateAppToNewStream(app);

        List<Dependency> actualDependencies = getDependencies(app);
        oldDependencies.forEach(dependency -> assertFalse(actualDependencies.contains(dependency),
                "Pom.xml after update should not contain dependency: " + dependency));
        newDependencies.forEach(dependency -> assertTrue(actualDependencies.contains(dependency),
                "Pom.xml after update should contain dependency: " + dependency));
    }

    protected void checkPluginUpdate(List<Plugin> oldPlugins, List<Plugin> newPlugins)
            throws XmlPullParserException, IOException {
        QuarkusCliRestService app = createAppBeforeUpdate();
        addPluginsToPom(app, oldPlugins);

        updateAppToNewStream(app);

        List<Plugin> actualPlugins = getPlugins(app);
        oldPlugins.forEach(plugin -> assertFalse(actualPlugins.contains(plugin),
                "Pom.xml after update should not contain plugin " + plugin));
        newPlugins.forEach(plugin -> assertTrue(actualPlugins.contains(plugin),
                "Pom.xml after update should contain plugin " + plugin));
    }

    protected QuarkusCliRestService createAppBeforeUpdate() {
        Log.info("Creating app with version stream: " + oldVersion);
        return cliClient.createApplication("app", defaults()
                .withPlatformBom(null)
                .withStream(oldVersion.toString())
                // overwrite managedResource to use quarkus version defined in pom.xml and not overwrite it in CLI command
                .withManagedResourceCreator((serviceContext,
                        quarkusCliClient) -> managedResourceBuilder -> new CliDevModeVersionLessQuarkusApplicationManagedResource(
                                serviceContext, quarkusCliClient)));
    }

    protected void updateAppToNewStream(QuarkusCliRestService app) {
        Log.info("Updating app to version stream: " + newVersion);
        app.update(defaultUpdate().withStream(newVersion.toString()));
    }
}
