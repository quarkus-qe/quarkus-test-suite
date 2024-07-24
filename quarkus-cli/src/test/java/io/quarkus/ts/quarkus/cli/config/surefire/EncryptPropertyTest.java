package io.quarkus.ts.quarkus.cli.config.surefire;

import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.EncryptProperties.SECRET_1;
import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.EncryptProperties.SECRET_2;
import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.EncryptProperties.SECRET_3;
import static io.quarkus.ts.quarkus.cli.config.surefire.EncryptPropertyTest.EncryptProperties.SECRET_4;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.NoSuchElementException;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

/**
 * This test is only support to run inside QuarkusCliConfigEncryptIT.
 */
@QuarkusTest
public class EncryptPropertyTest {

    public static final String ENCRYPTED_SECRET_3_PROPERTY = "encrypted-secret-3";
    public static final String UNKNOWN_SECRET_HANDLER_PROPERTY = "unknown-secret-handler";

    @Inject
    SmallRyeConfig config;

    @Test
    void encryptedSecret_Base64SecretFormat_GeneratedEncryptionKey() {
        assertEquals(SECRET_1.secret, getSecret1());
    }

    @Test
    public void encryptSecret_PlainKeyFormat_ExistingEncryptionKey() {
        assertEquals(SECRET_2.secret, getSecret2());
    }

    @Test
    public void failToDecryptSecretInWrongFormat() {
        // wrong encryption key
        assertThrows(RuntimeException.class, () -> getSecret3(config));
        // right encryption key
        var configWithRightKey = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .addDiscoveredSecretKeysHandlers()
                .withDefaultValue("smallrye.config.secret-handler.aes-gcm-nopadding.encryption-key",
                        encode(SECRET_3.encryptionKey))
                .withDefaultValue(SECRET_3.propertyName,
                        "${aes-gcm-nopadding::%s}".formatted(config.getConfigValue(ENCRYPTED_SECRET_3_PROPERTY).getValue()))
                .build();
        assertEquals(SECRET_3.secret, getSecret3(configWithRightKey));
    }

    @Test
    public void testWrongSecretKeyHandler() {
        assertThrows(NoSuchElementException.class, this::getSecretFromUnknownSecretHandler);
    }

    @Test
    public void testKeyStoreConfigSourceWithSecrets() {
        Assumptions.assumeFalse(OS.WINDOWS.isCurrentOs()); // Keytool command would require adjustments on Windows

        assertEquals(SECRET_4.secret, getSecret4());
    }

    private void getSecretFromUnknownSecretHandler() {
        config.getValue(UNKNOWN_SECRET_HANDLER_PROPERTY, String.class);
    }

    private String getSecret1() {
        return config.getValue(SECRET_1.propertyName, String.class);
    }

    private String getSecret2() {
        return config.getValue(SECRET_2.propertyName, String.class);
    }

    private static String getSecret3(Config config) {
        return config.getValue(SECRET_3.propertyName, String.class);
    }

    private String getSecret4() {
        return config.getValue(SECRET_4.propertyName, String.class);
    }

    private static String encode(String key) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString((key.getBytes(StandardCharsets.UTF_8)));
    }

    public enum EncryptProperties {
        SECRET_1("secret-1", "!@#$^%^&*()__++_)--=", null),
        SECRET_2("secret-2", "charter school", null),
        SECRET_3("secret-3", "Jr Gong", "Make It Bun Dem"),
        SECRET_4("secret-4", "Joe Biden", null);

        public final String propertyName;
        public final String secret;
        public final String encryptionKey;

        EncryptProperties(String propertyName, String secret, String encryptionKey) {
            this.propertyName = propertyName;
            this.secret = secret;
            this.encryptionKey = encryptionKey;
        }
    }
}
