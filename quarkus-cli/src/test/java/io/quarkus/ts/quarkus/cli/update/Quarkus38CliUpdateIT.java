package io.quarkus.ts.quarkus.cli.update;

import java.io.IOException;
import java.util.Properties;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.jupiter.api.Test;

public class Quarkus38CliUpdateIT extends AbstractQuarkusCliUpdateIT {
    private static final DefaultArtifactVersion oldLts = new DefaultArtifactVersion("3.2");
    private static final DefaultArtifactVersion newLts = new DefaultArtifactVersion("3.8");

    public Quarkus38CliUpdateIT() {
        super(oldLts, newLts);
    }

    @Test
    public void propertiesUpdateTest() throws IOException {
        Properties oldProperties = new Properties();
        oldProperties.put("quarkus.hibernate-search-orm.automatic-indexing.synchronization.strategy", "sync");
        oldProperties.put("quarkus.hibernate-search-orm.quarkusQE.automatic-indexing.synchronization.strategy", "sync");

        Properties expectedNewProperties = new Properties();
        expectedNewProperties.put("quarkus.hibernate-search-orm.indexing.plan.synchronization.strategy", "sync");
        expectedNewProperties.put("quarkus.hibernate-search-orm.quarkusQE.indexing.plan.synchronization.strategy", "sync");

        checkPropertiesUpdate(oldProperties, expectedNewProperties);
    }
}
