package io.quarkus.ts.quarkus.cli.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.util.QuarkusCLIUtils;
import io.quarkus.test.util.QuarkusCLIUtils.QuarkusDependency;

/**
 * Tests Quarkus CLI update command recipes for changes between 3.27 and 3.33.
 */
@Tag("quarkus-cli")
public class QuarkusCli333UpdatesIT extends AbstractQuarkusCliUpdateIT {

    public QuarkusCli333UpdatesIT() {
        super(new DefaultArtifactVersion("3.27"), new DefaultArtifactVersion("3.33"));
    }

    /**
     * Tests Quarkus CLI update recipe for 3.31.
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.31#junit-6-gear-white_check_mark
     * https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.31#extensions-gear-white_check_mark
     */
    @Test
    void testJunitAndExtensions() throws IOException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplicationWithExtensions();
        Path pom = app.getServiceFolder().resolve("pom.xml");
        // Let's check our assumptions first
        assertEquals(1,
                getAllLinesWith(pom, "<extensions>true</extensions>").size(),
                "Generated pom doesn't contain <extension> line, this test doesn't expect it!");
        assertEquals(0, getAllLinesWith(pom, "<argLine>").size(),
                "Generated pom should not contain agrLine configuration for surefire and failsafe");
        assertNotEquals(0, getAllLinesWith(pom, "junit5").size(),
                "Generated file should use JUnit5!");

        List<String> updatedPom;
        try (Stream<String> lines = Files.lines(pom)) {
            updatedPom = lines.filter(line -> !line.contains("<extensions>true</extensions>")).toList();
        }
        Files.write(pom, updatedPom);
        assertEquals(0, getAllLinesWith(pom, "<extensions>true</extensions>").size(),
                "The code above should have removed all extensions!");

        quarkusCLIAppManager.updateApp(app);

        List<String> junit = getAllLinesWith(pom, "junit");
        List<String> junit5 = getAllLinesWith(pom, "junit5");
        assertNotEquals(0, junit.size(), "JUnit5 should have beed replaced with JUnit");
        assertEquals(0, junit5.size(), "JUnit5 should have beed replaced with JUnit everywhere!");
        assertEquals(1, getAllLinesWith(pom, "<extensions>true</extensions>").size(),
                "Update should enable extensions!");

        List<String> argLinesAfter = getAllLinesWith(pom, "<argLine>");
        assertEquals(2, argLinesAfter.size(),
                "Updated pom should contain the agrLine configurations for the same plugins as before");
        for (String line : argLinesAfter) {
            assertTrue(line.contains("@{argLine}"),
                    "Updated pom doesn't contain @{argLine}!");
        }
    }

    /**
     * Tests Quarkus CLI update recipe for 3.30.
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.30#breaking-changes
     */
    @Test
    void testJDBCMetrics() throws IOException {
        Properties oldProperties = new Properties();
        Properties newProperties = new Properties();

        oldProperties.put("quarkus.datasource.jdbc.enable-metrics", "true");
        newProperties.put("quarkus.datasource.jdbc.metrics.enabled", "true");

        // TODO: drop using temp dir when https://github.com/quarkusio/quarkus-updates/issues/394 is fixed
        try (var ignored = cliClient.useTemporaryDirectory()) {
            QuarkusCLIUtils.checkPropertiesUpdate(quarkusCLIAppManager, oldProperties, newProperties);
        }
    }

    /**
     * Tests Quarkus CLI update recipe for 3.31
     * See https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.31#testcontainers-2-gear-white_check_mark
     */
    @Test
    void testContainers() throws IOException, XmlPullParserException {
        QuarkusCliRestService app = quarkusCLIAppManager.createApplicationWithExtensions("quarkus-rest");

        {
            Model pom = QuarkusCLIUtils.getPom(app);
            Dependency testcontainers = new QuarkusDependency("org.testcontainers:postgresql:1.21.4");
            pom.addDependency(testcontainers);
            QuarkusCLIUtils.savePom(app, pom);
        }

        Path pomFile = app.getServiceFolder().resolve("pom.xml");
        assertEquals(1, getAllLinesWith(pomFile, ">postgresql<").size(),
                "The prepared pom should contain a single postgres dependency");

        quarkusCLIAppManager.updateApp(app);

        assertTrue(getAllLinesWith(pomFile, ">postgresql<").isEmpty(),
                "postgresql dependency should be replaced with testcontainers-postgresql");

        Model pom = QuarkusCLIUtils.getPom(app);
        List<Dependency> dependencies = pom.getDependencies();
        List<Dependency> testContainers = dependencies.stream()
                .filter(d -> d.getGroupId().equals("org.testcontainers")).toList();
        assertEquals(1, testContainers.size(), "There must be only one testcontainers dependency!");
        Dependency postrgesSql = testContainers.get(0);
        assertEquals("org.testcontainers", postrgesSql.getGroupId(),
                "Testcontainers dependency should keep its groupId");
        assertEquals("testcontainers-postgresql", postrgesSql.getArtifactId(),
                "Testcontainers dependency should be renamed to testcontainers-postgresql");
        assertTrue(postrgesSql.getVersion().matches("^2\\.\\d+\\.\\d+$"),
                "Testcontainers dependency should be updated to version 2");
    }

    private static @NotNull List<String> getAllLinesWith(Path pom, String content) throws IOException {
        try (Stream<String> lines = Files.lines(pom)) {
            return lines.filter(line -> line.contains(content)).toList();
        }
    }
}
