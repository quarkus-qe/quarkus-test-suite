package io.quarkus.ts.quarkus.cli.config.surefire;

import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.CREATE_1;
import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.CREATE_2;
import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.CREATE_3;
import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.UPDATE_1;
import static io.quarkus.ts.quarkus.cli.config.surefire.SetPropertyTest.Properties.UPDATE_2;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

/**
 * This test is only support to run inside QuarkusCliConfigSetIT.
 */
@QuarkusTest
public class SetPropertyTest {

    @Inject
    SmallRyeConfig config;

    @Test
    void createPropertyCommand_NoSecret_ApplicationPropertiesDoesNotExist() {
        assertEquals(CREATE_1.propertyValue, config.getRawValue(CREATE_1.propertyName));
    }

    @Test
    void createPropertyCommand_NoSecret_ApplicationPropertiesExists() {
        assertEquals(CREATE_2.propertyValue, config.getRawValue(CREATE_2.propertyName));
    }

    @Test
    @DisabledIfSystemProperty(named = "gh-action-disable-on-win", matches = "true", disabledReason = "Some windows don't have all language pack/locales so it causing it fail")
    void createPropertyCommand_EncryptValue_UseExistingEncryptionKey() {
        assertEquals(CREATE_3.propertyValue, config.getRawValue(CREATE_3.propertyName));
    }

    @Test
    void updatePropertyCommand_ReplaceOriginalValueWithNewValue_EncryptNewValue() {
        assertEquals(UPDATE_1.propertyValue, config.getRawValue(UPDATE_1.propertyName));
    }

    @Test
    void updatePropertyCommand_EncryptOriginalValue() {
        assertEquals(UPDATE_2.propertyValue, config.getRawValue(UPDATE_2.propertyName));
    }

    public enum Properties {
        CREATE_1("create-one-key", "create-one-value"),
        CREATE_2("create-two-key", "create-two-value"),
        CREATE_3("create-three-key", "create-three-value"),
        UPDATE_1("update-one-key", "update-one-value"),
        UPDATE_2("update-two-key", "update-two-value");

        public final String propertyName;
        public final String propertyValue;

        Properties(String propertyName, String propertyValue) {
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
        }
    }
}
