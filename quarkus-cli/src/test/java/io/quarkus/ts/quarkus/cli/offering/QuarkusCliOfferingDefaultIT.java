package io.quarkus.ts.quarkus.cli.offering;

import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.QUARKUS_CONFIG;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.QUARKUS_TEST_CONFIG;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.getExtensionLineFromListOutput;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.getQuarkusVersionWithoutNumberSuffix;
import static io.quarkus.ts.quarkus.cli.offering.QuarkusCliOfferingUtils.updateRegistryConfigFileWithOffering;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import jakarta.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

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
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "https://issues.redhat.com/browse/QUARKUS-6780")
public class QuarkusCliOfferingDefaultIT {

    public static final String REST_EXTENSION_NAME = "REST Jackson";
    public static final String REST_EXTENSION_ARTIFACT = "quarkus-rest-jackson";
    public static final String REST_SUPPORT_SCOPE = "supported";

    @Inject
    static QuarkusCliClient cliClient;

    @BeforeEach
    public void prepareConfigBackup() throws IOException {
        FileUtils.copyFile(QUARKUS_CONFIG, QUARKUS_TEST_CONFIG);
    }

    @Test
    @Order(Integer.MAX_VALUE)
    public void listExtensionsWithNoOffering() {
        QuarkusCliClient.Result result = cliClient.listExtensions("--support-scope");
        assertTrue(result.getOutput().contains(REST_EXTENSION_NAME)
                && result.getOutput().contains(REST_EXTENSION_ARTIFACT),
                "--support-scope option output should contain" + REST_EXTENSION_ARTIFACT + ". Output: " + result.getOutput());

        String extensionLine = getExtensionLineFromListOutput(result, REST_EXTENSION_ARTIFACT);

        assertNotNull(extensionLine);
        String quarkusVersionWithoutNumber = getQuarkusVersionWithoutNumberSuffix();
        assertThat(REST_EXTENSION_ARTIFACT + " should not have support scope as no offering was selected",
                extensionLine, not(containsString(REST_SUPPORT_SCOPE)));
        assertThat(REST_EXTENSION_ARTIFACT + " should contain Quarkus version " + quarkusVersionWithoutNumber,
                extensionLine, containsString(quarkusVersionWithoutNumber));
    }

    @Test
    public void listExtensionsWithWrongOffering() throws IOException {
        // Testing output when the unknow offering is set
        updateRegistryConfigFileWithOffering("unknown-offering");
        QuarkusCliClient.Result result = cliClient.listExtensions("--support-scope");
        assertTrue(result.getOutput().contains(REST_EXTENSION_NAME)
                && result.getOutput().contains(REST_EXTENSION_ARTIFACT),
                "--support-scope option output should contain" + REST_EXTENSION_ARTIFACT + ". Output: " + result.getOutput());

        String extensionLine = getExtensionLineFromListOutput(result, REST_EXTENSION_ARTIFACT);

        assertNotNull(extensionLine);
        assertThat(REST_EXTENSION_ARTIFACT + " should not have support scope equal to " + REST_SUPPORT_SCOPE,
                extensionLine, not(containsString(REST_SUPPORT_SCOPE)));
        assertThat(
                REST_EXTENSION_ARTIFACT + " should have already released version from public registry and not testing version"
                        + QuarkusProperties.getVersion(),
                extensionLine, not(containsString(getQuarkusVersionWithoutNumberSuffix())));
    }

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/50480")
    public void listExtensionsWithEmptyOffering() throws IOException {
        // Testing output when the unknow offering is set
        updateRegistryConfigFileWithOffering("");
        QuarkusCliClient.Result result = cliClient.listExtensions("--support-scope");
        assertFalse(result.isSuccessful());
        // TODO the output in this case is not clear, needs to be updated when issue is fixed
    }
}
