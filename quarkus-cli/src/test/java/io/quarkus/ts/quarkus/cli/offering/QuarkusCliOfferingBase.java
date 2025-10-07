package io.quarkus.ts.quarkus.cli.offering;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.QUARKUS_CONFIG;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.QUARKUS_TEST_CONFIG;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.assertCorrectLangChain4jBom;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.assertCorrectPlatformBom;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.getExtensionLineFromListOutput;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;

public abstract class QuarkusCliOfferingBase {

    public static final String REST_EXTENSION_NAME = "REST Jackson";
    public static final String REST_EXTENSION_ARTIFACT = "quarkus-rest-jackson";
    public static final String REST_SUPPORT_SCOPE = "supported";
    public static final String LANGCHAIN4J_OPENAI_EXTENSION_NAME = "LangChain4j OpenAI ";
    public static final String LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT = "quarkus-langchain4j-openai";
    public static final String LANGCHAIN4J_OPENAI_SUPPORT_SCOPE = "tech-preview";

    @Inject
    static QuarkusCliClient cliClient;

    @BeforeAll
    public static void prepareConfigBackup() throws IOException {
        FileUtils.copyFile(QUARKUS_CONFIG, QUARKUS_TEST_CONFIG);
    }

    @Test
    public void listedExtensionShouldContainSupportScope() {
        QuarkusCliClient.Result result = cliClient.listExtensions("--support-scope");
        assertTrue(result.getOutput().contains(REST_EXTENSION_NAME)
                && result.getOutput().contains(REST_EXTENSION_ARTIFACT),
                "--support-scope option output should contain" + REST_EXTENSION_ARTIFACT + ". Output: " + result.getOutput());

        String extensionLine = getExtensionLineFromListOutput(result, REST_EXTENSION_ARTIFACT);

        assertNotNull(extensionLine);
        assertThat(REST_EXTENSION_ARTIFACT + " should have support scope equal to " + REST_SUPPORT_SCOPE,
                extensionLine, containsString(REST_SUPPORT_SCOPE));
    }

    @Test
    public void createAndBuildAppWhenOfferingIsSet() throws IOException {
        // Check if it's possible to create and build app when the offering is set in registry
        Path pathToTmpDirectory = Files.createTempDirectory("cli");
        File pathToConfigInTmpDirectory = Paths.get(pathToTmpDirectory.toAbsolutePath().toString(), ".quarkus",
                "config.yaml").toFile();
        FileUtils.copyFile(QUARKUS_TEST_CONFIG, pathToConfigInTmpDirectory);

        QuarkusCliRestService app = cliClient.createApplication("app",
                defaults().withExtensions(REST_EXTENSION_ARTIFACT, LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT),
                pathToTmpDirectory.toAbsolutePath().toString());
        File pom = app.getFileFromApplication("pom.xml");
        assertCorrectPlatformBom(pom, getQuarkusPlatformGroupId());
        assertCorrectLangChain4jBom(pom, langchain4JBomVersion());
        QuarkusCliClient.Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
    }

    public abstract String getQuarkusPlatformGroupId();

    public abstract String langchain4JBomVersion();
}
