package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.util.QuarkusCLIUtils.getQuarkusAppVersion;
import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.quarkus.model.QuarkusProperties;
import io.quarkus.test.util.DefaultQuarkusCLIAppManager;
import io.quarkus.test.util.IQuarkusCLIAppManager;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public abstract class AbstractQuarkusCliUpdateIT {
    @Inject
    static QuarkusCliClient cliClient;

    protected final DefaultArtifactVersion oldVersionStream;
    protected final DefaultArtifactVersion newVersionStream;
    protected final String newVersionFromProperties;
    protected final IQuarkusCLIAppManager quarkusCLIAppManager;

    public AbstractQuarkusCliUpdateIT(DefaultArtifactVersion oldVersionStream, DefaultArtifactVersion newVersionStream) {
        this.oldVersionStream = oldVersionStream;
        this.newVersionStream = newVersionStream;

        // takes quarkus.platform.version from maven parameters. If present, it will update to this exact BOM version
        // otherwise it will default to update to stream
        this.newVersionFromProperties = QuarkusProperties.getVersion();
        this.quarkusCLIAppManager = createAppManager();
    }

    protected IQuarkusCLIAppManager createAppManager() {
        if (this.newVersionFromProperties != null && this.newVersionFromProperties.contains("redhat")) {
            return new RHBQPlatformAppManager(cliClient, oldVersionStream, newVersionStream,
                    new DefaultArtifactVersion(newVersionFromProperties));
        }
        return new DefaultQuarkusCLIAppManager(cliClient, oldVersionStream, newVersionStream);
    }

    /**
     * Perform basic update to new stream and check version in pom.xml matches
     */
    @Test
    public void versionUpdateTest() throws IOException, XmlPullParserException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplication();

        quarkusCLIAppManager.updateApp(app);

        DefaultArtifactVersion updatedVersion = getQuarkusAppVersion(app);
        assertEquals(newVersionStream.getMajorVersion(), updatedVersion.getMajorVersion(),
                "Major version for app updated to " + newVersionStream + "should be " + newVersionStream.getMajorVersion());
        assertEquals(newVersionStream.getMinorVersion(), updatedVersion.getMinorVersion(),
                "Minor version for app updated to " + newVersionStream + " should be " + newVersionStream.getMinorVersion());
        // check that updated app is using RHBQ
        assertTrue(updatedVersion.toString().contains("redhat"),
                "Updated app is not using \"redhat\" version. Found version: " + updatedVersion);

        Log.info("Starting updated app");
        // start the updated app and verify that basic /hello endpoint works
        app.start();
        untilAsserted(() -> app.given().get("/hello").then().statusCode(HttpStatus.SC_OK),
                AwaitilityUtils.AwaitilitySettings.usingTimeout(Duration.ofSeconds(10))
                        .timeoutMessage("Updated app failed to expose working /hello endpoint"));
    }
}
