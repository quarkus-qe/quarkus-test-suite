package io.quarkus.ts.quarkus.cli;

import static io.quarkus.ts.quarkus.cli.tls.surefire.TlsCommandTest.CERT_NAME;
import static io.quarkus.ts.quarkus.cli.tls.surefire.TlsCommandTest.CN;
import static io.quarkus.ts.quarkus.cli.tls.surefire.TlsCommandTest.PASSWORD;
import static io.quarkus.ts.quarkus.cli.tls.surefire.TlsCommandTest.TRUST_STORE_PATH;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.function.Function;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.logging.Log;
import io.quarkus.test.bootstrap.QuarkusCliCommandResult;
import io.quarkus.test.bootstrap.tls.GenerateCertOptions;
import io.quarkus.test.bootstrap.tls.GenerateQuarkusCaOptions;
import io.quarkus.test.bootstrap.tls.QuarkusTlsCommand;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.ts.quarkus.cli.tls.surefire.TlsCommandTest;

@Tag("QUARKUS-4592")
@Tag("quarkus-cli")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public class QuarkusCliTlsCommandIT {

    // locations are expected according to the Quarkus docs describes generation target
    private static final File QUARKUS_BASE_DIR = new File(System.getenv("HOME"), ".quarkus");
    private static final File DEV_CA_CERT_FILE = new File(QUARKUS_BASE_DIR, "quarkus-dev-root-ca.pem");
    private static final File DEV_CA_PK_FILE = new File(QUARKUS_BASE_DIR, "quarkus-dev-root-key.pem");

    @Inject
    static QuarkusTlsCommand tlsCommand;

    @Order(1)
    @Test
    public void generateQuarkusCa() {
        // also prepares state for assertion in TlsCommandTest
        deleteFileIfExists(DEV_CA_CERT_FILE);
        deleteFileIfExists(DEV_CA_PK_FILE);
        tlsCommand
                .generateQuarkusCa()
                .withOption(GenerateQuarkusCaOptions.TRUSTSTORE_LONG)
                .withOption(GenerateQuarkusCaOptions.RENEW_SHORT)
                .executeCommand()
                .assertCommandOutputContains("Root CA certificate generated successfully")
                .assertCommandOutputContains("Quarkus Development CA generated and installed")
                .assertFileExistsStr(cmd -> cmd.getOutputLineRemainder("Truststore generated successfully:"));
        assertTrue(DEV_CA_CERT_FILE.exists(),
                "Quarkus CLI subcommand 'tls generate-quarkus-ca' didn't generate Quarkus DEV CA certificate");
        assertTrue(DEV_CA_PK_FILE.exists(),
                "Quarkus CLI subcommand 'tls generate-quarkus-ca' didn't generate Quarkus DEV CA private key");
    }

    @Order(2)
    @Test
    public void generateCertificate() {
        // also prepares state for assertion in TlsCommandTest
        String appSvcDir = tlsCommand.getApp().getServiceFolder().toAbsolutePath().toString();
        tlsCommand
                .generateCertificate()
                .withOption(GenerateCertOptions.COMMON_NAME_LONG, CN)
                .withOption(GenerateCertOptions.NAME_SHORT, CERT_NAME)
                .withOption(GenerateCertOptions.PASSWORD_SHORT, PASSWORD)
                .withOption(GenerateCertOptions.DIRECTORY_LONG, appSvcDir)
                .executeCommand()
                .assertCommandOutputContains("Quarkus Dev CA certificate found at " + DEV_CA_CERT_FILE.getAbsolutePath())
                .assertCommandOutputContains("PKCS12 keystore and truststore generated successfully!")
                .assertFileExistsStr(cmd -> cmd.getOutputLineRemainder("Key Store File:"))
                .assertFileExistsStr(cmd -> cmd.getOutputLineRemainder("Trust Store File:"))
                // save truststore path in application properties so that we can use it in TlsCommandTest
                .addToAppProps(cmd -> TRUST_STORE_PATH + "=" + addEscapes(cmd.getOutputLineRemainder("Trust Store File:")))
                .assertCommandOutputContains(
                        "Signed Certificate generated successfully and exported into `%s-keystore.p12`".formatted(CERT_NAME))
                // following properties are set by this tls command, and we want to use them TlsCommandTest as well
                .addToAppProps(getPropertyFromEnvFileAndChangeProfileToTest("quarkus.tls.key-store.p12.path"))
                .addToAppProps(getPropertyFromEnvFileAndChangeProfileToTest("quarkus.tls.key-store.p12.password"));
    }

    @Order(3)
    @Test
    public void runTestsUsingGeneratedCerts() {
        tlsCommand.buildAppAndExpectSuccess(TlsCommandTest.class);
    }

    private static void deleteFileIfExists(File file) {
        if (file.exists()) {
            // better inform so that user know his local Quarkus DEV CA is gone
            Log.info("Deleting file: " + file);
            if (!file.delete()) {
                throw new IllegalStateException("Failed to delete file: " + file);
            }
        }
    }

    @Test
    public void testHelpOption() {
        tlsCommand.generateQuarkusCa()
                .withOption(GenerateQuarkusCaOptions.HELP_LONG)
                .executeCommand()
                .assertCommandOutputContains("--install")
                .assertCommandOutputContains("--renew")
                .assertCommandOutputContains("--truststore")
                .assertCommandOutputContains("Generate Quarkus Dev CA certificate and private key")
                .assertCommandOutputContains("Install the generated CA into the system keychain")
                .assertCommandOutputContains("Update certificate if already created")
                .assertCommandOutputContains("Generate a PKCS12");
        tlsCommand.generateCertificate()
                .withOption(GenerateCertOptions.HELP_SHORT)
                .executeCommand()
                .assertCommandOutputContains("--directory")
                .assertCommandOutputContains("--name")
                .assertCommandOutputContains("--password")
                .assertCommandOutputContains("--renew")
                .assertCommandOutputContains("Generate a TLS certificate with the Quarkus Dev CA if available")
                .assertCommandOutputContains("The common name of the certificate")
                .assertCommandOutputContains("The directory in which the certificates will be created")
                .assertCommandOutputContains("Name of the certificate")
                .assertCommandOutputContains("The password of the keystore")
                .assertCommandOutputContains("Whether existing certificates will need to be replaced");
    }

    private static Function<QuarkusCliCommandResult, String> getPropertyFromEnvFileAndChangeProfileToTest(String propertyKey) {
        return cmd -> {
            // add generated env vars also to application properties under test profile
            // so that we can also use them in TlsCommandTest
            var propertyValue = cmd.getPropertyValueFromEnvFile("%dev." + propertyKey);
            return "%test." + propertyKey + "=" + addEscapes(propertyValue);
        };
    }

    private static String addEscapes(String propertyValue) {
        if (OS.WINDOWS.isCurrentOs()) {
            // we need to quote back slashes passed as command lines in Windows as they have special meaning
            // TODO: move this to the QE Test Framework as this is repeated a lot
            return propertyValue.replace("\\", "\\\\");
        }
        return propertyValue;
    }
}
