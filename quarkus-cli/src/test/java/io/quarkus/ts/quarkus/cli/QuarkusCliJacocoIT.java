package io.quarkus.ts.quarkus.cli;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import jakarta.inject.Inject;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.util.QuarkusCLIUtils;
import io.quarkus.test.util.QuarkusCLIUtils.QuarkusPlugin;

@Tag("QUARKUS-9738")
@QuarkusScenario
@DisabledOnNative(reason = "Only for JVM verification")
public class QuarkusCliJacocoIT {

    @Inject
    static QuarkusCliClient cliClient;

    @ParameterizedTest
    @ValueSource(strings = { "jacoco-app", "jacoco app with spaces" })
    public void shouldRunTestsWithJacoco(String directory) throws IOException, XmlPullParserException {
        QuarkusCliRestService app = cliClient.createApplication("org.acme:jacoco-app",
                defaults().withStream(null).withCurrentPlatformBom().withExtensions("rest", "jacoco"),
                directory);

        assertThat(QuarkusCLIUtils.getPom(app).getPackaging()).isEqualTo("quarkus");
        Plugin jacoco = new QuarkusPlugin("org.jacoco:jacoco-maven-plugin");
        PluginExecution agent = new PluginExecution();
        agent.addGoal("prepare-agent-integration");
        jacoco.addExecution(agent);
        QuarkusCLIUtils.addPluginsToPom(app, List.of(jacoco));

        QuarkusCliClient.Result result = app.buildOnJvm("--clean", "-DskipITs=false");
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
        assertThat(result.getOutput()).doesNotContain("Failed to locate serialized application model",
                "java.nio.file.InvalidPathException");
    }
}
