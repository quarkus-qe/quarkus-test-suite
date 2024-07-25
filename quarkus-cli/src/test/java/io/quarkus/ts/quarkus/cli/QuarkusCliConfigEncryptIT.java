package io.quarkus.ts.quarkus.cli;

import static io.quarkus.test.bootstrap.config.QuarkusEncryptConfigCommandBuilder.AES_GCM_NO_PADDING_HANDLER_ENC_KEY;
import static io.quarkus.test.bootstrap.config.QuarkusEncryptConfigCommandBuilder.KeyFormat.base64;
import static io.quarkus.test.bootstrap.config.QuarkusEncryptConfigCommandBuilder.KeyFormat.plain;
import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.ENCRYPTED_SECRET_3_PROPERTY;
import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.UNKNOWN_SECRET_HANDLER_PROPERTY;
import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.EncryptProperties.SECRET_1;
import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.EncryptProperties.SECRET_2;
import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.EncryptProperties.SECRET_3;
import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.EncryptProperties.SECRET_4;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.config.QuarkusConfigCommand;
import io.quarkus.test.bootstrap.config.QuarkusEncryptConfigCommandBuilder;
import io.quarkus.test.bootstrap.config.QuarkusEncryptConfigCommandBuilder.EncryptionKeyFormatOpt;
import io.quarkus.test.bootstrap.config.QuarkusEncryptConfigCommandBuilder.EncryptionKeyOpt;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest;

