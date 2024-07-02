package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;
import static io.quarkus.test.bootstrap.QuarkusCliClient.UpdateApplicationRequest.defaultUpdate;
import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.utils.AwaitilityUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
    public void versionUpdateTest() throws ParserConfigurationException, IOException, SAXException {
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
        putPropertiesToApp(app, oldProperties);

        updateAppToNewStream(app);
        Properties newProperties = readPropertiesFile(app);

        for (Map.Entry<Object, Object> entry : expectedNewProperties.entrySet()) {
            assertTrue(newProperties.containsKey(entry.getKey()),
                    "Properties after update does not contain " + entry.getKey());
            assertEquals(entry.getValue(), newProperties.get(entry.getKey()),
                    "Property " + entry.getKey() + " does not match after update");
        }
    }

    protected QuarkusCliRestService createAppBeforeUpdate() {
        Log.info("Creating app with version stream: " + oldVersion);
        return cliClient.createApplication("app", defaults()
                .withPlatformBom(null)
                .withStream(oldVersion.toString()));
    }

    protected void updateAppToNewStream(QuarkusCliRestService app) {
        Log.info("Updating app to version stream: " + newVersion);
        app.update(defaultUpdate().withStream(newVersion.toString()));
    }

    protected DefaultArtifactVersion getQuarkusAppVersion(QuarkusCliRestService app)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document pom = dBuilder.parse(app.getFileFromApplication("pom.xml"));

        return new DefaultArtifactVersion(pom.getElementsByTagName("quarkus.platform.version").item(0).getTextContent());
    }

    /**
     * Put properties into app's application.properties file
     */
    protected void putPropertiesToApp(QuarkusCliRestService app, Properties properties) throws IOException {
        File propertiesFile = getPropertiesFile(app);
        BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile));

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            writer.append(entry.getKey().toString());
            writer.append("=");
            writer.append(entry.getValue().toString());
            writer.append("\n");
        }
        writer.close();
    }

    protected Properties readPropertiesFile(QuarkusCliRestService app) throws IOException {
        File propertiesFile = getPropertiesFile(app);

        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));

        return properties;
    }

    protected File getPropertiesFile(QuarkusCliRestService app) {
        return app.getFileFromApplication("src/main/resources", "application.properties");
    }
}
