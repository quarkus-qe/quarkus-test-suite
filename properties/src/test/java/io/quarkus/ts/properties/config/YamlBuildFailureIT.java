package io.quarkus.ts.properties.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

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
public class YamlBuildFailureIT {
    private static final Logger LOG = Logger.getLogger(YamlBuildFailureIT.class);
    private static final String FOR_REMOVAL = "<extensions>true</extensions>";

    @TestQuarkusCli
    public void verify(QuarkusVersionAwareCliClient cliClient) throws IOException {
        QuarkusCliRestService app = cliClient.createApplication("yaml-app",
                QuarkusCliClient.CreateApplicationRequest.defaults()
                        .withExtensions("quarkus-config-yaml", "quarkus-rest"));

        Path pom = app.getFileFromApplication("pom.xml").toPath();
        //        Path newPom = withoutLine(pom, FOR_REMOVAL);
        //        Files.copy(newPom, pom, StandardCopyOption.REPLACE_EXISTING);
        Path configFolder = app.getServiceFolder().resolve("src/main/resources/");

        //copy template file from test/resources to the app
        ClassLoader loader = YamlBuildFailureIT.class.getClassLoader();
        Files.copy(loader.getResourceAsStream("application-profile.yml"), configFolder.resolve("application-dev.yml"));
        Files.copy(loader.getResourceAsStream("application-profile.yml"), configFolder.resolve("application-prod.yml"));

        // Make sure, that HTTP endpoint uses the content of the file
        Path resourceFile = app.getFileFromApplication("src/main/java/org/acme/", "GreetingResource.java").toPath();
        Path modifiedResourceFile = Paths.get("").resolve("src/test/java/org/acme/GreetingResource.java");
        Files.copy(modifiedResourceFile, resourceFile, StandardCopyOption.REPLACE_EXISTING);

        LOG.info("Version info ... " + cliClient.getQuarkusVersion());
        LOG.info("pom.xml ...");
        LOG.info("======");
        Files.lines(pom).forEach(LOG::info);
        LOG.info("======");

        QuarkusCliClient.Result result = app.buildOnJvm("--no-tests", "--", "-X", "-U");
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
        // Start using DEV mode;
        app.start();
        app.given()
                .get("/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Hello from modified file"));
    }

    private static Path withoutLine(Path source, String forRemoval) {
        Path pom = source.toAbsolutePath();
        Path temporaryPom = pom.resolveSibling(pom.getFileName() + ".tmp");
        LOG.info("Removing " + forRemoval + " from " + pom + " using " + temporaryPom);
        try (Stream<String> lines = Files.lines(pom);
                BufferedWriter writer = Files.newBufferedWriter(temporaryPom, StandardOpenOption.CREATE_NEW)) {
            lines
                    .filter(line -> !line.contains(forRemoval))
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Failed to remove " + forRemoval + " from " + pom, e);
        }
        return temporaryPom;
    }
}