@DisabledOnQuarkusVersion(version = "3\\.(9|10|11|12)\\..*", reason = "https://github.com/quarkusio/quarkus/pull/41203 merged in 3.13")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // remember, this is stateful test as well as stateful cmd builder
@Tag("QUARKUS-3456")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public class QuarkusCliConfigEncryptIT {

    private static QuarkusEncryptConfigCommandBuilder encryptBuilder = null;
    private static String encryptionKey = null;

    @Inject
    static QuarkusConfigCommand configCommand;

    @BeforeAll
    public static void beforeAll() {
        encryptBuilder = configCommand
                .withSmallRyeConfigCryptoDep()
                .encryptBuilder()
                .withSmallRyeConfigSourceKeystoreDep();
    }

    @Order(1)
    @Test
    public void encryptSecret_Base64SecretFormat_GenerateEncryptionKey() {
        // configured props are tested by EncryptPropertyTest#encryptedSecret_Base64SecretFormat_GeneratedEncryptionKey
        encryptBuilder
                .secret(SECRET_1.secret)
                .executeCommand()
                .secretConsumer(Assertions::assertNotNull)
                .storeSecretAsSecretExpression(SECRET_1.propertyName)
                .generatedKeyConsumer(encKey -> encryptionKey = encKey)
                .assertCommandOutputContains("""
                        The secret %s was encrypted to
                        """.formatted(SECRET_1.secret))
                .assertCommandOutputContains("""
                        with the generated encryption key (base64):
                        """);
    }

    @Order(2)
    @Test
    public void encryptSecret_PlainKeyFormat_ExistingEncryptionKey() {
        // configured props are tested by EncryptPropertyTest#encryptSecret_PlainKeyFormat_ExistingEncryptionKey
        encryptBuilder
                .encryptionKeyFormat(plain)
                .encryptionKeyFormatOpt(EncryptionKeyFormatOpt.SHORT)
                .encryptionKey(encryptionKey)
                .encryptionKeyOpt(EncryptionKeyOpt.SHORT)
                .secret(SECRET_2.secret)
                .executeCommand()
                .secretConsumer(Assertions::assertNotNull)
                .storeSecretAsSecretExpression(SECRET_2.propertyName)
                .assertCommandOutputContains("""
                        The secret %s was encrypted to
                        """.formatted(SECRET_2.secret))
                .assertCommandOutputNotContains("with the generated encryption key");
    }

    @Order(3)
    @Test
    public void failToDecryptSecretInWrongFormat() {
        // configured props are tested by EncryptPropertyTest#failToDecryptSecretInWrongFormat

        // prepare secret that is encrypted with a different encryption key than used by Quarkus application
        // so that unit test can see decryption fails with a wrong property and succeeds with a correct one
        // also any secret key that is not in "AES" algorithm will fail
        encryptBuilder
                .encryptionKeyFormat(base64)
                .encryptionKeyFormatOpt(EncryptionKeyFormatOpt.LONG)
                .encryptionKey(SECRET_3.encryptionKey)
                .encryptionKeyOpt(EncryptionKeyOpt.LONG)
                .secret(SECRET_3.secret)
                .doNotSetEncryptionKeyToSecretHandler()
                .executeCommand()
                .secretConsumer(Assertions::assertNotNull)
                .storeSecretAsSecretExpression(SECRET_3.propertyName)
                .storeSecretAsRawValue(ENCRYPTED_SECRET_3_PROPERTY)
                .assertCommandOutputContains("""
                        The secret %s was encrypted to
                        """.formatted(SECRET_3.secret))
                .assertCommandOutputNotContains("with the generated encryption key");
    }

    @DisabledOnOs(OS.WINDOWS) // Keytool command would require adjustments on Windows
    @Order(4)
    @Test
    public void testKeyStoreConfigSourceWithSecrets() {
        // configured keystore config source is tested by EncryptPropertyTest#testKeyStoreConfigSourceWithSecrets

        // this tests "Create Keystore" section
        // see https://quarkus.io/version/main/guides/config-secrets#create-a-keystore
        // unit test will check that Quarkus app retrieves secret correctly
        var propertiesKeystoreName = "properties";
        var propertiesKeystorePwd = "properties-password";
        var encKeystoreName = "key";
        var encKeystorePassword = "key-password";
        // use keystores (one for actual secrets, one for secret encryption key)
        encryptBuilder.getConfigCommand().addToApplicationPropertiesFile(
                "smallrye.config.source.keystore.\"properties\".path", propertiesKeystoreName,
                "smallrye.config.source.keystore.\"properties\".password", propertiesKeystorePwd,
                "smallrye.config.source.keystore.\"properties\".handler", "aes-gcm-nopadding",
                "smallrye.config.source.keystore.\"key\".path", encKeystoreName,
                "smallrye.config.source.keystore.\"key\".password", encKeystorePassword);

        // generate keystores
        var encKeyBase64Encoded = base64.format(QuarkusEncryptConfigCommandBuilder.generateEncryptionKey());
        encryptBuilder
                .encryptionKeyFormat(plain)
                .encryptionKeyFormatOpt(EncryptionKeyFormatOpt.LONG)
                .secret(SECRET_4.secret)
                .encryptionKey(encryptionKey)
                .executeCommand()
                .secretConsumer(Assertions::assertNotNull)
                .secretConsumer(secret -> encryptBuilder
                        .createKeyStore(SECRET_4.propertyName, secret, propertiesKeystoreName, propertiesKeystorePwd)
                        .createKeyStore(AES_GCM_NO_PADDING_HANDLER_ENC_KEY, encKeyBase64Encoded, encKeystoreName,
                                encKeystorePassword))
                .assertApplicationPropertiesDoesNotContain(SECRET_4.secret)
                .assertApplicationPropertiesDoesNotContain(SECRET_4.propertyName)
                .assertApplicationPropertiesDoesNotContain(encKeyBase64Encoded)
                .assertCommandOutputContains("""
                        The secret %s was encrypted to
                        """.formatted(SECRET_4.secret));
    }

    @Order(5)
    @Test
    public void testWrongSecretKeyHandler() {
        // configured property is tested by EncryptPropertyTest#testWrongSecretKeyHandler

        // add unknown secret handler so that unit test can assert this is not reported
        encryptBuilder.getConfigCommand()
                .addToApplicationPropertiesFile(UNKNOWN_SECRET_HANDLER_PROPERTY, "${unknown-secret-handler::hush-hush}");
    }

    @Order(6)
    @Test
    public void testEncryptCommandHelp() {
        encryptBuilder
                .printOutHelp()
                .assertCommandOutputContains("""
                        Encrypt a Secret value using the AES/GCM/NoPadding algorithm as a default
                        """)
                .assertCommandOutputContains("""
                        encryption key is generated unless a specific key is set with the --key option
                        """)
                .assertCommandOutputContains("""
                        Usage: quarkus config encrypt [-eh] [--verbose] [-f=<encryptionKeyFormat>]
                        """)
                .assertCommandOutputContains("[-k=<encryptionKey>] SECRET")
                .assertCommandOutputContains("""
                        The Secret value to encrypt
                        """)
                .assertCommandOutputContains("-f, --format=<encryptionKeyFormat>")
                .assertCommandOutputContains("""
                        The Encryption Key Format (base64 / plain)
                        """)
                .assertCommandOutputContains("""
                        Print more context on errors and exceptions
                        """)
                .assertCommandOutputContains("""
                        Display this help message
                        """)
                .assertCommandOutputContains("Verbose mode")
                .assertCommandOutputContains("-k, --key=<encryptionKey>")
                .assertCommandOutputContains("The Encryption Key");
    }

    @Order(7)
    @Test
    public void testQuarkusApplicationWithGeneratedSecrets() {
        encryptBuilder.getConfigCommand().buildAppAndExpectSuccess(EncryptPropertyTest.class);
    }

}
