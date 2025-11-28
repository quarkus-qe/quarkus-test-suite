package io.quarkus.ts.quarkus.cli.offering;

import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.QUARKUS_TEST_CONFIG;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.getExtensionLineFromListOutput;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.updateRegistryConfigFileWithOffering;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.EnabledOnQuarkusVersion;
import io.quarkus.test.services.quarkus.model.QuarkusProperties;

@Tag("quarkus-cli")
@Tag("QUARKUS-6397")
@QuarkusScenario
@DisabledOnNative(reason = "Only for JVM verification")
@EnabledOnQuarkusVersion(version = ".*redhat.*", reason = "Need set up registry config for prod testing")
public class QuarkusCliOfferingRedHatIT extends QuarkusCliOfferingBase {

    @BeforeAll
    public static void setOfferingInConfigFile() throws IOException {
        updateRegistryConfigFileWithOffering("redhat");
    }

    @Test
    public void listedExtensionShouldNotContainSupportScopeForLangchain4jOpenAi() {
        QuarkusCliClient.Result result = cliClient.listExtensions("--support-scope",
                "--config=" + QUARKUS_TEST_CONFIG.getAbsolutePath(),
                "-s=" + LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT);
        assertTrue(result.getOutput().contains(LANGCHAIN4J_OPENAI_EXTENSION_NAME)
                && result.getOutput().contains(LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT),
                "--support-scope option output is unexpected. Output: " + result.getOutput());

        String extensionLine = getExtensionLineFromListOutput(result, LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT);

        assertNotNull(extensionLine);
        assertThat(LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT + " should not have support scope",
                extensionLine, not(containsString(LANGCHAIN4J_OPENAI_SUPPORT_SCOPE)));
    }

    @Override
    public String getQuarkusPlatformGroupId() {
        return "com.redhat.quarkus.platform";
    }

    @Override
    public String langchain4JBomVersion() {
        // The version of platform should be unproductized version e.g. 3.27.0
        return QuarkusProperties.getVersion().replaceAll("^(\\d+\\.\\d+\\.\\d+).*", "$1");
    }
}
