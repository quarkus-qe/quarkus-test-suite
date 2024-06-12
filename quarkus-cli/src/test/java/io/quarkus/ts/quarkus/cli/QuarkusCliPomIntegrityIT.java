package io.quarkus.ts.quarkus.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;
import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class QuarkusCliPomIntegrityIT {
    /*
     * This scenario is related to the backport https://github.com/quarkusio/quarkus/issues/39088
     *
     */
    private static final String NEW_EXTENSION = "quarkus-kafka-client";
    private static final String COMMENT = "<!-- Disable native build on this module -->";

    private static final String APP_NAME = "my-quarkus-app";

    private static Path POM_PATH;

    @Inject
    static QuarkusCliClient cliClient;

    @BeforeAll
    public static void loadPom() {
        try (InputStream input = QuarkusCliPomIntegrityIT.class.getClassLoader().getResourceAsStream("test.properties")) {
            Properties properties = new Properties();
            if (input == null) {
                throw new IOException("Configuration file not found");
            }
            properties.load(input);
            String rawPath = properties.getProperty("app.pom.path");
            if (rawPath == null) {
                throw new RuntimeException("Path configuration is missing");
            }
            String pathStr = rawPath
                    .replace("{className}", QuarkusCliPomIntegrityIT.class.getSimpleName())
                    .replace("{appName}", APP_NAME);
            POM_PATH = Paths.get(pathStr);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load configuration", ex);
        }

    }

    @Test
    public void shouldKeepCommentInPomAfterAddAndRemoveExtension() throws IOException {
        QuarkusCliRestService app = cliClient.createApplication(APP_NAME);
        File pomFile = POM_PATH.toFile();

        try (var fileWriter = new FileWriter(pomFile, true)) {
            fileWriter.write(System.lineSeparator());
            fileWriter.write(COMMENT);
        }

        // Add extension
        app.installExtension(NEW_EXTENSION);
        assertPomContainsComment(pomFile, "The comment after add extension still should be there");

        // Remove extension
        app.removeExtension(NEW_EXTENSION);
        assertPomContainsComment(pomFile, "The comment after remove extension still should be there");
    }

    private static void assertPomContainsComment(File pom, String errMessage) throws IOException {
        List<String> pomContent = Files.readAllLines(pom.toPath());
        // the comment is added to the (new) last line,
        // however Quarkus formats the POM file when extension is added / removed
        // and removes the new line separator, so we need to look for the comment in the last line;
        var lastLine = pomContent.get(pomContent.size() - 1);
        Assertions.assertTrue(lastLine.contains(COMMENT), errMessage);
    }
}
