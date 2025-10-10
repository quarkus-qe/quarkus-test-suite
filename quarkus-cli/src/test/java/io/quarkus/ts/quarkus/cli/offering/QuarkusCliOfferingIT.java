package io.quarkus.ts.quarkus.cli.offering;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.QUARKUS_CONFIG;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.QUARKUS_CONFIG_BACKUP;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.assertCorrectPlatformBom;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.getExtensionLineFromListOutput;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import jakarta.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.bootstrap.QuarkusCliRestService;

public abstract class QuarkusCliOfferingIT {

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
        FileUtils.copyFile(QUARKUS_CONFIG, QUARKUS_CONFIG_BACKUP);
    }

    @AfterAll
    public static void restoreBackupConfig() throws IOException {
        FileUtils.copyFile(QUARKUS_CONFIG_BACKUP, QUARKUS_CONFIG);
        FileUtils.delete(QUARKUS_CONFIG_BACKUP);
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
    public void createAndBuildAppWhenOfferingIsSet() {
        // Check if it's possible to create and build app when the offering is set in registry
        QuarkusCliRestService app = cliClient.createApplication("app",
                defaults().withExtensions(REST_EXTENSION_ARTIFACT, LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT));
        assertCorrectPlatformBom(app.getFileFromApplication("pom.xml"), getQuarkusPlatformGroupId());
        QuarkusCliClient.Result result = app.buildOnJvm();
        assertTrue(result.isSuccessful(), "The application didn't build on JVM. Output: " + result.getOutput());
    }

    public abstract String getQuarkusPlatformGroupId();
}
