package io.quarkus.ts.properties.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.bootstrap.QuarkusVersionAwareCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.TestQuarkusCli;

@QuarkusScenario
@Tag("quarkus-cli")
@Tag("https://github.com/quarkusio/quarkus/pull/50790")
public class YamlBuildFailureIT {
    private static final Logger LOG = Logger.getLogger(YamlBuildFailureIT.class);
    private static final String EXTENSIONS_LINE = "<extensions>true</extensions>";
    private static final String PACKAGING_LINE = "<packaging>";

    @TestQuarkusCli
    public void verifyDefaultJarPackageAndNoMavenExtesions(QuarkusVersionAwareCliClient cliClient) throws IOException {
        QuarkusCliRestService app = cliClient.createApplication("yaml-app",
                QuarkusCliClient.CreateApplicationRequest.defaults()
                        .withExtensions("quarkus-config-yaml", "quarkus-rest"));
        adjustPomXml(app.getFileFromApplication("pom.xml").toPath());

        Path configFolder = app.getServiceFolder().resolve("src/main/resources/");

        //copy template file from test/resources to the app
        ClassLoader loader = YamlBuildFailureIT.class.getClassLoader();
        Files.copy(loader.getResourceAsStream("application-profile.yml"), configFolder.resolve("application-dev.yml"));
        Files.copy(loader.getResourceAsStream("application-profile.yml"), configFolder.resolve("application-prod.yml"));

        // Make sure, that HTTP endpoint uses the content of the file
        Path resourceFile = app.getFileFromApplication("src/main/java/org/acme/", "GreetingResource.java").toPath();
        Path modifiedResourceFile = Paths.get("").resolve("src/test/java/org/acme/GreetingResource.java");
        Files.copy(modifiedResourceFile, resourceFile, StandardCopyOption.REPLACE_EXISTING);
        QuarkusCliClient.Result result = app.buildOnJvm("--no-tests");
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
        // Start using DEV mode;
        app.start();
        app.given()
                .get("/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Hello from modified file"));
    }

    private static void adjustPomXml(Path pom) throws IOException {
        List<String> newLines = new ArrayList<>();
        for (String line : Files.readAllLines(pom, StandardCharsets.UTF_8)) {
            if (line.contains(EXTENSIONS_LINE)) {
                LOG.info("Removing Maven extensions support for quarkus-maven-plugin");
                LOG.info("Adding Maven executions for quarkus-maven-plugin");
                newLines.add("                <executions>");
                newLines.add("                    <execution>");
                newLines.add("                        <goals>");
                newLines.add("                            <goal>build</goal>");
                newLines.add("                            <goal>generate-code</goal>");
                newLines.add("                            <goal>generate-code-tests</goal>");
                newLines.add("                            <goal>native-image-agent</goal>");
                newLines.add("                        </goals>");
                newLines.add("                    </execution>");
                newLines.add("                </executions>");
            } else if (line.contains(PACKAGING_LINE)) {
                LOG.info("Removing definition of packaging");
            } else {
                newLines.add(line);
            }
        }
        Files.write(pom, newLines, StandardCharsets.UTF_8);
    }
}
