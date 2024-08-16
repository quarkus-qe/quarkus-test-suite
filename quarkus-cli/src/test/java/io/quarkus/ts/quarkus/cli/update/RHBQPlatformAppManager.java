package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.bootstrap.QuarkusCliClient.UpdateApplicationRequest.defaultUpdate;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.util.DefaultQuarkusCLIAppManager;

/**
 * AppManager designed to update app to specific quarkus-bom version, instead of to stream as DefaultQuarkusCLIAppManager
 */
public class RHBQPlatformAppManager extends DefaultQuarkusCLIAppManager {
    protected final DefaultArtifactVersion newPlatformVersion;

    public RHBQPlatformAppManager(QuarkusCliClient cliClient, DefaultArtifactVersion oldStreamVersion,
            DefaultArtifactVersion newStreamVersion, DefaultArtifactVersion newPlatformVersion) {
        super(cliClient, oldStreamVersion, newStreamVersion);
        this.newPlatformVersion = newPlatformVersion;
    }

    @Override
    public void updateApp(QuarkusCliRestService app) {
        Log.info("Updating app to version: " + newPlatformVersion);
        app.update(defaultUpdate()
                .withPlatformVersion(newPlatformVersion.toString()));
    }
}
