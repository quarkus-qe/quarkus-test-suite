package io.quarkus.ts.quarkus.cli.update;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.surefire.shared.lang3.tuple.Pair;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.util.IQuarkusCLIAppManager;
import io.quarkus.test.util.QuarkusCLIUtils;

@Tag("quarkus-cli")
public class QuarkusCli320UpdatesIT extends AbstractQuarkusCliUpdateIT {
    private static final DefaultArtifactVersion oldVersion = new DefaultArtifactVersion("3.15");
    private static final DefaultArtifactVersion newVersion = new DefaultArtifactVersion("3.20");

    public QuarkusCli320UpdatesIT() {
        super(oldVersion, newVersion);
    }

    @Test
    /**
     * Compatibility layer, that existed between 3.9 and 3.15 was removed. Let's check, that the upgrade can still be done
     */
    public void dependencyUpdate() throws IOException, XmlPullParserException {
        IQuarkusCLIAppManager longUpdate = QuarkusCLIUtils.createAppManager(cliClient,
                new DefaultArtifactVersion("3.8"),
                newVersionStream);
        Path greatRenameCSV = Paths.get("src/test/resources/quarkus315dependencyUpdates.csv");
        List<Pair<Dependency, Dependency>> dependenciesToUpdate = loadDependencyPairsFromCSV(greatRenameCSV);
        checkDependenciesUpdate(longUpdate, dependenciesToUpdate);
    }

    @Test
    public void jdbcTracingProperty() throws IOException {
        Properties oldProperties = new Properties();
        Properties newProperties = new Properties();

        oldProperties.put("quarkus.hibernate-orm.validation.enabled", "true");
        newProperties.put("quarkus.hibernate-orm.validation.mode", "auto");

        oldProperties.put("quarkus.http.cors", "true");
        newProperties.put("quarkus.http.cors.enabled", "true");

        oldProperties.put("quarkus.log.console.json", "true");
        newProperties.put("quarkus.log.console.json.enabled", "true");

        // TODO: drop using temp dir when https://github.com/quarkusio/quarkus-updates/issues/394 is fixed
        try (var ignored = cliClient.useTemporaryDirectory()) {
            QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, newProperties);
        }
    }
}
