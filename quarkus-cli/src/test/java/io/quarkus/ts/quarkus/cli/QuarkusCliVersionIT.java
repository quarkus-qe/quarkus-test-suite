package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.builder.Version;
import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;

@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnQuarkusVersion(version = "1\\..*", reason = "Quarkus CLI has been reworked in 2.x")
@DisabledIfSystemProperty(named = "profile.id", matches = "native", disabledReason = "Only for JVM verification")
public class QuarkusCliVersionIT {

    @Inject
    static QuarkusCliClient cliClient;

    @Test
    @DisabledIfSystemProperty(named = "quarkus.platform.group-id", matches = "com.redhat.quarkus.platform", disabledReason = "productized CLI doesn't exist yet")
    public void shouldVersionMatchQuarkusVersion() {
        // Using option
        assertEquals(Version.getVersion(), cliClient.run("version").getOutput());

        // Using shortcut
        assertEquals(Version.getVersion(), cliClient.run("-v").getOutput());
    }

    @Test
    @DisabledOnQuarkusSnapshot(reason = "Snapshots use io.quarkus as a group-id, while generated code uses io.quarkus.platform")
    public void checkGroupId() throws IOException, XmlPullParserException {
        QuarkusCliRestService app = cliClient.createApplication("app-pom-file");

        //Verify, that generated projects use the same group as this one
        Properties tests = readMavenProject(new File("../pom.xml")).getProperties();
        Properties generated = readMavenProject(app.getServiceFolder().resolve("pom.xml").toFile()).getProperties();

        assertTrue(generated.containsKey("quarkus.platform.group-id"));
        assertNotEquals("", generated.getProperty("quarkus.platform.group-id"));
        assertEquals(tests.getProperty("quarkus.platform.group-id"), generated.getProperty("quarkus.platform.group-id"));
    }

    @NotNull
    private MavenProject readMavenProject(File file) throws IOException, XmlPullParserException {
        FileReader reader = new FileReader(file);
        Model model = new MavenXpp3Reader().read(reader);
        model.setPomFile(file);

        return new MavenProject(model);
    }
}
