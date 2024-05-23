package io.quarkus.ts.quarkus.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        final Path POM_PATH = Paths.get("target", QuarkusCliPomIntegrityIT.class.getSimpleName(), APP_NAME, "pom.xml");
        QuarkusCliRestService app = cliClient.createApplication("my-quarkus-app",
                QuarkusCliClient.CreateApplicationRequest.defaults());

        List<String> initialPomContent = Files.readAllLines(POM_PATH);
        initialPomContent.add(1, COMMENT);

        // Add extension
        app.installExtension(NEW_EXTENSION);
        List<String> updatedPomContent = Files.readAllLines(POM_PATH);
        Assertions.assertTrue(updatedPomContent.contains(COMMENT), "The comment after add extension still should be there");

        // Remove extension
        app.removeExtension(NEW_EXTENSION);
        List<String> finalPomContent = Files.readAllLines(POM_PATH);
        Assertions.assertTrue(finalPomContent.contains(COMMENT), "The comment after remove extension still should be there");
    }
}
