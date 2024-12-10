package io.quarkus.ts.quarkus.cli;

import static io.quarkus.test.bootstrap.config.QuarkusEncryptConfigCommandBuilder.AES_GCM_NO_PADDING_HANDLER_ENC_KEY;
import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.CREATE_1;
import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.CREATE_2;
import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.CREATE_3;
import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.UPDATE_1;
import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.UPDATE_2;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.config.QuarkusConfigCommand;
import io.quarkus.test.bootstrap.config.QuarkusSetConfigCommandBuilder.EncryptOption;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest;

@DisabledOnQuarkusVersion(version = "3\\.(9|10|11|12)\\..*", reason = "https://github.com/quarkusio/quarkus/pull/41203 merged in 3.13")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // remember, this is stateful test as well as stateful cmd builder
@Tag("QUARKUS-3456")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnNative // Only for JVM verification
public class QuarkusCliConfigSetIT {

    @Inject
    static QuarkusConfigCommand configCommand;

    @BeforeAll
    public static void beforeAll() {
        configCommand.withSmallRyeConfigCryptoDep();
    }

    @Order(1)
    @Test
    public void createPropertyCommand_NoSecret_ApplicationPropertiesDoesNotExist() {
        // configured props tested by SetPropertyTest#createPropertyCommand_NoSecret_ApplicationPropertiesDoesNotExist
        configCommand.removeApplicationProperties();
        configCommand
                .createProperty()
                .name(CREATE_1.propertyName)
                .value(CREATE_1.propertyValue)
                .executeCommand()
                .assertCommandOutputContains("""
                        Could not find an application.properties file, creating one now
                        """)
                .assertCommandOutputContains("""
                        application.properties file created in src/main/resources
                        """)
                .assertCommandOutputContains("""
                        Adding configuration %s with value %s
                        """.formatted(CREATE_1.propertyName, CREATE_1.propertyValue))
                .assertApplicationPropertiesContains(CREATE_1.propertyName, CREATE_1.propertyValue);
    }

    @Order(2)
    @Test
    public void createPropertyCommand_NoSecret_ApplicationPropertiesExists() {
        // configured props tested by SetPropertyTest#createPropertyCommand_NoSecret_ApplicationPropertiesExists
        configCommand
                .createProperty()
                .name(CREATE_2.propertyName)
                .value(CREATE_2.propertyValue)
                .executeCommand()
                .assertCommandOutputContains("""
                        Adding configuration %s with value %s
                        """.formatted(CREATE_2.propertyName, CREATE_2.propertyValue))
                .assertCommandOutputNotContains("""
                        Could not find an application.properties file, creating one now
                        """)
                .assertCommandOutputNotContains("""
                        application.properties file created in src/main/resources
                        """)
                .assertApplicationPropertiesContains(CREATE_2.propertyName, CREATE_2.propertyValue);
    }

    @Order(3)
    @Test
    @DisabledIfSystemProperty(named = "gh-action-disable-on-win", matches = "true", disabledReason = "Some windows don't have all language pack/locales so it causing it fail")
    public void createPropertyCommand_EncryptValue_UseExistingEncryptionKey() {
        // configured props tested by SetPropertyTest#createPropertyCommand_EncryptValue_UseExistingEncryptionKey
        // use existing secret
        configCommand.addToApplicationPropertiesFile(AES_GCM_NO_PADDING_HANDLER_ENC_KEY, "čingischán%ˇáíé=");

        configCommand
                .createProperty()
                .name(CREATE_3.propertyName)
                .value(CREATE_3.propertyValue)
                .encrypt(EncryptOption.LONG)
                .executeCommand()
                .assertCommandOutputContains("""
                        Adding configuration %s with value ${aes-gcm-nopadding::
                        """.formatted(CREATE_3.propertyName))
                .assertCommandOutputNotContains(CREATE_3.propertyValue)
                .assertApplicationPropertiesContains(CREATE_3.propertyName + "=")
                .assertApplicationPropertiesDoesNotContain(CREATE_3.propertyValue);
    }

    @Order(4)
    @Test
    public void updatePropertyCommand_ReplaceOriginalValueWithNewValue_EncryptNewValue() {
        // configured props tested by SetPropertyTest#updatePropertyCommand_ReplaceOriginalValueWithNewValue_EncryptNewValue
        var wrongValue = "Wrong value! Danger Will Robinson!";
        configCommand.addToApplicationPropertiesFile(UPDATE_1.propertyName, wrongValue);

        configCommand
                .updateProperty()
                .name(UPDATE_1.propertyName)
                .value(UPDATE_1.propertyValue)
                .encrypt(EncryptOption.SHORT)
                .executeCommand()
                .assertCommandOutputContains("""
                        Setting configuration %s to value ${aes-gcm-nopadding::
                        """.formatted(UPDATE_1.propertyName))
                .assertCommandOutputNotContains(UPDATE_1.propertyValue)
                .assertCommandOutputNotContains(wrongValue)
                .assertApplicationPropertiesDoesNotContain(UPDATE_1.propertyValue)
                .assertApplicationPropertiesDoesNotContain(wrongValue);
    }

    @Order(5)
    @Test
    public void updatePropertyCommand_EncryptOriginalValue() {
        // configured props tested by SetPropertyTest#updatePropertyCommand_EncryptOriginalValue
        configCommand.addToApplicationPropertiesFile(UPDATE_2.propertyName, UPDATE_2.propertyValue);

        configCommand
                .updateProperty()
                .name(UPDATE_2.propertyName)
                .encrypt(EncryptOption.LONG)
                .executeCommand()
                .assertCommandOutputContains("""
                        Setting configuration %s to value ${aes-gcm-nopadding::
                        """.formatted(UPDATE_2.propertyName))
                .assertCommandOutputNotContains(UPDATE_2.propertyValue)
                .assertApplicationPropertiesDoesNotContain(UPDATE_2.propertyValue);
    }

    @Order(6)
    @Test
    public void testSetCommandHelp() {
        configCommand
                .setProperty()
                .printOutHelp()
                .assertCommandOutputContains("""
                        Sets a configuration in application.properties
                        """)
                .assertCommandOutputContains("""
                        Usage: quarkus config set [-ehk] [--verbose] NAME [VALUE]
                        """)
                .assertCommandOutputContains("""
                        Configuration name
                        """)
                .assertCommandOutputContains("""
                        Configuration value
                        """)
                .assertCommandOutputContains("""
                        Print more context on errors and exceptions
                        """)
                .assertCommandOutputContains("""
                        Display this help message
                        """)
                .assertCommandOutputContains("""
                        Verbose mode
                        """)
                .assertCommandOutputContains("""
                        Encrypt the configuration value
                        """)
                .assertCommandOutputContains("-k, --encrypt");
    }

    @Order(8)
    @Test
    @DisabledIfSystemProperty(named = "gh-action-disable-on-win", matches = "true", disabledReason = "Some windows don't have all language pack/locales so it causing it fail")
    public void testQuarkusApplicationWithModifiedApplicationProperties() {
        configCommand.buildAppAndExpectSuccess(SetPropertyTest.class);
    }
}
