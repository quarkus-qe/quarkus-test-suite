package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.annotations.EnabledOnQuarkusVersion;
import io.quarkus.test.utils.AwaitilityUtils;

@EnabledOnQuarkusVersion(version = "3.8.*", reason = "This class is testing only updates to 3.8.* versions")
@Disabled("https://github.com/quarkusio/quarkus/issues/42567")
public class Quarkus213to38CliUpdateIT extends AbstractQuarkusCliUpdateIT {
    private static final DefaultArtifactVersion oldVersion = new DefaultArtifactVersion("2.13");
    private static final DefaultArtifactVersion newVersion = new DefaultArtifactVersion("3.8");

    public Quarkus213to38CliUpdateIT() {
        super(oldVersion, newVersion);
    }

    @Test
    public void updateToLatestStream() {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplication();

        // update to latest version
        quarkusCLIAppManager.updateApp(app);

        Log.info("Starting updated app");
        // start the updated app and verify that basic /hello endpoint works
        app.start();
        untilAsserted(() -> app.given().get("/hello").then().statusCode(HttpStatus.SC_OK),
                AwaitilityUtils.AwaitilitySettings.usingTimeout(Duration.ofSeconds(10))
                        .timeoutMessage("Updated app failed to expose working /hello endpoint"));
    }
}
