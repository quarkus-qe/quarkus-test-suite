package io.quarkus.ts.properties.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.bootstrap.QuarkusVersionAwareCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.TestQuarkusCli;

@QuarkusScenario
@Tag("quarkus-cli")
public class YamlBuildFailureIT {

    @TestQuarkusCli
    public void verify(QuarkusVersionAwareCliClient cliClient) throws IOException {
        QuarkusCliRestService app = cliClient.createApplication("yaml-app",
                QuarkusCliClient.CreateApplicationRequest.defaults()
                        .withExtensions("quarkus-config-yaml", "quarkus-rest"));

        // Copy template file from test/resources to the app
        Path configFolder = app.getServiceFolder().resolve("src/main/resources/");
        ClassLoader loader = YamlBuildFailureIT.class.getClassLoader();
        Files.copy(loader.getResourceAsStream("application-profile.yml"), configFolder.resolve("application-dev.yml"));
        Files.copy(loader.getResourceAsStream("application-profile.yml"), configFolder.resolve("application-prod.yml"));

        // Copy GreetingResource file from test/java/org/acme to the app
        Path resourceFile = app.getFileFromApplication("src/main/java/org/acme/", "GreetingResource.java").toPath();
        Path modifiedResourceFile = Paths.get("").resolve("src/test/java/org/acme/GreetingResource.java");
        Files.copy(modifiedResourceFile, resourceFile, StandardCopyOption.REPLACE_EXISTING);

        // Build the app
        QuarkusCliClient.Result result = app.buildOnJvm("--no-tests");
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
        // Start the DEV mode
        app.start();
        app.given()
                .get("/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Hello from modified file"));
    }
}
