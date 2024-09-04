package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.util.QuarkusCLIUtils.addDependenciesToPom;
import static io.quarkus.test.util.QuarkusCLIUtils.getDependencies;
import static io.quarkus.test.util.QuarkusCLIUtils.getQuarkusAppVersion;
import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.surefire.shared.lang3.tuple.ImmutablePair;
import org.apache.maven.surefire.shared.lang3.tuple.Pair;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.quarkus.model.QuarkusProperties;
import io.quarkus.test.util.DefaultQuarkusCLIAppManager;
import io.quarkus.test.util.IQuarkusCLIAppManager;
import io.quarkus.test.util.QuarkusCLIUtils;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
@DisabledOnNative // Only for JVM verification
@Tag("quarkus-cli")
public abstract class AbstractQuarkusCliUpdateIT {
    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_COMMENT = "#";

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
        // check that updated app is using RHBQ, if we're testing with RHBQ
        if (QuarkusProperties.getVersion().contains("redhat")) {
            assertTrue(updatedVersion.toString().contains("redhat"),
                    "Updated app is not using \"redhat\" version. Found version: " + updatedVersion);
        }

        Log.info("Starting updated app");
        // start the updated app and verify that basic /hello endpoint works
        app.start();
        untilAsserted(() -> app.given().get("/hello").then().statusCode(HttpStatus.SC_OK),
                AwaitilityUtils.AwaitilitySettings.usingTimeout(Duration.ofSeconds(10))
                        .timeoutMessage("Updated app failed to expose working /hello endpoint"));
    }

    /**
     * Load dependencies that should be updated from CSV.
     * Expects format: <oldDependency>,<newDependency>
     */
    protected List<Pair<Dependency, Dependency>> loadDependencyPairsFromCSV(Path csvFile) throws IOException {
        List<Pair<Dependency, Dependency>> dependencyPairs = new ArrayList<>();
        try (Stream<String> lines = Files.lines(csvFile)) {
            List<List<String>> records = lines
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith(CSV_COMMENT))
                    .map(line -> Arrays.asList(line.split(CSV_SEPARATOR)))
                    .toList();

            records.forEach(record -> {
                if (record.size() != 2) {
                    throw new IllegalArgumentException(
                            "Records in CSV must have 2 fields. Not true for record: " + record);
                }

                dependencyPairs.add(new ImmutablePair<>(
                        new QuarkusCLIUtils.QuarkusDependency(record.get(0)),
                        new QuarkusCLIUtils.QuarkusDependency(record.get(1))));
            });
        }

        return dependencyPairs;
    }

    /**
     * Create app, put dependencies into it, update it and check new dependencies are present.
     * Use {@link QuarkusCLIUtils.QuarkusDependency} it has .equals method properly set.
     *
     * @param dependenciesToUpdate Pairs of dependencies that should be updated.
     *        Key should be old dependency that should be updated. Value the new expected one.
     */
    public static void checkDependenciesUpdate(IQuarkusCLIAppManager appManager,
            List<Pair<Dependency, Dependency>> dependenciesToUpdate)
            throws XmlPullParserException, IOException {
        QuarkusCliRestService app = appManager.createApplication();
        List<Dependency> oldDependencies = dependenciesToUpdate.stream().map(Pair::getKey).collect(Collectors.toList());
        addDependenciesToPom(app, oldDependencies);

        appManager.updateApp(app);
        List<Dependency> actualDependencies = getDependencies(app);
        dependenciesToUpdate.forEach(dependencyToUpdate -> {
            assertFalse(actualDependencies.contains(dependencyToUpdate.getKey()),
                    "Dependency " + dependencyToUpdate.getKey() + " should be updated to " + dependencyToUpdate.getValue());

            assertTrue(actualDependencies.contains(dependencyToUpdate.getValue()),
                    "Dependency " + dependencyToUpdate.getKey() + " should be updated to " + dependencyToUpdate.getValue());
        });
    }
}
