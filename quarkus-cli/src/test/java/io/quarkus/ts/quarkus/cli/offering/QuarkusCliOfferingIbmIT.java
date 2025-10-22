package io.quarkus.ts.quarkus.cli.offering;

import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.getExtensionLineFromListOutput;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.updateRegistryConfigFileWithOffering;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.EnabledOnQuarkusVersion;

@Tag("quarkus-cli")
@Tag("QUARKUS-6397")
@QuarkusScenario
@DisabledOnNative(reason = "Only for JVM verification")
@EnabledOnQuarkusVersion(version = ".*redhat.*", reason = "Need set up registry config for prod testing")
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "https://issues.redhat.com/browse/QUARKUS-6780")
public class QuarkusCliOfferingIbmIT extends QuarkusCliOfferingBase {

    @BeforeAll
    public static void setOfferingInConfigFile() throws IOException {
        updateRegistryConfigFileWithOffering("ibm");
    }

    @Test
    public void listedExtensionShouldContainSupportScopeForLangchain4jOpenAi() throws IOException {
        QuarkusCliClient.Result result = cliClient.listExtensions("--support-scope");
        assertTrue(result.getOutput().contains(LANGCHAIN4J_OPENAI_EXTENSION_NAME)
                && result.getOutput().contains(LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT),
                "--support-scope option output is unexpected. Output: " + result.getOutput());

        String extensionLine = getExtensionLineFromListOutput(result, LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT);

        assertNotNull(extensionLine);
        assertThat(
                LANGCHAIN4J_OPENAI_EXTENSION_ARTIFACT + " should have support scope equal to "
                        + LANGCHAIN4J_OPENAI_SUPPORT_SCOPE,
                extensionLine, containsString(LANGCHAIN4J_OPENAI_SUPPORT_SCOPE));
    }

    @Override
    public String getQuarkusPlatformGroupId() {
        return "com.redhat.quarkus.platform";
    }

    @Override
    public String langchain4JBomVersion() {
        return "${quarkus.platform.version}";
    }
}
