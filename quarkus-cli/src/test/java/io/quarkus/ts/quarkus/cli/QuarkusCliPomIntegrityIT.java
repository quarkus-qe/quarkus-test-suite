package io.quarkus.ts.quarkus.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
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

    @Inject
    static QuarkusCliClient cliClient;

    @Test
    public void shouldKeepCommentInPomAfterAddAndRemoveExtension() throws IOException {
        QuarkusCliRestService app = cliClient.createApplication(APP_NAME);
        var pomFile = app.getFileFromApplication("pom.xml");

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
