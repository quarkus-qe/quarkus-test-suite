package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.util.QuarkusCLIUtils.getQuarkusAppVersion;
import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import io.quarkus.test.util.DefaultQuarkusCLIAppManager;
import io.quarkus.test.util.IQuarkusCLIAppManager;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public abstract class AbstractQuarkusCliUpdateIT {
    @Inject
    static QuarkusCliClient cliClient;

    protected final DefaultArtifactVersion oldVersion;
    protected final DefaultArtifactVersion newVersion;
    protected final IQuarkusCLIAppManager quarkusCLIAppManager;

    public AbstractQuarkusCliUpdateIT(DefaultArtifactVersion oldVersion, DefaultArtifactVersion newVersion) {
        this(oldVersion, newVersion, new DefaultQuarkusCLIAppManager(cliClient, oldVersion, newVersion));
    }

    public AbstractQuarkusCliUpdateIT(DefaultArtifactVersion oldVersion, DefaultArtifactVersion newVersion,
            IQuarkusCLIAppManager quarkusCLIAppManager) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.quarkusCLIAppManager = quarkusCLIAppManager;
    }

    /**
     * Perform basic update to new stream and check version in pom.xml matches
     */
    @Test
    public void versionUpdateTest() throws IOException, XmlPullParserException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplication();

        quarkusCLIAppManager.updateApp(app);

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
}
