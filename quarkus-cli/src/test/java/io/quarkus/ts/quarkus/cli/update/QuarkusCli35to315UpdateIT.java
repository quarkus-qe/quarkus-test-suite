package io.quarkus.ts.quarkus.cli.update;

import static io.quarkus.test.util.QuarkusCLIUtils.getDependencies;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.QuarkusCliRestService;

@DisabledOnOs(value = OS.WINDOWS, disabledReason = "https://github.com/quarkusio/quarkus/issues/46639")
@Tag("quarkus-cli")
public class QuarkusCli35to315UpdateIT extends AbstractQuarkusCliUpdateIT {
    private static final DefaultArtifactVersion oldVersion = new DefaultArtifactVersion("3.2");
    private static final DefaultArtifactVersion newVersion = new DefaultArtifactVersion("3.15");

    public QuarkusCli35to315UpdateIT() {
        super(oldVersion, newVersion);
    }

    /**
     * Test that updating from Quarkus 3.2 to 3.15 correctly transforms
     * the 'quarkus-rest-client-reactive-jackson' extension to 'quarkus-rest-client-jackson',
     * and does not incorrectly transform it to 'quarkus-resteasy-client-jackson'.
     * This test addresses a specific issue where the update recipes could apply in the wrong order,
     * causing incorrect dependency transformations. More info here:https://github.com/quarkusio/quarkus/issues/44183
     *
     */
    @Tag("https://github.com/quarkusio/quarkus/issues/44183")
    @Test
    public void testCorrectRecipesOrder() throws XmlPullParserException, IOException {

        QuarkusCliRestService app = quarkusCLIAppManager
                .createApplicationWithExtensions("quarkus-rest-client-reactive-jackson");

        quarkusCLIAppManager.updateApp(app);
        List<Dependency> dependencies = getDependencies(app);
        assertTrue(
                dependencies.stream().anyMatch(dependency -> dependency.getArtifactId().equals("quarkus-rest-client-jackson")),
                "'quarkus-rest-client-jackson' should be present after the update.");

        assertFalse(
                dependencies.stream()
                        .anyMatch(dependency -> dependency.getArtifactId().equals("quarkus-rest-client-reactive-jackson")),
                "'quarkus-rest-client-reactive-jackson' should not be present after the update.");

        assertFalse(
                dependencies.stream()
                        .anyMatch(dependency -> dependency.getArtifactId().equals("quarkus-resteasy-client-jackson")),
                "'quarkus-resteasy-client-jackson' should not be present after the update.");
    }

}
